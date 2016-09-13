package assignment_8;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

public class DocumentGenerator {
	final static String INDEX_NAME = "ap_dataset";
	final static String DOC_TYPE = "document";
	private static final String STOPWORDFILE = "./stoplist.properties";
	static Client client;
	static List<DocumentData> docData;
	static Map<String, ArrayList<DocumentData>> mappedData;
	static Set<String> duplicateDocuments;
	static Properties queryProp;
	private static Set<String> stopWords = new HashSet<>();
	private static Map<Integer, HashMap<String, Integer>> qRelData;
	private static Map<String, String> documentTextMap = new HashMap<>();
	private static Map<Integer, Map<String, Integer>> vocalbularyForQuery = new HashMap<>();
	private static Map<String, Integer> termIds = new LinkedHashMap<>();
	private static List<ArrayList<Integer>> documentTerms;
	private static Map<Integer, Set<String>> queryDocMap = new LinkedHashMap<>();
	private static final String OUTPUT_DIRECTORY = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 8/data";

	public static void main(String[] args) {
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		initialize();
		getDocumentText();
		System.out.println("****** DOCUMENT TEXT FETCHED ******");
		processDocuments();
		System.out.println("****** ALL QUERIES COMPLETE ******");
		dumpVocabulary();
		dumpDocs();
		System.out.println("**** COMPLETE ****");
	}

	private static void dumpDocs() {
		for(Entry<Integer, Set<String>> qEntry: queryDocMap.entrySet()){
			StringBuffer sb = new StringBuffer();
			Integer qId = qEntry.getKey();
			String del = "";
			for(String doc : qEntry.getValue()){
				sb.append(del);
				del=System.lineSeparator();
				sb.append(doc);
			}
			
			String docFile = OUTPUT_DIRECTORY+"/docList/"+qId + "-docs.txt";
			Util.writeToFile(docFile, sb.toString());
		}
	}

	private static void dumpVocabulary() {

		for (Entry<Integer, Map<String, Integer>> qEntry : vocalbularyForQuery.entrySet()) {
			Integer qId = qEntry.getKey();
			StringBuffer sb = new StringBuffer();
			String del = "";
			for (Entry<String, Integer> tEntry : qEntry.getValue().entrySet()) {
				sb.append(del);
				del = System.lineSeparator();
				sb.append(tEntry.getValue());
				sb.append("\t");
				sb.append(tEntry.getKey());
			}
			String vocabFile = OUTPUT_DIRECTORY + "/vocab/" + qId + "-vocab.txt";
			Util.writeToFile(vocabFile, sb.toString());
		}

	}

	private static void initialize() {
		qRelData = QRelParser.parseQRel();
		client = Util.getClient();
		parseStopWords();
	}

	private static void parseStopWords() {

		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(STOPWORDFILE));
			while ((line = reader.readLine()) != null) {
				stopWords.add(line.trim());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void processDocuments() {

		duplicateDocuments = new HashSet<>();
		docData = new ArrayList<>();
		List<String> queries = QueryParser.getQueryList();
		for (String query : queries) {
			termIds = new LinkedHashMap<>();
			mappedData = new HashMap<>();
			documentTerms = new ArrayList<>();
			String temp = query.split(" ")[0];
			String queryId = temp.substring(0, temp.indexOf("."));
			String[] queryTokens = QueryParser.queryTokenizer(query);

			processQuery(queryTokens);

			List<String> topDocuments = BM25_Util.bm25TFUtil(mappedData, queryId);
			Set<String> docsForQuery = new HashSet<>();
			docsForQuery.addAll(topDocuments);
			for (Entry<String, Integer> dEntry : qRelData.get(Integer.parseInt(queryId.trim())).entrySet()) {
				if (dEntry.getValue() == 1)
					docsForQuery.add(dEntry.getKey());
			}
			queryDocMap.put(Integer.parseInt(queryId.trim()), docsForQuery);
			parseDocuments(docsForQuery);
			dumpTermData(queryId);
			Map<String, Integer> termMapCopy = new LinkedHashMap<>();
			termMapCopy.putAll(termIds);
			vocalbularyForQuery.put(Integer.parseInt(queryId.trim()), termMapCopy);
			System.out.println("****** " + queryId + " completed ******");
			
		}

	}

	private static void dumpTermData(String queryId) {

		StringBuffer queryText = new StringBuffer();
		String delimiter = "";
		for (int i = 0; i < documentTerms.size(); i++) {
			queryText.append(delimiter);
			queryText.append("| ");
			delimiter = System.lineSeparator();

			StringBuffer docText = new StringBuffer();
			String separator = "";
			for (Integer termId : documentTerms.get(i)) {
				docText.append(separator);
				separator = " ";
				docText.append(termId);
			}
			queryText.append(docText);
		}
		String fileName = OUTPUT_DIRECTORY + "/feature/" + queryId + ".txt";
		Util.writeToFile(fileName, queryText.toString());
	}

	private static void parseDocuments(Set<String> docsForQuery) {

		for (String doc : docsForQuery) {
			String text = documentTextMap.get(doc);
			if (text.length() == 0 || text.equals(""))
				continue;

			// String[] tokens = text.split("[^(\\w+(\\.?\\w+)*)]");

			Pattern pattern = Pattern.compile("[A-Za-z0-9]+(\\.?[A-Za-z0-9]+)*");
			Matcher matcher = pattern.matcher(text);
			Set<String> alltokens = new HashSet<>();
			while (matcher.find()) {
				String word = matcher.group().toLowerCase();
				alltokens.add(word);
			}
			processTokens(alltokens);
		}
	}

	private static void processTokens(Set<String> tokens) {
		List<Integer> terms = new ArrayList<>();
		for (String term : tokens) {
			if(isSkipWord(term))
				continue;
			
			int id = getTermId(term);
			terms.add(id);
		}
		documentTerms.add((ArrayList<Integer>) terms);
	}

	private static boolean isSkipWord(String term) {
		
		if(stopWords.contains(term))
			return true;
		else if(Pattern.matches("[0-9]+.?[0-9]+", term))
			return true;
		else if(Pattern.matches("\\w[0-9]+", term))
			return true;
		else if(term.length()<3)
			return true;
		
		return false;
			
	}

	private static int getTermId(String term) {

		if (!termIds.containsKey(term.trim())) {
			termIds.put(term.trim(), termIds.size() + 1);
		}
		return termIds.get(term.trim());
	}

	private static String cleanTerm(String term) {
		if (term.contains(".")) {
			if (term.charAt(0) == '.')
				term = term.substring(1, term.length());

			if (term.trim().length() > 0 && term.charAt(term.length() - 1) == '.')
				term = term.substring(0, term.length() - 1);
		}

		if (term.contains("(")) {
			if (term.charAt(0) == '(')
				term = term.substring(1, term.length());

			if (term.trim().length() > 0 && term.charAt(term.length() - 1) == '(')
				term = term.substring(0, term.length() - 1);
		}

		if (term.contains(")")) {
			if (term.charAt(0) == ')')
				term = term.substring(1, term.length());

			if (term.trim().length() > 0 && term.charAt(term.length() - 1) == ')')
				term = term.substring(0, term.length() - 1);
		}
		return term;
	}

	private static void processQuery(String[] tokens) {

		for (String s : tokens) {
			getDocumentDataForTerm(s.toLowerCase());
		}
	}

	private static void getDocumentDataForTerm(String queryTerm) {
		String stemmedTerm = Util.getStemOfWord(queryTerm);
		try {
			getResponseString(stemmedTerm);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void getResponseString(String term) throws UnknownHostException {

		SearchResponse scrollResp = client.prepareSearch(INDEX_NAME).setTypes(DOC_TYPE).setScroll(new TimeValue(60000))
				.setQuery(QueryBuilders.matchQuery("text", term)).setSize(100) // 100
				// Query
				// hits
				// per
				// shard
				.setExplain(true).execute().actionGet();

		// scroll

		// Scroll until no hits are returned
		List<DocumentData> docDataList = new ArrayList<>();
		// duplicateData = new HashSet<>();
		duplicateDocuments = new HashSet<>();
		long docFreq = scrollResp.getHits().getTotalHits();
		while (true) {
			for (SearchHit hit : scrollResp.getHits().getHits()) {
				DocumentData docData = parseDataForDocument(term.trim(), hit, docFreq);

				if (((int) docFreq) == 0)
					System.out.println(docData.getTermFreq());

				if (!duplicateDocuments.contains(docData.getDocNo())) {
					docDataList.add(docData);
					duplicateDocuments.add(docData.getDocNo());
				}
			}
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute()
					.actionGet();
			// Break condition: No hits are returned
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}
		mappedData.put(term, (ArrayList<DocumentData>) docDataList);
	}

	private static DocumentData parseDataForDocument(String term, SearchHit hit, long docFreq) {

		String docNo = hit.getSource().get("docno").toString();
		// System.out.println(docNo);
		long termFreq = parseTermFreq(hit.getExplanation().toHtml());
		DocumentData docData = new DocumentData(docNo, term, termFreq, docFreq);
		return docData;

	}

	private static long parseTermFreq(String explanation) {

		String t = explanation.split("termFreq=")[1];
		long tf = (long) Double.parseDouble(t.substring(0, t.indexOf("<")));
		return tf;
	}

	public static void getDocumentText() {

		for (int i = 1; i < 84678; i++) {
			GetResponse response = client.prepareGet(INDEX_NAME, DOC_TYPE, i + "").execute().actionGet();
			String text = response.getSourceAsMap().get("text").toString();
			String docno = response.getSourceAsMap().get("docno").toString();
			documentTextMap.put(docno, text);
		}
	}

}
