package xlike_hr.extractor.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TokenSplitter {

	private static Logger log = LogManager.getLogger(TokenSplitter.class);

	private static class BillPugh {
		private static final TokenSplitter INSTANCE = new TokenSplitter();
	}

	private TokenizerME tokenizer = null;
	
	
	private TokenSplitter() {
		Properties properties = new Properties();
		try {
			properties.load(getClass().getClassLoader().getResourceAsStream("xlike_hr.properties"));
		} catch (IOException e) {
			log.error("Error loading service properties");
			e.printStackTrace();
		}
		initTokenizer(properties);
	}
	
	
	private void initTokenizer(Properties properties) {
		InputStream tokenizerModelIn= null;
		try {
			String tokenizerModelPath =  properties.getProperty("models.path") + properties.getProperty("tokenizer.model");
			log.debug("Loading tokenizer model from " + tokenizerModelPath);
			tokenizerModelIn = new FileInputStream(tokenizerModelPath);
			TokenizerModel tokenizerModel = new TokenizerModel(tokenizerModelIn);
			tokenizer = new TokenizerME(tokenizerModel);
		} catch (IOException e) {
			log.error("Error loading token model");
			e.printStackTrace();
		}
		if(tokenizerModelIn != null){
			try {
				tokenizerModelIn.close();
			} catch (IOException e) {
				//ignore
			}
		}
	}
 
	public static TokenSplitter getInstance() {
		return BillPugh.INSTANCE;
	}
	
	public synchronized String[] getTokens(String text){
		return tokenizer.tokenize(text);
	}

}
