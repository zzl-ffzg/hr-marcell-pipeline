package xlike_hr.service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
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
import xlike_hr.processor.ProcessorPipeline;
import xlike_hr.service.params.Analyze;
import xlike_hr.util.EncodingConverter;
import xlike_hr.util.ProcessingLevel;

/**
 * Analyze Service entry point
 * 
 * 
 */
@Path("/analyze")
public class AnalyzeService {

	private static Logger log = LogManager.getLogger(AnalyzeService.class);

	/**
	 * Analyze operation GET request handler.
	 * 
	 * @param text
	 *            free text or conll depending on input
	 * @param input
	 *            text, tokens, lemmas, entities
	 * @param target
	 *            tokens, lemmas, entities
	 * @param conll
	 *            boolean output conll or xml
	 * @return analysis result in xml format
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response analyze_get(@QueryParam("text") String text,
			@DefaultValue("text") @QueryParam("input") String input,
			@DefaultValue("entities") @QueryParam("target") String target,
			@DefaultValue("false") @QueryParam("conll") boolean conll) {
		log.debug("Request received");
		log.debug("Input: " + input);
		log.debug("Output conll: " + conll);
		log.debug("Target: " + target);
		String conllStr = String.valueOf(conll);
		
		try {
			text = EncodingConverter.decode(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage());
		}
		return analyze(text, input, target, conllStr);
	}

	/**
	 * Analyze operation POST request handler
	 * 
	 * @param params
	 *            service params in xml format
	 * @return analysis result in xml format
	 */
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response analyze_post(Analyze params)
	{
		log.debug("Request received");
		log.debug("Input: " + params.getInput());
		log.debug("Output conll: " + params.getConll());
		log.debug("Target: " + params.getTarget());
		log.debug("Default encoding: " + Charset.defaultCharset().displayName());
		// convert text param from utf-8 to default encoding
		//String text = null;
		String text = params.getText();
/*		try {
			text = EncodingConverter.decode(params.getText(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage());
		}*/

		return analyze(text, params.getInput(), params.getTarget(), params.getConll());
	}

	private Response analyze(String text, String input, String target, String outputConll)
	{
		if (text.isEmpty() || text == null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid text parameter").build();
		
		boolean conllOut;
		
		if ("true".equals(outputConll)) 
		{
			conllOut = true;
		} 
		else if ("false".equals(outputConll)) 
		{
			conllOut = false;
		} 
		else 
		{
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("Invalid conll parameter").build();
		}
		ProcessingLevel inputLevel = ProcessingLevel.convert(input);
		
		ProcessingLevel targetLevel = ProcessingLevel.convert(target);
		
		if (inputLevel == ProcessingLevel.UNKNOWN) 
		{
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("Invalid input parameter").build();
		}
		if (targetLevel == ProcessingLevel.UNKNOWN) 
		{
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("Invalid target parameter").build();
		}
		try 
		{
			/*
			text = text.replace(" .", ".");
			text = text.replace(".", ". ");
			text = text.replace("\r", "");
			text = text.replace("\t", "");
			text = text.replace("\n\n", "");
			text = text.replace("  ", " ");
			text = text.replace("  ", " ");
			text = text.replace("&", "&amp;");
			text = text.replace("<", "&lt;");
			text = text.replace("\"", "&quot;");
			text = text.replace("'", "&apos;");
			text = text.replace(">", "&gt;");
			*/
			Conll conll = null;
			ProcessorPipeline pipeline = new ProcessorPipeline();
			if (inputLevel == ProcessingLevel.TEXT) 
			{
				conll = pipeline.getTokenConll(text);
				inputLevel = ProcessingLevel.TOKENS;
			} 
			else 
			{
				conll = new Conll();
				conll.parseConllFile(text);
			}
			pipeline.configure(inputLevel, targetLevel);
			pipeline.process(conll);
			
			Item responseXml = conll.toXML(conllOut);

			log.debug("Processing complete. Sending response.");
			return Response.ok(responseXml, MediaType.APPLICATION_XML).build();
		}
		catch (Exception e) 
		{
			log.error(e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		}
	}
}
