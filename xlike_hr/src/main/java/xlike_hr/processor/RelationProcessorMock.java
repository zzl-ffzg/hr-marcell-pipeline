package xlike_hr.processor;

import java.io.IOException;
import java.util.Properties;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientResponse;
import xlike_hr.model.Conll;

public class RelationProcessorMock implements IProcessor {
	private String serviceUrl = null;
	

	private static Logger log = LogManager.getLogger(RelationProcessorMock.class);

	/**
	 * Mock class for Relation Processor.
	 */
	public RelationProcessorMock() {
		Properties properties = new Properties();
		try 
		{
			properties.load(getClass().getClassLoader().getResourceAsStream("xlike_de.properties"));
			serviceUrl = properties.getProperty("parser.url");
		} 
		catch (IOException e)
		{
			log.error("Error loading parser service url");
			e.printStackTrace();
		}
	}

	@Override
	public void process(Conll conll)
	{
		log.debug("Processing relations...");
		//log.debug(conll.toString());
		
		try 
		{
//import com.sun.jersey.api.client.Client;
//import com.sun.jersey.api.client.ClientResponse;
//import com.sun.jersey.api.client.WebResource;
//import com.sun.jersey.api.client.config.ClientConfig;
//import com.sun.jersey.api.client.config.DefaultClientConfig;
//

			Client client = ClientBuilder.newClient();
			WebTarget service = client.target(UriBuilder.fromUri(serviceUrl).build()).path("myresource/{param}");

			String xmlStr = conll.toStringNoCol(6);
			
			xmlStr = xmlStr.trim();
			xmlStr += "\n\n";
			
			//log.debug("Conll sent to server:\n" + xmlStr);
			
			String requestStr = "<analyze><input>entities</input><target>relations</target><conll>true</conll><srl>true</srl><wsd>true</wsd><text>BLA</text><data>" + xmlStr + "</data></analyze>";
						
			ClientResponse response = service.request().post(Entity.xml(requestStr), ClientResponse.class);
			
			if (response.getStatus() == 200)
			{
				String responseXml = response.readEntity(String.class);
				//log.debug("The mock returned: " + responseXml);
				// remove <conll></conll>
				//responseXml = responseXml.replace("<conll>", "").replace("</conll>", "").replace("<item>", "");
				String respConll = responseXml.replaceAll("(?s).*<conll>", "").replaceAll("(?s)</conll>.*", "");
				log.debug("response recieved...");
				StringBuffer framesBuffer = new StringBuffer();
				StringBuffer nodesBuffer = new StringBuffer();
				framesBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				//framesBuffer.append(responseXml.replaceAll("(?s).*<frames>", "").replaceAll("(?s)</frames>.*", ""));
				framesBuffer.append(responseXml.substring(responseXml.indexOf("<frames>"), responseXml.indexOf("</frames>")));
				framesBuffer.append("</frames>");
				nodesBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				//nodesBuffer.append(responseXml.replaceAll("(?s).*<nodes>", "").replaceAll("(?s)</nodes>.*", ""));
				nodesBuffer.append(responseXml.substring(responseXml.indexOf("<nodes>"), responseXml.indexOf("</nodes>")));
				nodesBuffer.append("</nodes>");
				String respFrames = framesBuffer.toString();
				String respNodes = nodesBuffer.toString();
				//String respFrames = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><frames>" + responseXml.replaceAll("(?s).*<frames>", "").replaceAll("(?s)</frames>.*", "") + "</frames>";
				//String respNodes = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><nodes>" + responseXml.replaceAll("(?s).*<nodes>", "").replaceAll("(?s)</nodes>.*", "") + "</nodes>";
				//log.debug("Frames: " + respFrames);
				//log.debug("Nodes: " + respNodes);
				//log.debug("Edited Response: " + responseXml);
				conll.setFrame(respFrames);
				conll.setNode(respNodes);
				//conll.setFrameEnriched(respFrames); TIN
				//conll.setNodeEnriched(respNodes); TIN
				
				conll.parseConllFile(respConll);
			} 
			else
				log.error("Problems with the relation service");
				
		} 
		catch (Exception e)
		{
			// ignore
			log.error("Relation processing mock service not runnig");
		}
	}
}
