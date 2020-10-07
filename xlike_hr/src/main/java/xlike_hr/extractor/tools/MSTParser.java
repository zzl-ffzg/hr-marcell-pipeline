package xlike_hr.extractor.tools;

import org.apache.logging.log4j.Logger;
import xlike_hr.util.FileUtil;
import xlike_hr.model.Conll;
import xlike_hr.model.ConllSentence;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import mstparser.DependencyParser;
import mstparser.DependencyPipe;
import mstparser.DependencyPipe2O;
import mstparser.ParserOptions;

import org.apache.logging.log4j.LogManager;

public class MSTParser {
	private static Logger log = LogManager.getLogger(MSTParser.class);
	private static final MSTParser instance = new MSTParser();
	private String mstModel = "";
	private DependencyParser dp;
	private File testFile;
	private File outputFile; 
	
	private MSTParser() {
		try {
			testFile = FileUtil.createTempFile();
			outputFile = FileUtil.createTempFile();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Properties properties = new Properties();
		try {
			properties.load(getClass().getClassLoader().getResourceAsStream("xlike_hr.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mstModel = properties.getProperty("mst.model");
		log.debug("MST Parser model file " + mstModel);
		init();
	}
	private void init(){
		String testFilePath = testFile.getPath();
		String ouptputFilePath = outputFile.getPath();

		String[] parserOptions = new String[6];
		parserOptions[0] = "test";
		parserOptions[1] = "model-name:" + mstModel; // u bin direktoriju
														// tomcata radi
														// problema
														// sa : i ostalim
														// znakovima u
														// apsolutnom pathu
														// a ne kuzi
														// relatvine
														// pathove??
		parserOptions[2] = "test-file:" + testFilePath;
		parserOptions[3] = "output-file:" + ouptputFilePath;
		// decode-type:non-proj i order:1
		parserOptions[4]= "decode-type:non-proj";
		parserOptions[5]= "order:2"; //"order:1";
		ParserOptions options = new ParserOptions(parserOptions);
		DependencyPipe pipe = null;
		try {
			pipe = options.secondOrder ? new DependencyPipe2O(options)
					: new DependencyPipe(options);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		dp = new DependencyParser(pipe, options);

		log.debug("Loading MST Parser model...");
		try {
			dp.loadModel(options.modelName);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		log.debug("done.");

		pipe.closeAlphabets();
	}
	public static MSTParser getInstance(){
		return instance;
	}
	
	public void getDependencies(Conll conll){
		
		for (int sentenceIndex : conll.getSentences().keySet())
		{
			try 
			{
				ConllSentence conllSentence = conll.getSentences().get(sentenceIndex);
				FileUtil.writeFile(testFile, conllSentence.toString());
				dp.outputParses();

				String fileContent = FileUtil.readFile(outputFile.getPath());
				conllSentence.updateMSTValuesFromFile(fileContent);

				FileUtil.deleteFileContent(testFile);
				FileUtil.deleteFileContent(outputFile);
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
