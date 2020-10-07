package xlike_hr.extractor;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xlike_hr.extractor.tools.NERClassifier;
import xlike_hr.model.Conll;
import xlike_hr.model.ConllSentence;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

public class EntityExtractor {
	private static Logger log = LogManager.getLogger(EntityExtractor.class);
	private NERClassifier nerClassifier = NERClassifier.getInstance();

	//gets ne tags from entire text
	// public void extract(Conll conll) {
	// log.debug("Extracting entities...");
	// List<List<CoreLabel>> out = nerClassifier.getNERCTags(conll.getText());
	//
	// int sentenceIndex = 1;
	// int tokenIndex = 1;
	// for (List<CoreLabel> sentence : out) {
	// ConllSentence conllSentence = conll.getSentences().get(
	// sentenceIndex);
	// for (CoreLabel word : sentence) {
	// conllSentence.updateTokenNERCTags(tokenIndex, word.word(),
	// word.get(AnswerAnnotation.class), word.beginPosition(),
	// word.endPosition());
	// tokenIndex++;
	// }
	// sentenceIndex++;
	// tokenIndex = 1;
	// }
	//
	
	//gets ne tags on sentence level
	/*public void extract(Conll conll) {
		log.debug("Extracting entities...");
		for (int sentenceIndex : conll.getSentences().keySet())
		{
			String sentenceStr = conll.getSentences().get(sentenceIndex).getText();
			List<List<CoreLabel>> out = nerClassifier.getNERCTags(sentenceStr);
			List<CoreLabel> sentence = new ArrayList<CoreLabel>();
			for (List<CoreLabel> sent : out)
			{
				for (CoreLabel word : sent)
				{
					sentence.add(word);
				}
			}
			ConllSentence conllSentence = conll.getSentences().get(sentenceIndex);
			int tokenIndex = 1;
			for (CoreLabel word : sentence)
			{
				conllSentence.updateTokenNERCTags(tokenIndex, word.word(), word.get(AnswerAnnotation.class), word.beginPosition(), word.endPosition());
				tokenIndex++;
			}

		}
		log.debug("Entity extractor conll output:\n" + conll.toString());
	}*/
	
	public void extract(Conll conll)
	{
		log.debug("Extracting entities...");
		for (int sentenceIndex : conll.getSentences().keySet())
		{
			String sentenceStr = conll.getSentences().get(sentenceIndex).getText();
			List<List<CoreLabel>> out = nerClassifier.getNERCTags(sentenceStr);
			List<CoreLabel> sentence = new ArrayList<CoreLabel>();
			for (List<CoreLabel> sent : out)
			{
				for (CoreLabel word : sent)
				{
					sentence.add(word);
				}
			}
			
			ConllSentence conllSentence = conll.getSentences().get(sentenceIndex);
			int tokenIndex = 1;
			int count = 1;
			for (CoreLabel word : sentence)
			{
				String temp;
				
				
				if(word.get(AnswerAnnotation.class).contains("I-") && count == 1)
				{
					temp = word.get(AnswerAnnotation.class).replace("I-", "B-");
					count++;
				}
				else
				{
					if(word.get(AnswerAnnotation.class).contains("I-"))
					{
						temp = word.get(AnswerAnnotation.class);
						count++;
					}
					else
					{
						temp = word.get(AnswerAnnotation.class);
						count = 1;
					}
				}
						
				
				
				conllSentence.updateTokenNERCTags(tokenIndex, word.word(), temp, word.beginPosition(), word.endPosition());
				
				tokenIndex++;
			}

		}
		log.debug("Entity extractor conll output:\n" + conll.toString());
		
		
	}

}
