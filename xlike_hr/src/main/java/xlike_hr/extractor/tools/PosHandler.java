package xlike_hr.extractor.tools;

import org.apache.logging.log4j.LogManager;
import xlike_hr.model.*;

import org.annolab.tt4j.TokenHandler;
import org.apache.logging.log4j.Logger;

public class PosHandler implements TokenHandler<String>{
private static Logger log = LogManager.getLogger(PosHandler.class);
	private ConllSentence conllSentence = null;
	private int tokenCount;
	
	public PosHandler(ConllSentence conllSentence){
		this.conllSentence = conllSentence;
		tokenCount = 1;
	}
	@Override
	public void token(String token, String pos, String lemma) {
		conllSentence.updateTokenPosTag(tokenCount, token, pos, 0, 0);
		tokenCount++;
		
	}

	
}
