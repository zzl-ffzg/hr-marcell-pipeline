package xlike_hr.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConllSentence {
	private static final String WHITESPACE = " ";
	private static Logger log = LogManager.getLogger(ConllSentence.class);
	private Map<Integer, ConllToken> conllTokens;
	private int tokenCount;

	public ConllSentence() {
		conllTokens = new LinkedHashMap<Integer, ConllToken>();
		tokenCount = 0;
	}

	public boolean isEmpty(){
		if(conllTokens.size() == 0){
			return true;
		}
		return false;
	}
	public String getText() {
		StringBuilder sb = new StringBuilder();

		for (int tokenIndex : conllTokens.keySet()) {
			ConllToken token = conllTokens.get(tokenIndex);
			String word = token.getWord();
			if (sb.length() == 0) {
				sb.append(word);
			} else {
				if (!".".equals(word) && !",".equals(word) && !":".equals(word)
						&& !";".equals(word)) {
					sb.append(WHITESPACE);
				}
				sb.append(word);
			}
		}
		return sb.toString();
	}

	public String[] getWordList() {
		String[] words = new String[conllTokens.size()];
		int i = 0;
		for (int tokenIndex : conllTokens.keySet()) {
			ConllToken conllToken = conllTokens.get(tokenIndex);
			words[i] = conllToken.getWord();
			i++;
		}
		return words;
	}
	
	public void updateMSTValuesFromFile(String fileContent) {
		StringTokenizer st = new StringTokenizer(fileContent, Conll.NEWLINE);

		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (!"\r".equals(token) && !"".equals(token)) {
				updateToken(token);
			}
		}
	}

	private void updateToken(String conllFileLine) 
	{
		StringTokenizer st = new StringTokenizer(conllFileLine);
		int id = Integer.parseInt(st.nextToken());

		String phead = null;
		String pdeprel = null;
		String word = st.nextToken();
		String lemma = st.nextToken();
		String pos = st.nextToken();
		String msd = st.nextToken();
		String ne = st.nextToken();
		String head = st.nextToken();
		String deprel = st.nextToken();
		if (st.hasMoreTokens()) {
			phead = st.nextToken();
			if (st.hasMoreTokens()) {
				pdeprel = st.nextToken();
			}
		}
		ConllToken conllToken = conllTokens.get(id);
		conllToken.setHead(head);
		conllToken.setDlabel(deprel);
	}

	public void addNewTokenWord(String word) {
		tokenCount++;
		ConllToken attrs = new ConllToken(tokenCount, word);
		conllTokens.put(tokenCount, attrs);
	}
	public void updateTokenWord(int index, String word) {
		ConllToken token = conllTokens.get(index);
		if (token != null) {
			if (token.getId() == index) {
				token.setWord(word);
			} else {
				log.error("Error updating token. Token " + word
						+ " does not exist");
			}
		} else {
			log.debug("Token " + word + "does not exist. Creating new token.");
			ConllToken newToken = new ConllToken(index, word, null, null, null,
					null);
			conllTokens.put(index, newToken);
		}
	}


	public void updateTokenLemma(int tokenIndex, String word, String lemma) {
		ConllToken token = conllTokens.get(tokenIndex);
		if (token != null) {
			if (token.getWord().equals(word)) {
				token.setLemma(lemma);
			} else {
				log.error("Error updating token. Token " + word
						+ " does not exist");
			}
		} else {
			log.error("Error updating token. Token " + word + " does not exist ");
		}
	}
	
	//@SuppressWarnings("unused")
	private ConllToken findToken(String word, String type)
	{
		/*
		if(word.equals("-RRB-"))
		{
			word=")";
		}
		else if(word.equals("-LRB-"))
		{
			word="(";
		}*/
		for(int id: conllTokens.keySet())
		{
			ConllToken token = conllTokens.get(id);
			if(token.getWord().contains(word) && type.equals("POS") && !token.getPOStagged())
			{
				token.setPOStagged(true);
				log.debug("Token found for word " + word);
				return token;
			}
			if(token.getWord().contains(word) && type.equals("NE") && !token.getNEtagged())
			{
				token.setNEtagged(true);
				log.debug("Token found for word " + word);
				return token;
			}
		}
		log.debug("Token "+ word +" not found in sentence:" +this.getText());
		return null;
	}
	
	public void updateTokenPosTag(int tokenIndex, String word, String pos, int start, int end) 
	{
		//log.debug("Updating POS Tag - token_in: " + tokenIndex + " word: " + word + " pos: " + pos + " start: " + start + " end: " + end);
		ConllToken token = findToken(word, "POS");//conllTokens.get(tokenIndex); // //  (TIN)
		if (token != null) 
		{
			/*
			if(word.equals("-RRB-"))
			{
				word=")";
			}
			else if(word.equals("-LRB-"))
			{
				word="(";
			}
			*/
			if (token.getWord().contains(word))
			{
				token.setPos(pos);
				token.setMsd(pos);
				token.setStart(start);
				token.setEnd(end);
			} 
			else 
			{
				log.error("Error updating token. Token " + word + " does not exist" + tokenIndex);
			}
		} 
		else 
		{
			log.error("Error updating token. Token " + word + " does not exist token== null");
		}
	}
	
	public void updateTokenNERCTags(int tokenIndex, String word, String namedEntity, int start, int end) {
		ConllToken token = findToken(word, "NE");//conllTokens.get(tokenIndex); //findToken(word); (TIN)
		if (token != null) 
		{
			/*
			if(word.equals("-RRB-"))
			{
				word=")";
			}
			else if(word.equals("-LRB-"))
			{
				word="(";
			}
			*/
			if (token.getWord().contains(word))
			{
				token.setNe(namedEntity);
				token.setStart(start);
				token.setEnd(end);
			}
			else
			{
				log.error("Error updating token. Token " + word + " does not exist");
			}
		}
		else
		{
			log.error("Error updating token. Token " + word + " does not exist");
		}
	}

	public String toString() {// nema poravnanja jer to sredi mst parser
		log.debug("Generating CoNLL sentence string");
		StringBuilder sb = new StringBuilder();
		for (int tokenIndex : conllTokens.keySet()) {
			ConllToken token = conllTokens.get(tokenIndex);
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
			sb.append("_");
			sb.append(Conll.TAB);
			sb.append("0");
			sb.append(Conll.TAB);
			sb.append(token.getDlabel());
			sb.append(Conll.TAB);
			sb.append(token.getPred());
			sb.append(Conll.TAB);
			sb.append(token.getProle());
			sb.append(Conll.NEWLINE);
		}
		return sb.toString();
	}

	public void addToken(ConllToken token) {
		tokenCount++;
		conllTokens.put(token.getId(), token);
	}

	public Map<Integer, ConllToken> getTokens() {
		return conllTokens;
	}
}
