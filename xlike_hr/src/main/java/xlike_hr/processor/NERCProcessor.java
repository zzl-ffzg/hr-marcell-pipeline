package xlike_hr.processor;

import xlike_hr.extractor.EntityExtractor;
import xlike_hr.model.Conll;
/**
 * 
 * Processes the entities
 *
 */
public class NERCProcessor implements IProcessor {

	EntityExtractor entityExtractor;

	public NERCProcessor() {
		entityExtractor = new EntityExtractor();
	}

	@Override
	public void process(Conll conll) {		
		entityExtractor.extract(conll);
	}
}
