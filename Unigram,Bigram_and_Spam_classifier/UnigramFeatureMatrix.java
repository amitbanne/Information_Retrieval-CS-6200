package assignment_7;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

public class UnigramFeatureMatrix {

	private static final String SPAM_INFO = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/trec07_spam/trec07p/full/index";
	private static final String FEATURE_MATRIX_TRAIN = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/Feature_Matrix_Train_Unigram.txt";
	private static final String FEATURE_MATRIX_TEST = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/Feature_Matrix_Test_Unigram.txt";
	private static final String UNIGRAM_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/Unigram.txt";
	private static Client client;
	private final static String INDEX_NAME = "spam_classifier";
	private final static String DOC_TYPE = "document";
	private static Map<String, Integer> unigrams;
	private static Map<String, List<TermUnit>> featureMatrixTrain = new LinkedHashMap<>();
	private static Map<String, List<TermUnit>> featureMatrixTest = new LinkedHashMap<>();
	private static Map<String, String> emailSpamMap = new HashMap<>();

	private static String documentFile = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/Unigram_Documents.txt";
	
	public static void main(String[] args) {
		client = Util.transportClientBuilder();
		deserializeSpamInfo();
		System.out.println("*** SPAM MAP DESERIALIZED ***");
		computeUnigrams();
		System.out.println("***** UNIGRAM SIZE= " + unigrams.size() + " *********");
		System.out.println("*** FEATURE MATRIX COMPUTED ***");
		dumpUnigrams();
		System.out.println("*** UNIGRAMS DUMPED ***");
		dumpFeatureMatrix(featureMatrixTrain, FEATURE_MATRIX_TRAIN);
		System.out.println("*** FEATURE MATRIX TRAIN DUMPED ***");
		dumpFeatureMatrix(featureMatrixTest, FEATURE_MATRIX_TEST);
		dumpTestDocuments();
		System.out.println("*** FEATURE MATRIX TEST DUMPED ***");
	}

	private static void dumpTestDocuments() {
		StringBuilder dataString = new StringBuilder();
		String delimiter = "";

		for (Entry<String, List<TermUnit>> spamEntry : featureMatrixTest.entrySet()) {
			dataString.append(delimiter);
			delimiter = System.lineSeparator();

			String email = spamEntry.getKey();
			dataString.append(email);
			
		}
		Util.writeToFile(documentFile, dataString.toString());
	}

	private static void dumpUnigrams() {

		StringBuilder dataString = new StringBuilder();
		String delimiter = "";

		for (Entry<String, Integer> uEntry : unigrams.entrySet()) {
			dataString.append(delimiter);
			delimiter = System.lineSeparator();
			dataString.append(uEntry.getKey());
			dataString.append(" ");
			dataString.append(uEntry.getValue());
		}
		Util.writeToFile(UNIGRAM_FILE, dataString.toString());

	}

	private static void dumpFeatureMatrix(Map<String, List<TermUnit>> featureMatrix, String file) {

		StringBuilder dataString = new StringBuilder();
		String delimiter = "";

		for (Entry<String, List<TermUnit>> spamEntry : featureMatrix.entrySet()) {
			dataString.append(delimiter);
			delimiter = System.lineSeparator();

			String email = spamEntry.getKey();
			//dataString.append(email);
			
			dataString.append((emailSpamMap.get(email).equals("spam") ? 1 : 0));
			dataString.append(" ");
			
			List<TermUnit> sortedTerms = new ArrayList<>(spamEntry.getValue());
			Collections.sort(sortedTerms, new Comparator<TermUnit>() {

				@Override
				public int compare(TermUnit o1, TermUnit o2) {
					if(unigrams.get(o1.getTerm()) <= unigrams.get(o2.getTerm()))
						return -1;
					else
						return 1;
				}
			});
			String sep="";
			for (TermUnit fUnit : sortedTerms) {
				Integer termId = unigrams.get(fUnit.getTerm());
				String tf = fUnit.getTf().toString();
				dataString.append(sep);
				sep=" ";
				dataString.append(termId);
				dataString.append(":");
				dataString.append(tf);
			}
			
		}
		Util.writeToFile(file, dataString.toString());

	}

	private static void deserializeSpamInfo() {

		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(SPAM_INFO));
			while ((line = reader.readLine()) != null) {
				String tokens[] = line.split(" ");
				String spamStatus = tokens[0].trim();
				String emailId = tokens[1].split("/")[2].trim();

				emailSpamMap.put(emailId, spamStatus);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void computeFeatureMatrix() {
		for (Entry<String, Integer> spamEntry : unigrams.entrySet()) {
			processSpamEntry(spamEntry.getKey(), spamEntry.getValue());
		}
	}

	private static void computeUnigrams() {
		unigrams = new LinkedHashMap<>();
		for (int i = 1; i <= 75419; i++) {
			getIndexedDocument(i + "");
			System.out.println("COMPLETED: "+i);
		}
	}

	public static void getIndexedDocument(String id) {

		GetResponse response = client.prepareGet(INDEX_NAME, DOC_TYPE, id).execute().actionGet();

		if (response.isExists()) {
			String name=response.getSourceAsMap().get("name").toString();
			String type=response.getSourceAsMap().get("type").toString();
			String text=response.getSourceAsMap().get("text").toString();
			extractUnigramsFromText(name, text, type);
		}
	}

	/*private static void extractUnigramsFromText(String docName, String docText) {
		docText = docText.trim();
		docText = docText.replaceAll("\\.{2,}", "");
		docText = docText.replaceAll("\"", "");
		docText = docText.replace("(", "");
		docText = docText.replace(")", "");
		docText = docText.replace("[", "");
		docText = docText.replace("{", "");
		docText = docText.replace("}", "");
		docText = docText.replace("]", "");
		docText = docText.replace(";", "");
		docText = docText.replace("-", " ");
		docText = docText.replace("/", "");
		docText = docText.replace("#", "");
		docText = docText.replace("?", "");
		docText = docText.replace("@", "");
		docText = docText.replace("<", "");
		docText = docText.replace(">", "");
		docText = docText.replace(",", "");
		docText = docText.replace("+", "");
		docText = docText.replace("=", " ");
		docText = docText.replace("$", "");
		docText = docText.replace("%", "");
		docText = docText.replace("_", "");
		docText = docText.replace("-", " ");
		docText = docText.replace("\t", "");
		String tokens[] = docText.split(" ");
		for (String s : tokens) {
			s = cleanToken(s);
			if (!(s.length() == 0)) {
				if (s.split(" ").length != 1) {
					for (String t : s.split(" ")) {
						t = cleanToken(t);
						t = t.replace("\n", "");
						t = t.replace("\t", "");
						t = t.replace(" ", "");
						t = t.replace(".", "");
						t = t.trim().replaceAll(" +", " ");
						if (t.trim().length()!=0 && !unigrams.containsKey(t.trim())) {
							int size = unigrams.size();
							unigrams.put(t.trim(), size + 1);
						}
					}
				} else {
					s = s.replace("\n", "");
					s = s.replace(".", "");
					s = s.replace("\t", "");
					s = s.replace(" ", "");
					s = s.trim().replaceAll(" +", " ");
					if (s.trim().length()!=0 &&!unigrams.containsKey(s.trim())) {
						int size = unigrams.size();
						unigrams.put(s.trim(), size + 1);
					}
				}
			}
		}

	}
*/
	
	private static void extractUnigramsFromText(String docName, String docText, String type) {
		
		Map<String, Integer> docTF = new LinkedHashMap<>();
		docText = docText.trim();
		docText = docText.replaceAll("\\.{2,}", " ");
		docText = docText.replaceAll("\"", "");
		docText = docText.replace("http://", "http ");
		docText = docText.replace("<br>", "");
		docText = docText.replace("<html>", "");
		docText = docText.replace("<body>", "");
		docText = docText.replace("<p>", "");
		docText = docText.replace("(", "");
		docText = docText.replace(")", "");
		docText = docText.replace("[", "");
		docText = docText.replace("{", "");
		docText = docText.replace("}", "");
		docText = docText.replace("]", "");
		docText = docText.replace(";", "");
		docText = docText.replace("-", " ");
		docText = docText.replace(":", " ");
		docText = docText.replace("//", " ");
		docText = docText.replace("/", " ");
		docText = docText.replace("#", "");
		docText = docText.replace("?", "");
		docText = docText.replace("@", " ");
		docText = docText.replace("<", "");
		docText = docText.replace(">", "");
		docText = docText.replace(",", "");
		docText = docText.replace("+", "");
		docText = docText.replace("=", " ");
		//docText = docText.replace("$", "");
		docText = docText.replace("%", "");
		docText = docText.replace("_", " ");
		docText = docText.replace("-", " ");
		docText = docText.replace("\t", "");
		String tokens[] = docText.split(" ");
		for (String s : tokens) {
			s = cleanToken(s);
		
			if(s.length()>20 && s.length()<1 && !Pattern.matches("[0-9]+", s))
				continue;
			
			if (!(s.length() == 0)) {
				if (s.split(" ").length != 1) {
					for (String t : s.split(" ")) {
						t = cleanToken(t);
						t = t.replace("\n", "");
						t = t.replace("\t", "");
						t = t.replace(" ", "");
						t = t.replace(".", "");
						t = t.trim().replaceAll(" +", " ");
						

						if(t.length()>20 && t.length()<1 && !Pattern.matches("[0-9]+", t))
							continue;
						
						if (t.trim().length()!=0 && !unigrams.containsKey(t.trim())) {
							int size = unigrams.size();
							unigrams.put(t.trim(), size + 1);
						}
						
						if(docTF.containsKey(t)){
							docTF.put(t, 1+docTF.get(t));
						}else{
							docTF.put(t, 1);
						}
					}
				} else {
					s = s.replace("\n", "");
					s = s.replace(".", "");
					s = s.replace("\t", "");
					s = s.replace(" ", "");
					s = s.trim().replaceAll(" +", " ");

					if(s.length()>20 && s.length()<1 && !Pattern.matches("[0-9]+", s))
						continue;
					
					if (s.trim().length()!=0 &&!unigrams.containsKey(s.trim())) {
						int size = unigrams.size();
						unigrams.put(s.trim(), size + 1);
					}
					
					if(docTF.containsKey(s)){
						docTF.put(s, 1+docTF.get(s));
					}else{
						docTF.put(s, 1);
					}
				}
			}
		}
		
		for(Entry<String, Integer> tEntry : docTF.entrySet()){
			updateFeatureMatrix(docName, new TermUnit(tEntry.getKey(), tEntry.getValue()), type);
		}

	}

	private static String cleanToken(String s) {
		s = s.trim();
		if (s.length() == 0)
			return s;

		s = s.replace(".", "");
		s = s.replace("\t", "");
		s = s.replaceAll("\"", "");
		s = s.replace("(", "");
		s = s.replace("{", "");
		s = s.replace("}", "");
		s = s.replace(")", "");
		s = s.replace("[", "");
		s = s.replace(" ", "");
		s = s.replace("]", "");
		s = s.replace(";", "");
		s = s.replace("\n", "");
		s = s.replace("-", "");
		s = s.replace("/", "");
		s = s.replace("#", "");
		s = s.replace("?", "");
		s = s.replace(",", "");
		s = s.replace("+", "");
		s = s.replace("=", "");
		s = s.replace("$", "");
		s = s.replace("%", "");
		s = s.replace("_", "");
		s = s.trim().replaceAll(" +", " ");
		return s;
	}

	private static void processSpamEntry(String term, Integer termId) {

		SearchResponse scrollResp = client.prepareSearch(INDEX_NAME).setTypes(DOC_TYPE).setScroll(new TimeValue(60000))
				// 1000 Query hits per shard
				.setQuery(QueryBuilders.matchQuery("text", term)).setSize(5000).setExplain(true).execute().actionGet();

		while (true) {

			for (SearchHit hit : scrollResp.getHits().getHits()) {
				String email = hit.sourceAsMap().get("name").toString();
				String type = hit.sourceAsMap().get("type").toString();
				String tfString = hit.explanation().toHtml();
				Integer tf = (int) Double.parseDouble(tfString.split("termFreq=")[1].split("<br")[0]);
				updateFeatureMatrix(email, new TermUnit(term, tf), type);

			}
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute()
					.actionGet();
			// Break condition: No hits are returned
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}
		System.out.println("Completed: "+termId);

	}

	private static void updateFeatureMatrix(String email, TermUnit termUnit, String type) {

		if (type.equals("TRAIN")) {
			if (featureMatrixTrain.containsKey(email)) {
				List<TermUnit> existingTerms = featureMatrixTrain.get(email);
				existingTerms.add(termUnit);
				featureMatrixTrain.put(email, existingTerms);
			} else {
				List<TermUnit> existingTerms = new ArrayList<>();
				existingTerms.add(termUnit);
				featureMatrixTrain.put(email, existingTerms);
			}
		} else if (type.equals("TEST")) {
			if (featureMatrixTest.containsKey(email)) {
				List<TermUnit> existingTerms = featureMatrixTest.get(email);
				existingTerms.add(termUnit);
				featureMatrixTest.put(email, existingTerms);
			} else {
				List<TermUnit> existingTerms = new ArrayList<>();
				existingTerms.add(termUnit);
				featureMatrixTest.put(email, existingTerms);
			}
		}

	}
}
