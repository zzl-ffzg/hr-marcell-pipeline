package xlike_hr.extractor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xlike_hr.extractor.tools.SentenceSplitter;
import xlike_hr.extractor.tools.TokenSplitter;
import xlike_hr.model.Conll;
import xlike_hr.model.ConllSentence;

public class TokenExtractor {

	private static Logger log = LogManager.getLogger(TokenExtractor.class);

	public TokenExtractor() {}

	public Conll extract(String text) {
		log.debug("Extracting tokens...");
		Conll conll = new Conll();
		String[] sentences = SentenceSplitter.getInstance().getSenetnces(text);
		for (int i = 0; i < sentences.length; i++) {
			String sentence = sentences[i];
			ConllSentence conllSentence = new ConllSentence();
			String[] tokens = TokenSplitter.getInstance().getTokens(sentence);
			for (int j = 0; j < tokens.length; j++) {
				conllSentence.addNewTokenWord(tokens[j]);
			}
			conll.addSentence(conllSentence);
		}
		return conll;
	}

}
