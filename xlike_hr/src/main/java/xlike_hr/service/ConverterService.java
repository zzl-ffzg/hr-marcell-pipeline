package xlike_hr.service;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xlike_hr.model.Conll;
import xlike_hr.model.xml.Item;
@Path("/conll2xml")
public class ConverterService {
	private static Logger log = LogManager.getLogger(ConverterService.class);
	/**
	 * 
	 * @param conllString
	 * @return
	 */
	@GET
	@Produces({ MediaType.APPLICATION_XML })
	public Response conllToXml_get(@QueryParam("conllString") String conllString) {		
		return conllToXml(conllString);

	}
	@POST
	@Produces({ MediaType.APPLICATION_XML })
	public Response conllToXml_post(@FormParam("conllString") String conllString) {		
		return conllToXml(conllString);

	}
	private Response conllToXml(String conllString) {
		Conll conll = new Conll();
		conll.parseConllFile(conllString);
		Item responseXml = conll.toXML(false);
		log.debug("Processing complete. Sending response.");
		return Response.ok(responseXml, MediaType.APPLICATION_XML).build();
	}
}
