package xlike_hr.test;

import java.io.StringWriter;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;


import xlike_hr.model.Conll;
import xlike_hr.model.xml.Item;
import xlike_hr.processor.ProcessorPipeline;
import xlike_hr.util.ProcessingLevel;



public class PipelineTest {
	
	
	
	public static void main(String[] args) throws JAXBException
	{
		String text = "No, Todorićevo carstvo završilo bi u debelom minusu, da ne dobiva najveće potpore u državi.Kako je Index već ranije pisao, tvrtke iz koncerna Agrokor su od države dobile više od 100 milijuna kuna, a listu najvećih korisnika državnih poticaja i u 2013. predvodi Todorićevo Belje.";
		ProcessorPipeline pipeline = new ProcessorPipeline();
		
		Conll conll = pipeline.getTokenConll(text);
		
		pipeline.configure(ProcessingLevel.TOKENS, ProcessingLevel.RELATIONS);
		
		pipeline.process(conll);
		
		Item response = conll.toXML(false);
		
		System.out.println("***" + conll.toFormattedString() + "***");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(response.getClass());
		
		StringWriter writer = new StringWriter();
		
		jaxbContext.createMarshaller().marshal(response, writer);
		String xmlString = writer.toString();		
		
		System.out.println(xmlString);
	}

}
