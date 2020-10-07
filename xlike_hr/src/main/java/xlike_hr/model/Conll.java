package xlike_hr.model;



import java.io.StringReader;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;






/* TIN
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import javax.xml.parsers.*;
*/
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;










//import xlike.model.xml.Entity;
//import xlike.model.xml.EntityList;


import xlike_hr.model.xml.FrameList;
import xlike_hr.model.xml.Item;
import xlike_hr.model.xml.Mention;
import xlike_hr.model.xml.MentionList;
import xlike_hr.model.xml.MentionToken;
import xlike_hr.model.xml.Node;
import xlike_hr.model.xml.NodeList;
import xlike_hr.model.xml.ObjectFactory;
import xlike_hr.model.xml.Sentence;
import xlike_hr.model.xml.SentenceList;
import xlike_hr.model.xml.Service;
import xlike_hr.model.xml.ServiceList;
import xlike_hr.model.xml.Token;
import xlike_hr.model.xml.TokenList;

import javax.xml.bind.JAXB;
import javax.xml.datatype.*;



public class Conll 
{
	private static final String NULL = "_";
	public static final String TAB = "\t";
	private static final String WHITESPACE = " ";
	public static final String NEWLINE = "\n";
	private static Logger log = LogManager.getLogger(Conll.class);
	private Map<Integer, ConllSentence> conllSentences;
	private String respNodes = null;
	private String respFrames = null;
	//private String nodesEnriched = null; TIN
	//private String framesEnriched = null; TIN
	private int sentenceCount;

	public Conll()
	{
		conllSentences = new LinkedHashMap<Integer, ConllSentence>();
		sentenceCount = 0;
		respNodes = null;
		respFrames = null;
	}

	public void addSentence(ConllSentence sentence) {
		sentenceCount++;
		conllSentences.put(sentenceCount, sentence);
	}

	public Map<Integer, ConllSentence> getSentences() {
		return conllSentences;
	}

	/**
	 * Creates conll object sentences and tokens from CoNLL file
	 * 
	 * @param fileContent
	 *            CoNLL file content
	 */
	public void parseConllFile(String fileContent) {
		conllSentences = new LinkedHashMap<Integer, ConllSentence>(); // reset
																		// the
																		// object
		sentenceCount = 0;
		Scanner scanner = new Scanner(fileContent);

		ConllSentence sentence = new ConllSentence();
		boolean sentenceAdded = false;
		while (scanner.hasNextLine()) {
			
			String line = scanner.nextLine();
			if (!"\r".equals(line) && !"".equals(line) &&!" ".equals(line)) {
				parseTokens(line, sentence);
				sentenceAdded = false;
				
			} else {
				if (!sentence.isEmpty()) {
					//log.debug("Adding CoNLL sentence");
					addSentence(sentence);
					sentence = new ConllSentence();
					sentenceAdded = true;
				
				}
			}

		}
		scanner.close();
		if (!sentenceAdded)
			addSentence(sentence); // adds the last sentence if not added
		//log.debug("File read: " +this.toFormattedString());
	}

	private void parseTokens(String strToken, ConllSentence sentence)
	{
		//log.debug("Parsing CoNLL line: " + strToken);
		StringTokenizer st = new StringTokenizer(strToken);
		int id = Integer.parseInt(st.nextToken());
	
		String head = null;
		String deprel = null;
		String phead = null;
		String pdeprel = null;
		String lemma = null;
		String pos = null;
		String msd = null;
		String ne = null;
		String a1 = null;
		String a2 = null;
		String word = st.nextToken();
		if (st.hasMoreTokens()) 
		{
			lemma = st.nextToken();
			if (st.hasMoreTokens()) 
			{
				pos = st.nextToken();
				if (st.hasMoreTokens()) 
				{
					msd = st.nextToken();
					if (st.hasMoreTokens()) 
					{
						ne = st.nextToken();
													// mandatory
						if (st.hasMoreTokens()) 
						{
							head = st.nextToken();
							if (st.hasMoreTokens()) 
							{
								deprel = st.nextToken();
								if (st.hasMoreTokens()) 
								{
									phead = st.nextToken();
									if (st.hasMoreTokens()) 
									{
										pdeprel = st.nextToken();
										if (st.hasMoreTokens()) //TIN
										{//TIN
											a1 = st.nextToken(); //TIN
											if (st.hasMoreTokens()) //TIN
											{//TIN
												a2 = st.nextToken();//TIN
											}//TIN
										}//TIN
									}
								}
							}
						}
					}
				}
			}
		}
		ConllToken token = new ConllToken(id, word, lemma, pos, msd, ne, head,
				deprel, phead, pdeprel, a1, a2);//TIN
		sentence.addToken(token);
		//log.debug("token parsed id: " + token.getId() + " word: " + token.getWord());
	}

	/**
	 * Extracts free text from Conll
	 * 
	 * @return text
	 */
	public String getText() {
		StringBuilder sb = new StringBuilder();
		for (int sentenceIndex : conllSentences.keySet()) {
			ConllSentence sentence = conllSentences.get(sentenceIndex);
			for (int tokenIndex : sentence.getTokens().keySet()) {
				ConllToken token = sentence.getTokens().get(tokenIndex);
				String word = token.getWord();
				if (sb.length() == 0) {
					sb.append(word);
				} else {
					if (!".".equals(word) && !",".equals(word)
							&& !":".equals(word) && !";".equals(word)) {
						sb.append(WHITESPACE);
					}
					sb.append(word);
				}
			}
		}
		//log.debug("Text fom conll: " +sb.toString());
		return sb.toString();
	}

	public Item toXML(boolean addConll) 
	{
		log.debug("Generating XML");
		
		ObjectFactory xmlFactory = new ObjectFactory();
		
		Item xmlItem = xmlFactory.createItem();
		ServiceList xmlServices = xmlFactory.createServiceList();
		SentenceList xmlSentences = xmlFactory.createSentenceList();
		
		//EntityList xmlEntities = xmlFactory.createEntityList();

		NodeList xmlNodes = xmlFactory.createNodeList();
		int entityId = 1;
		for (int sentenceIndex : conllSentences.keySet()) 
		{
			ConllSentence conllSentence = conllSentences.get(sentenceIndex);
			Sentence xmlSentence = xmlFactory.createSentence();
			xmlSentence.setText(conllSentence.getText());
			xmlSentence.setId(String.valueOf(sentenceIndex));
			TokenList xmlTokens = xmlFactory.createTokenList();
			
			for (int tokenIndex : conllSentence.getTokens().keySet()) 
			{
				ConllToken conllToken = conllSentence.getTokens().get(tokenIndex);
				String tokenId = String.valueOf(sentenceIndex) + "." + String.valueOf(tokenIndex);
				Token xmlToken = xmlFactory.createToken();
				xmlToken.setId(tokenId);
				xmlToken.setContent(conllToken.getWord());
				if (!conllToken.getLemma().equals(NULL))
					xmlToken.setLemma(conllToken.getLemma());
				if (!conllToken.getPos().equals(NULL))
					xmlToken.setPos(conllToken.getPos());
				if (conllToken.getStart() > -1)
					xmlToken.setStart(conllToken.getStart());
				if (conllToken.getEnd() > -1)
					xmlToken.setEnd(conllToken.getEnd());
				
				if(respNodes == null)
				{
					if (!conllToken.getNe().equals(NULL) && !conllToken.getNe().equals("O")) 
					{
						//Entity xmlEntity = xmlFactory.createEntity();
						Node xmlNode = xmlFactory.createNode();
						xmlNode.setId("E" + entityId);
						xmlNode.setType("entity");
						xmlNode.setDisplayName(conllToken.getLemma());
						xmlNode.setClazz(conllToken.getNe());
						MentionList xmlMentionList = xmlFactory.createMentionList();
						Mention xmlMention = xmlFactory.createMention();
						xmlMention.setSentenceId(String.valueOf(sentenceIndex));
						xmlMention.setWords(conllToken.getWord());
						
						MentionToken xmlMentionToken = xmlFactory.createMentionToken();
						xmlMentionToken.setId(tokenId);
						
						boolean found = false;
						for(int i = 0; i < xmlNodes.getNode().size(); i++)
						{
							if(xmlNodes.getNode().get(i).getDisplayName().equals(xmlNode.getDisplayName()))
							{
								xmlMention.setId(xmlNodes.getNode().get(i).getId() + "." + (xmlNodes.getNode().get(i).getMentions().getMention().size() + 1));
								xmlNodes.getNode().get(i).getMentions().getMention().add(xmlMention);
								found = true;
							}
						}
						if(!found)
						{
							xmlMention.setId(xmlNode.getId() + ".1");
							xmlMentionList.getMention().add(xmlMention);
							entityId++;
							xmlNode.setMentions(xmlMentionList);
							xmlNodes.getNode().add(xmlNode);
							//xmlEntities.getEntity().add(xmlEntity);
						}
						xmlMention.getMentionToken().add(xmlMentionToken);
					}
				}
				xmlTokens.getToken().add(xmlToken);
			}

			xmlSentence.setTokens(xmlTokens);
			xmlSentences.getSentence().add(xmlSentence);
		}
		
		if(respFrames != null)
		{
			FrameList flist = JAXB.unmarshal(new StringReader(respFrames), FrameList.class);
			xmlItem.setFrames(flist);
		}
		if(respNodes != null)
		{
			NodeList nlist = JAXB.unmarshal(new StringReader(respNodes), NodeList.class);
			xmlItem.setNodes(nlist);
		}
		else
			xmlItem.setNodes(xmlNodes);
		//xmlItem.setEntities(xmlEntities);
		
		Service xmlService = xmlFactory.createService();
		GregorianCalendar c = new GregorianCalendar();
		c.getTime();
		XMLGregorianCalendar date2 = null;
		try 
		{
			date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} 
		catch (DatatypeConfigurationException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		xmlService.setDate(date2);
		xmlServices.getService().add(xmlService);
		xmlItem.setSentences(xmlSentences);
		xmlItem.setServices(xmlServices);
		if (addConll)
		{
			xmlItem.setConll(toFormattedString());
		}
		return xmlItem;
	}

	/**
	 * Creates properly formatted string representation of CoNLL file
	 * 
	 * @return string representation of conll file
	 */
	public String toFormattedString() {
		log.debug("Generating CoNLL");
		StringBuilder sb = new StringBuilder();
		Integer[] maxLen = getMaxLengths();
		for (int sentenceIndex : conllSentences.keySet()) {
			ConllSentence sentence = conllSentences.get(sentenceIndex);
			for (int tokenIndex : sentence.getTokens().keySet()) {
				ConllToken token = sentence.getTokens().get(tokenIndex);
				sb.append(String.format("%-" + maxLen[0] + "s", token.getId()));
				sb.append(TAB);
				sb.append(String.format("%-" + maxLen[1] + "s", token.getWord()));
				sb.append(TAB);
				sb.append(String.format("%-" + maxLen[2] + "s", token.getLemma()));
				sb.append(TAB);
				sb.append(String.format("%-" + maxLen[3] + "s", token.getPos()));
				sb.append(TAB);
				sb.append(String.format("%-" + maxLen[4] + "s", token.getMsd()));
				sb.append(TAB);
				sb.append(String.format("%-" + maxLen[5] + "s", token.getNe()));
				sb.append(TAB);
				sb.append(String.format("%-" + maxLen[6] + "s", token.getHead()));
				sb.append(TAB);
				sb.append(String.format("%-" + maxLen[7] + "s", token.getDlabel()));
				sb.append(TAB);
				sb.append(String.format("%-" + maxLen[8] + "s", token.getPred()));
				sb.append(TAB);
				sb.append(String.format("%-" + maxLen[9] + "s", token.getProle()));
				sb.append(TAB);
				sb.append(String.format("%-" + maxLen[10] + "s", token.getA1()));//TIN
				sb.append(TAB);
				sb.append(String.format("%-" + maxLen[11] + "s", token.getA2()));//TIN
				sb.append(NEWLINE);
				
			}
			sb.append(NEWLINE);
		}
		return sb.toString();
	}

	public String toString() {
		log.debug("Generating CoNLL");
		StringBuilder sb = new StringBuilder();
		for (int sentenceIndex : conllSentences.keySet()) {
			ConllSentence sentence = conllSentences.get(sentenceIndex);
			for (int tokenIndex : sentence.getTokens().keySet()) {
				ConllToken token = sentence.getTokens().get(tokenIndex);
				sb.append(token.getId());
				sb.append(Conll.TAB);
				sb.append(token.getWord());
				sb.append(Conll.TAB);
				sb.append(token.getLemma());
				sb.append(Conll.TAB);
				sb.append(token.getPos());
				sb.append(Conll.TAB);
				sb.append(token.getMsd());
				sb.append(Conll.TAB);
				sb.append(token.getNe());
				sb.append(Conll.TAB);
				sb.append(token.getHead());
				sb.append(Conll.TAB);
				sb.append(token.getDlabel());
				sb.append(Conll.TAB);
				sb.append(token.getPred());
				sb.append(Conll.TAB);
				sb.append(token.getProle());
				sb.append(Conll.TAB);
				sb.append(token.getA1());//TIN
				sb.append(Conll.TAB);
				sb.append(token.getA2());//TIN
				sb.append(Conll.NEWLINE);
			}
			sb.append(NEWLINE);
		}
		return sb.toString();
	}
	
	public String toStringNoCol(int No) 
	{
		log.debug("Generating CoNLL");
		StringBuilder sb = new StringBuilder();
		for (int sentenceIndex : conllSentences.keySet()) 
		{
			ConllSentence sentence = conllSentences.get(sentenceIndex);
			for (int tokenIndex : sentence.getTokens().keySet()) 
			{
				ConllToken token = sentence.getTokens().get(tokenIndex);
				switch(No)
				{
					case(3):
						//log.debug("case: " + No);
						sb.append(token.getId());
						sb.append(Conll.TAB);
						sb.append(token.getWord());
						sb.append(Conll.TAB);
						sb.append(token.getLemma());
						sb.append(Conll.NEWLINE);
						break;
					case(4):
						//log.debug("case: " + No);
						sb.append(token.getId());
						sb.append(Conll.TAB);
						sb.append(token.getWord());
						sb.append(Conll.TAB);
						sb.append(token.getLemma());
						sb.append(Conll.TAB);
						sb.append(token.getPos());
						sb.append(Conll.NEWLINE);
						break;
					case(5):
						//log.debug("case: " + No);
						sb.append(token.getId());
						sb.append(Conll.TAB);
						sb.append(token.getWord());
						sb.append(Conll.TAB);
						sb.append(token.getLemma());
						sb.append(Conll.TAB);
						sb.append(token.getPos());
						sb.append(Conll.TAB);
						sb.append(token.getMsd());
						sb.append(Conll.NEWLINE);
						break;
					case(6):
						//log.debug("case: " + No);
						sb.append(token.getId());
						sb.append(Conll.TAB);
						sb.append(token.getWord());
						sb.append(Conll.TAB);
						sb.append(token.getLemma());
						sb.append(Conll.TAB);
						sb.append(token.getPos());
						sb.append(Conll.TAB);
						sb.append(token.getMsd());
						sb.append(Conll.TAB);
						sb.append(token.getNe());
						sb.append(Conll.NEWLINE);
						break;
					default:
						//log.debug("default case");
						sb.append(token.getId());
						sb.append(Conll.TAB);
						sb.append(token.getWord());
						sb.append(Conll.TAB);
						sb.append(token.getLemma());
						sb.append(Conll.TAB);
						sb.append(token.getPos());
						sb.append(Conll.TAB);
						sb.append(token.getMsd());
						sb.append(Conll.TAB);
						sb.append(token.getNe());
						sb.append(Conll.TAB);
						sb.append(token.getHead());
						sb.append(Conll.TAB);
						sb.append(token.getDlabel());
						sb.append(Conll.TAB);
						sb.append(token.getPred());
						sb.append(Conll.TAB);
						sb.append(token.getProle());
						sb.append(Conll.NEWLINE);
						break;
				}
			}
			sb.append(NEWLINE);
		}
		return sb.toString();
	}
	
	
	//TIN
	/*
	public void parseXML(String XML, String input) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    InputSource is = new InputSource();
	    is.setCharacterStream(new StringReader(XML));

	    Document doc = db.parse(is);
	    
	    doc.getDocumentElement().normalize();
	
	}
	*/
	private Integer[] getMaxLengths() {

		int maxID = 0, maxWord = 0, maxLemma = 0, maxPos = 0, maxMsd = 0, maxNe = 0, maxHead = 0, maxDlabel = 0, maxPred = 0, maxProle = 0, maxA1 = 0, maxA2 = 0;
		for (int i : conllSentences.keySet()) {
			ConllSentence sentence = conllSentences.get(i);
			for (int j : sentence.getTokens().keySet()) {
				ConllToken token = sentence.getTokens().get(j);
				if (String.valueOf(token.getId()).length() > maxID)
					maxID = String.valueOf(token.getId()).length();
				if (token.getWord().length() > maxWord)
					maxWord = token.getWord().length();
				if (token.getLemma().length() > maxLemma)
					maxLemma = token.getLemma().length();
				if (token.getPos().length() > maxPos)
					maxPos = token.getPos().length();
				if (token.getMsd().length() > maxMsd)
					maxMsd = token.getMsd().length();
				if (token.getNe().length() > maxNe)
					maxNe = token.getNe().length();
				if (token.getHead().length() > maxHead)
					maxHead = token.getHead().length();
				if (token.getDlabel().length() > maxDlabel)
					maxDlabel = token.getDlabel().length();
				if (token.getPred().length() > maxPred)
					maxPred = token.getPred().length();
				if (token.getProle().length() > maxProle)
					maxProle = token.getProle().length();
				if (token.getA1().length() > maxA1)
					maxA1 = token.getA1().length();
				if (token.getA2().length() > maxA2)
					maxA2 = token.getA2().length();
			}
		}

		Integer[] maxLengths = { maxID, maxWord, maxLemma, maxPos, maxMsd,
				maxNe, maxHead, maxDlabel, maxPred, maxProle, maxA1, maxA2 };
		return maxLengths;

	}
	
	public void setNode(String node)
	{
		respNodes = node;
	}
	public void setFrame(String frame)
	{
		respFrames = frame;
	}
	/*
	public String getNodeEnriched()
	{
		return nodesEnriched;
	}
	public String getFrameEnriched()
	{
		return framesEnriched;
	}
	*/
}
