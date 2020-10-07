package xlike_hr.processor;

import xlike_hr.extractor.TokenExtractor;
import xlike_hr.model.Conll;

/**
 * 
 * Processes the tokens
 *
 */
public class TokenizerProcessor {
	private TokenExtractor tokenExtractor;

	public TokenizerProcessor() {
		tokenExtractor = new TokenExtractor();
	}

	public Conll process(String text) {

		return tokenExtractor.extract(text);
	}

}
