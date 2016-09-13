package assignment_8;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;

public class ClusterDocs {

	private final static String INDEX_NAME = "ap_dataset";
	private final static String DOC_TYPE = "document";
	private static final String STOPWORDFILE = "./stoplist.properties";
	private static Client client;
	private static Set<String> stopWords = new HashSet<>();
	private static Map<String, String> documentTextMap = new HashMap<>();
	private static Map<String, Integer> termIds = new LinkedHashMap<>();
	private static List<ArrayList<Integer>> documentTerms = new ArrayList<>();
	private static final String OUTPUT_DIRECTORY = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 8/data";

	public static void main(String[] args) {
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		initialize();
		getDocumentText();
		System.out.println("****** DOCUMENT TEXT FETCHED ******");
		processDocuments();
		System.out.println("****** CORPUS DATA DUMP COMPLETE ******");
		dumpVocabulary();
		System.out.println("**** VOCAB DUMP COMPLETE ****");
		dumpDocuments();
		System.out.println("**** DOCUMENT DUMP COMPLETE ****");
		System.out.println("**** PROCESS COMPLETE ****");
	}

	private static void dumpDocuments() {
		StringBuffer sb = new StringBuffer();
		String del = "";
		for (String doc : documentTextMap.keySet()) {
				sb.append(del);
				del = System.lineSeparator();
				sb.append(doc);
		}
		String vocabFile = OUTPUT_DIRECTORY + "/cluster/DOC-LIST.txt";
		Util.writeToFile(vocabFile, sb.toString());
		
	}

	private static void dumpVocabulary() {
		StringBuffer sb = new StringBuffer();
		String del = "";
		for (Entry<String, Integer> qEntry : termIds.entrySet()) {
				sb.append(del);
				del = System.lineSeparator();
				sb.append(qEntry.getKey());
				sb.append("=");
				sb.append(qEntry.getValue());
		}
		String vocabFile = OUTPUT_DIRECTORY + "/cluster/VOCAB.txt";
		Util.writeToFile(vocabFile, sb.toString());
	}

	private static void initialize() {
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
		parseDocuments(documentTextMap.keySet());
		dumpTermData();
	}

	private static void dumpTermData() {

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
		String fileName = OUTPUT_DIRECTORY + "/cluster/FEATURE_MATRIX.txt";
		Util.writeToFile(fileName, queryText.toString());
	}

	private static void parseDocuments(Set<String> docsForQuery) {

		int count = 0;
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
			count++;
			if(count%1000 == 0)
				System.out.println("*** "+count+ " docs processed ***");
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
		else if(Pattern.matches("[0-9]+.[0-9]+", term))
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

	
	public static void getDocumentText() {

		for (int i = 1; i < 84678; i++) {
			GetResponse response = client.prepareGet(INDEX_NAME, DOC_TYPE, i + "").execute().actionGet();
			String text = response.getSourceAsMap().get("text").toString();
			String docno = response.getSourceAsMap().get("docno").toString();
			documentTextMap.put(docno, text);
		}
	}

}
