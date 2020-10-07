import hashlib

from collections import namedtuple


def split_in_n_grams(n, tokens):
    if n > len(tokens):
        raise StopIteration
    for i in range(len(tokens)-n-1):
        yield tokens[i:i+n]


def make_term_hash(n_gram):
    return hashlib.md5(' '.join([t.lemma for t in n_gram]).encode()).hexdigest()


class DataMismatchException(Exception):
    pass


class PipelineException(Exception):
    pass


XLikeToken = namedtuple('XLikeToken', ['order', 'form', 'lemma', 'pos', 'msd', 'ne', 'head',
                                       'deprel', 'empty1', 'empty2', 'empty3', 'empty4'])


class MarcellToken(namedtuple('MarcellToken', ['order', 'form', 'lemma', 'upos', 'xpos', 'feats', 'head',
                                               'deprel', 'deps', 'misc', 'ne', 'np', 'iate', 'eurovoc'])):
    def to_line(self):
        return '\t'.join(self)

    @staticmethod
    def create_from_xlike(x, **overrides):
        return MarcellToken(
            order=overrides.get('order', x.order),
            form=overrides.get('form', x.form),
            lemma=overrides.get('lemma', x.lemma),
            upos=overrides.get('upos', '_'),
            xpos=overrides.get('xpos', x.pos),
            feats=overrides.get('feats', '_'),
            head=overrides.get('head', x.head),
            deprel=overrides.get('deprel', x.deprel),
            deps=overrides.get('deps', '_'),
            misc=overrides.get('misc', '_'),
            ne=overrides.get('ne', x.ne),
            np=overrides.get('np', '_'),
            iate=overrides.get('iate', '_'),
            eurovoc=overrides.get('eurovoc', '_')
        )

    @staticmethod
    def create_from_marcell(m, **overrides):
        return MarcellToken(
            order=overrides.get('order', m.order),
            form=overrides.get('form', m.form),
            lemma=overrides.get('lemma', m.lemma),
            upos=overrides.get('upos', m.upos),
            xpos=overrides.get('xpos', m.xpos),
            feats=overrides.get('feats', m.feats),
            head=overrides.get('head', m.head),
            deprel=overrides.get('deprel', m.deprel),
            deps=overrides.get('deps', m.deps),
            misc=overrides.get('misc', m.misc),
            ne=overrides.get('ne', m.ne),
            np=overrides.get('np', m.np),
            iate=overrides.get('iate', m.iate),
            eurovoc=overrides.get('eurovoc', m.eurovoc)
        )


class Sentence():
    def __init__(self, paragraph, text):
        self.text = text
        self._tokens = []
        paragraph.add_sentence(self)

    def __len__(self):
        return len(self._tokens)

    def __getitem__(self, key):
        return self._tokens[key]

    def stringify(self):
        return '\n'.join([t.to_line() for t in self._tokens])

    def add_token(self, token):
        self._tokens.append(token)

    def update_token(self, position, new_token):
        self._tokens[position] = new_token

    @property
    def tokens(self):
        return self._tokens


class Paragraph():
    def __init__(self, document):
        self._sentences = []
        document.add_paragraph(self)

    def stringify(self, doc_id, par_id):
        out = ""
        for i, sentence in enumerate(self._sentences, start=1):
            out += "# sent_id = {}-p{}s{}\n".format(doc_id, par_id, i)
            out += "# text = {}\n".format(sentence.text)
            out += sentence.stringify()
            out += "\n\n"
        return out

    def add_sentence(self, sentence):
        self._sentences.append(sentence)


class Document():
    def __init__(self):
        self._paragraphs = []

    def stringify(self, doc_id):
        out = ""
        for i, paragraph in enumerate(self._paragraphs, start=1):
            out += "# newpar id = {}-p{}\n".format(doc_id, i)
            out += paragraph.stringify(doc_id, i)
        return out

    def add_paragraph(self, paragraph):
        self._paragraphs.append(paragraph)

    def format_output(self, metadata, stream):
        stream.write('# global.columns = ID FORM LEMMA UPOS XPOS FEATS HEAD DEPREL DEPS MISC MARCELL:NE MARCELL:NP MARCELL:IATE MARCELL:EUROVOC\n')
        stream.write('# newdoc id = {}\n'.format(metadata['id']))
        if metadata.get('year', ''):
            stream.write('# date = {}\n'.format(metadata['year']))
        if metadata.get('title', ''):
            stream.write('# title = {}\n'.format(metadata['title']))
        if metadata.get('type', ''):
            stream.write('# type = {}\n'.format(metadata['type']))
            stream.write('# entype = {}\n'.format(metadata['entype']))
        descriptors = [d for d in metadata.get('type', []) if isinstance(d, dict)]
        if descriptors:
            stream.write('# keywords = {}\n'.format('; '.join([d['descriptor'] for d in descriptors])))
        if metadata.get('url', ''):
            stream.write('# url = {}\n'.format(metadata['url']))
        if metadata.get('in_effect_since', ''):
            stream.write('# date_effect = {}\n'.format(metadata['in_effect_since']))

        stream.write('\n')
        stream.write(self.stringify(metadata['id']))
        return stream
