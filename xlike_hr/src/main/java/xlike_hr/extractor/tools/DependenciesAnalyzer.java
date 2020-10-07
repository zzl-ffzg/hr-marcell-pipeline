package xlike_hr.extractor.tools;

import org.apache.logging.log4j.LogManager;
import xlike_hr.extractor.tools.MSTParser;
import xlike_hr.model.Conll;
import xlike_hr.processor.IProcessor;

import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

public class DependenciesAnalyzer implements IProcessor 
{
	private static Logger log = LogManager.getLogger(DependenciesAnalyzer.class);
	private String mstModel = "";
	private MSTParser parser = MSTParser.getInstance();
	
	public DependenciesAnalyzer() 
	{
		Properties properties = new Properties();
		
		try 
		{
			properties.load(getClass().getClassLoader().getResourceAsStream("xlike_hr.properties"));
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mstModel = properties.getProperty("mst.model");
		
		log.debug("MST Parser model file " + mstModel);
//		File testFile = null;
//		File outputFile = null;
//
//		try {
//			testFile = FileUtil.createTempFile();
//			outputFile = FileUtil.createTempFile();
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

	}

	public void process(Conll conll) {
		log.debug("Processing MST...");
		parser.getDependencies(conll);
//		String testFilePath = testFile.getPath();
//		String ouptputFilePath = outputFile.getPath();
//
//		String[] parserOptions = new String[4];
//		parserOptions[0] = "test";
//		parserOptions[1] = "model-name:" + mstModel; // u bin direktoriju
//														// tomcata radi
//														// problema
//														// sa : i ostalim
//														// znakovima u
//														// apsolutnom pathu
//														// a ne kuzi
//														// relatvine
//														// pathove??
//		parserOptions[2] = "test-file:" + testFilePath;
//		parserOptions[3] = "output-file:" + ouptputFilePath;
//		ParserOptions options = new ParserOptions(parserOptions);
//		DependencyPipe pipe = null;
//		try {
//			pipe = options.secondOrder ? new DependencyPipe2O(options)
//					: new DependencyPipe(options);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//		DependencyParser dp = new DependencyParser(pipe, options);
//
//		log.debug("Loading MST Parser model...");
//		try {
//			dp.loadModel(options.modelName);
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		log.debug("done.");
//
//		pipe.closeAlphabets();
//		for (int sentenceIndex : conll.getSentences().keySet()) {
//			try {
//				ConllSentence conllSentence = conll.getSentences().get(
//						sentenceIndex);
//				FileUtil.writeFile(testFile, conllSentence.toString());
//				dp.outputParses();
//
//				String fileContent = FileUtil.readFile(ouptputFilePath);
//				conllSentence.updateMSTValuesFromFile(fileContent);
//
//				FileUtil.deleteFileContent(testFile);
//				FileUtil.deleteFileContent(outputFile);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		testFile.delete();
	}

	

}
