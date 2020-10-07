import justext
import json
import os
import re
import requests
import time

from bs4 import BeautifulSoup
from operator import itemgetter
from xml.sax.saxutils import escape, unescape

from reldi_hr_tokeniser.tokeniser import sentence_split, tokenize, generate_tokenizer

from .utils import (
    DataMismatchException, PipelineException,
    XLikeToken, MarcellToken,
    Document, Paragraph, Sentence,
    split_in_n_grams, make_term_hash
)


module_dir = os.path.dirname(os.path.abspath(__file__))

XML_ENVELOPE = """<?xml version="1.0" encoding="utf-8"?><analyze><text>{}</text><target>relations</target><conll>true</conll></analyze>"""

NON_DOUBLE_BRS_RE = re.compile(r'(?:(?<!br>)<br>(?!<br)|(?<!br/>)<br/>(?!<br)|(?<!br />)<br />(?!<br))')
MULTIPLE_PUNCTS_RE = re.compile(r'[\.\-_]{4,}')
SOFT_HYPHEN_RE = re.compile(r'\u00ad')


hr_stoplist = justext.get_stoplist('Croatian')
tokenizer = generate_tokenizer()


def setup_annotator(config, logger, verbose_output=False):
    def fetch(session, url, text):
        data = XML_ENVELOPE.format(escape(text))
        try:
            response = session.post(url, data=data.encode('utf-8'), timeout=90)
            return response.content
        except requests.exceptions.RequestException:
            logger.error("Timed out. Sent: {}".format(text))
            raise PipelineException()

    def extract_paragraphs(content, is_html):
        if is_html:
            return [
                p.text for p in justext.justext(content.encode('utf-8'), hr_stoplist, encoding='utf-8') if not p.dom_path.endswith('pre')
            ]

        return [p.strip() for p in content.split('\n')]

    def generate_chunks(text):
        chunks = sentence_split(tokenize(tokenizer, text))
        for chunk in chunks:
            if verbose_output:
                logger.info("Sentence: %s" % chunk)
            if len(chunk) > 65:
                # disregard such long sentences
                if verbose_output:
                    logger.warning("Sentence too long. Length: %d. Skipping." % len(chunk))
                continue
            out = ''.join([w[0] for w in chunk]).strip()
            if not out:
                continue
            yield out

    with open(os.path.join(module_dir, config["IATE_DATA_FILE"]), 'r') as f:
        iate_data = json.loads(f.read())

    with open(os.path.join(module_dir, config["EUROVOC_DATA_FILE"]), 'r') as f:
        eurovoc_data = json.loads(f.read())

    iate_term_lengths = [int(l) for l in iate_data.keys()]
    eurovoc_term_lengths = [int(l) for l in eurovoc_data.keys()]

    term_lengths = list(reversed(sorted(set(iate_term_lengths+eurovoc_term_lengths))))

    def add_vocabulary(sentence):
        sentence_iate = {}
        sentence_eurovoc = {}
        for n in term_lengths:
            for i, n_gram in enumerate(split_in_n_grams(n, sentence.tokens)):
                if i not in sentence_iate:
                    sentence_iate[i] = []
                if i not in sentence_eurovoc:
                    sentence_eurovoc[i] = []
                iate_record = iate_data.get(str(n), {}).get(make_term_hash(n_gram))
                if iate_record:
                    sentence_iate[i].append((iate_record[0], n))
                eurovoc_record = eurovoc_data.get(str(n), {}).get(make_term_hash(n_gram))
                if eurovoc_record:
                    sentence_eurovoc[i].append((eurovoc_record[0], n))

        iate_sentence_index = 1
        for i, iate_occurrences in sorted(sentence_iate.items(), key=itemgetter(0)):
            for iate_id, length in sorted(iate_occurrences, key=itemgetter(1), reverse=True):
                for j in range(i, i+length):
                    iate_content = sentence[j].iate
                    if iate_content == '_':
                        iate_content = ''
                    else:
                        iate_content += ';'
                    overrides = {'iate': iate_content}
                    if j == i:
                        overrides['iate'] = overrides['iate'] + '{}:{}'.format(iate_sentence_index, iate_id)
                    else:
                        overrides['iate'] = overrides['iate'] + str(iate_sentence_index)
                    sentence.update_token(j, MarcellToken.create_from_marcell(sentence[j], **overrides))
                iate_sentence_index += 1

        eurovoc_sentence_index = 1
        for i, eurovoc_occurrences in sorted(sentence_eurovoc.items(), key=itemgetter(0)):
            for eurovoc_id, length in sorted(eurovoc_occurrences, key=itemgetter(1), reverse=True):
                for j in range(i, i+length):
                    eurovoc_content = sentence[j].eurovoc
                    if eurovoc_content == '_':
                        eurovoc_content = ''
                    else:
                        eurovoc_content += ';'
                    overrides = {'eurovoc': eurovoc_content}
                    if j == i:
                        overrides['eurovoc'] = overrides['eurovoc'] + '{}:{}'.format(eurovoc_sentence_index, eurovoc_id)
                    else:
                        overrides['eurovoc'] = overrides['eurovoc'] + str(eurovoc_sentence_index)
                    sentence.update_token(j, MarcellToken.create_from_marcell(sentence[j], **overrides))
                eurovoc_sentence_index += 1

    def annotate(content, is_html):
        document = Document()

        with requests.Session() as session:
            session.headers.update({"Connection": "close", "Content-Type": "application/xml; charset=utf-8"})

            content = NON_DOUBLE_BRS_RE.sub('। ', content)
            content = MULTIPLE_PUNCTS_RE.sub('', content)
            content = SOFT_HYPHEN_RE.sub('', content)

            for read_paragraph in extract_paragraphs(content, is_html):
                paragraph = None

                if verbose_output:
                    logger.info("Paragraph text: %s" % read_paragraph)

                chunks = generate_chunks(read_paragraph)

                for chunk in chunks:
                    pipeline_response = fetch(session, config["PIPELINE_URL"], chunk)
                    if verbose_output:
                        logger.info("Pipeline response: %s" % pipeline_response.decode('utf-8'))
                    soup = BeautifulSoup(pipeline_response.decode('utf-8'), 'xml')

                    sentences = soup.item.sentences.find_all('sentence')
                    conll = unescape(soup.item.conll.string)

                    conll_sentences = [c_s.strip().split('\n') for c_s in conll.strip().split('\n\n')]

                    if len(list(sentences)) != len(conll_sentences):
                        raise DataMismatchException()

                    for soup_sentence, conll_sentence in zip(sentences, conll_sentences):
                        if paragraph is None:
                            # create paragraph only if it contains sentences
                            paragraph = Paragraph(document)
                        sentence = Sentence(paragraph, soup_sentence.find('text').string)

                        tokens = soup_sentence.find('tokens').find_all('token')

                        previous_soup_token = previous_conll_token = None
                        for w, (soup_token, conll_token) in enumerate(zip(tokens, conll_sentence)):
                            if w == 0:
                                # looking ahead because SpaceAfter can only be detected on the following token
                                previous_soup_token = soup_token
                                previous_conll_token = conll_token
                                continue

                            xlike_token = XLikeToken(*[f.strip() for f in previous_conll_token.split('\t')])
                            overrides = {}
                            try:
                                if soup_token['start'] == previous_soup_token['end']:
                                    overrides['misc'] = 'SpaceAfter=No'
                            except:
                                pass
                            marcell_token = MarcellToken.create_from_xlike(xlike_token, **overrides)
                            if verbose_output:
                                logger.info("Marcell token: {}".format(marcell_token))
                                logger.info(marcell_token.to_line())
                            previous_soup_token = soup_token
                            previous_conll_token = conll_token
                            sentence.add_token(marcell_token)

                        if previous_conll_token:
                            # last iteration of the loop
                            xlike_token = XLikeToken(*[f.strip() for f in previous_conll_token.split('\t')])
                            marcell_token = MarcellToken.create_from_xlike(xlike_token)
                            if verbose_output:
                                logger.info("Marcell token: {}".format(marcell_token))
                                logger.info(marcell_token.to_line())
                            sentence.add_token(marcell_token)

                    add_vocabulary(sentence)
                    time.sleep(config["PIPELINE_THROTTLE_TIME"])
        return document
    return annotate


def format_output(document, metadata, stream):
    return document.format_output(metadata, stream)
