package xlike_hr.test;

import xlike_hr.model.Conll;
import xlike_hr.processor.RelationProcessorMock;

public class MockRelationTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Conll conll = new Conll();
		conll.parseConllFile(ConllTest.conllText);
		RelationProcessorMock relProcessor = new RelationProcessorMock();
		try
		{
			relProcessor.process(conll);
		}
		catch(Exception e)
		{
			System.out.println("ERROR occured: " +e.getMessage());
		}
			
		
	}
}
