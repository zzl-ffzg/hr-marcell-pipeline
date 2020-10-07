package xlike_hr.extractor.tools;

import java.io.IOException;
import java.util.Properties;

import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xlike_hr.model.ConllSentence;

public class LemmaTagger {
	private static Logger log = LogManager.getLogger(LemmaTagger.class);
	private static final LemmaTagger instance = new LemmaTagger();
	private TreeTaggerWrapper<String> treeTagger = null;

	private LemmaTagger() {
		Properties properties = new Properties();
		try {
			properties.load(getClass().getClassLoader().getResourceAsStream("xlike_hr.properties"));
			System.setProperty("treetagger.home", properties.getProperty("treetagger.installation"));
			initLemmaTagger(properties);
		} catch (IOException e1) {
			log.error("TreeTagger model loading failed");
			e1.printStackTrace();
		}
	}

	private void initLemmaTagger(Properties properties) {

		treeTagger = new TreeTaggerWrapper<String>();
		// Tree tagger model file is located in the models directory of the
		// TreeTagger installation
		log.debug("Setting tree tagger model " + properties.getProperty("treetagger.model"));
		try {
			treeTagger.setModel(properties.getProperty("treetagger.model"));
		} catch (IOException e) {
			log.error("Error loading TreeTagger model");
			e.printStackTrace();
		}
	}

	public static LemmaTagger getInstance() {
		return instance;
	}

	public synchronized void getLemmas(ConllSentence conllSentence, String[] wordList)
	{
		treeTagger.setHandler(new LemmaHandler(conllSentence));
		try {
			treeTagger.process(wordList);
		} catch (Exception e) {
			log.error("Error processing lemmas");
			e.printStackTrace();
		}
	}
	//TIN
	public synchronized void getPos(ConllSentence conllSentence, String[] wordList)
	{
		log.debug("About to process pos tags");
		treeTagger.setHandler(new PosHandler(conllSentence));
		try {
			treeTagger.process(wordList);
		} catch (Exception e) {
			log.error("Error processing pos tags");
			e.printStackTrace();
		}
		
	}
}
