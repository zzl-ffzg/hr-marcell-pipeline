package xlike_hr.processor;

import xlike_hr.extractor.LemmaExtractor;
import xlike_hr.model.Conll;
/**
 * Processes the lemmas
 *
 */
public class LemmatizerProcessor implements IProcessor {
	
	private LemmaExtractor lemmaExtractor;

	public LemmatizerProcessor(){
		lemmaExtractor = new LemmaExtractor();
	}

	@Override
	public void process(Conll conll) {
		lemmaExtractor.extract(conll);
	}

}
