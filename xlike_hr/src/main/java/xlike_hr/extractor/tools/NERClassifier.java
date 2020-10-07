package xlike_hr.extractor.tools;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

public class NERClassifier 
{
	private static Logger log = LogManager.getLogger(PosTagger.class);
	private static final NERClassifier instance = new NERClassifier();
	private AbstractSequenceClassifier<CoreLabel> classifier = null;
			
	private NERClassifier() 
	{
		Properties properties = new Properties();
		try 
		{
			properties.load(getClass().getClassLoader().getResourceAsStream("xlike_hr.properties"));
			String neClassifierPath = properties.getProperty("models.path") + properties.getProperty("ner.classifier");
			log.debug("Loading classifier " + properties.getProperty("ner.classifier"));
			classifier = CRFClassifier.getClassifierNoExceptions(neClassifierPath);
		} 
		catch (IOException e)
		{
			log.error("Error loading classifier");
			e.printStackTrace();
		}
	}
	/*
	@SuppressWarnings("unchecked")
	private void initNERClassifier(Properties properties)
	{
		String neClassifierPath = properties.getProperty("models.path") + properties.getProperty("ner.classifier");
		log.debug("Loading classifier " + properties.getProperty("ner.classifier"));
		classifier = CRFClassifier.getClassifierNoExceptions(neClassifierPath);
	}
	*/
	public static NERClassifier getInstance() {
		return instance;
	}
	public synchronized List<List<CoreLabel>> getNERCTags(String text)
	{
		return classifier.classify(text);
	}
}
