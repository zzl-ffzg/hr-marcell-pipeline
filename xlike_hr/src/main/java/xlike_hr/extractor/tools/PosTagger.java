package xlike_hr.extractor.tools;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PosTagger 
{
	private static Logger log = LogManager.getLogger(PosTagger.class);
	private static class BillPugh {
		private static final PosTagger instance = new PosTagger();
	}
	private MaxentTagger tagger = null;

	private PosTagger() 
	{
		Properties properties = new Properties();
		try 
		{
			properties.load(getClass().getClassLoader().getResourceAsStream("xlike_hr.properties"));
			initPosTagger(properties);
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void initPosTagger(Properties properties) 
	{
		String posTaggerModelPath = properties.getProperty("models.path") + properties.getProperty("postagger.model");
		log.debug("Loading pos tagger model from " + posTaggerModelPath);

		try 
		{
			tagger = new MaxentTagger(posTaggerModelPath);
		} 

		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("Error loading POS Tagger");
		}
	}
	
	

	public static PosTagger getInstance() {
		return BillPugh.instance;
	}

	public synchronized List<List<HasWord>> getSentences(String text) {
		return MaxentTagger.tokenizeText(new StringReader(text));
	}

	public synchronized ArrayList<TaggedWord> getPosTags(List<HasWord> sentence) {

		return tagger.tagSentence(sentence);
	}

}
