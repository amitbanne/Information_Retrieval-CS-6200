package assignment_7;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.json.JSONArray;
import org.json.JSONObject;

public class SpamFeatureMatrix {
	private static final String SPAM_INFO = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/trec07_spam/trec07p/full/index";
	/*private static final String FEATURE_MATRIX_TRAIN = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/Feature_Matrix_Train.txt";
	private static final String FEATURE_MATRIX_TEST = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/Feature_Matrix_Test.txt";
	private final static String SPAM_WORD_LIST = "./SpamList-1";
	*/
	private static final String FEATURE_MATRIX_TRAIN = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/CUSTOM_Feature_Matrix_Train.txt";
	private static final String FEATURE_MATRIX_TEST = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/CUSTOM_Feature_Matrix_Test.txt";
	private final static String SPAM_WORD_LIST = "./SPAM-CUSTOM-List.txt";
	
	private static Client client;
	private final static String INDEX_NAME = "spam_classifier";
	private final static String DOC_TYPE = "document";
	
	private static Map<String, Integer> spamWords;
	private static Map<String, List<TermUnit>> featureMatrixTrain = new HashMap<>();
	private static Map<String, List<TermUnit>> featureMatrixTest = new HashMap<>();
	private static Map<String, String> emailSpamMap = new HashMap<>();
	private static String documentFile = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/CUSTOM_SPAM_LIST_Documents.txt";
	public static void main(String[] args) {
		client = Util.transportClientBuilder();
		deserializeSpamInfo();
		System.out.println("*** SPAM MAP DESERIALIZED ***");
		deserializeSpamWords(SPAM_WORD_LIST);
		//dumpTest();
		System.out.println("SPAM WORDS COUNT: "+spamWords.size());
		
		System.out.println("*** SPAM WORD LIST DESERIALIZED ***");
		computeFeatureMatrix();
		System.out.println("*** FEATURE MATRIX COMPUTED ***");
		dumpFeatureMatrix(featureMatrixTrain, FEATURE_MATRIX_TRAIN);
		System.out.println("*** FEATURE MATRIX TRAIN DUMPED ***");
		dumpFeatureMatrix(featureMatrixTest, FEATURE_MATRIX_TEST);
		dumpTestDocuments();
		System.out.println("*** FEATURE MATRIX TEST DUMPED ***");
		
	}

	
	private static void dumpTest() {

		StringBuilder dataString = new StringBuilder();
		String delimiter = "";

		for (Entry<String, Integer> spamEntry : spamWords.entrySet()) {
			dataString.append(delimiter);
			delimiter = System.lineSeparator();

			String email = spamEntry.getKey();
			dataString.append(email);
			
		}
		Util.writeToFile("./SPAM_SET.txt", dataString.toString());

		
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

	
	private static void dumpFeatureMatrix(Map<String, List<TermUnit>> featureMatrix, String file) {

		StringBuilder dataString = new StringBuilder();
		String delimiter = "";

		for (Entry<String, List<TermUnit>> spamEntry : featureMatrix.entrySet()) {
			dataString.append(delimiter);
			delimiter = System.lineSeparator();

			String email = spamEntry.getKey();
			//dataString.append(email);
			
			dataString.append((emailSpamMap.get(email).equals("spam")?1:0));
			for (TermUnit fUnit : spamEntry.getValue()) {
				Integer termId = spamWords.get(fUnit.getTerm());
				String tf = fUnit.getTf().toString();
				dataString.append(" ");
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
		for (Entry<String, Integer> spamEntry : spamWords.entrySet()) {
			processSpamEntry(spamEntry.getKey(), spamEntry.getValue());
			
			/*TermVectorsResponse resp = client.prepareTermVectors().setIndex(INDEX_NAME)
                    .setType(DOC_TYPE).setId("1").setTermStatistics(true)
                    .execute().actionGet();
		    try {
		    	XContentBuilder builder = XContentFactory.jsonBuilder();
		    	builder.startObject();
		    	resp.toXContent(builder, ToXContent.EMPTY_PARAMS);
		    	builder.endObject();
		    	
		    	JSONObject obj = new JSONObject(builder.string());
		    	String pageName = obj.getJSONObject("term_vectors").getJSONObject("text").getJSONObject("terms").toString();

		    	JSONArray arr = obj.getJSONObject("term_vectors").getJSONObject("text").getJSONObject("terms").names();
		    	for (int i = 0; i < arr.length(); i++)
		    	{
		    	    String post_id = arr.getJSONObject(i).toString();
		    	    System.out.println(post_id);
		    	    System.exit(0);
		    	}
		    	
		    	System.out.println(pageName);
*/				
			
			
		}
	}

	private static void deserializeSpamWords(String spamWordList) {
		spamWords = new LinkedHashMap<>();
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(spamWordList));
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() == 0)
					continue;
				
					line = line.trim().replaceAll(" +", " ");
					if(!spamWords.containsKey(line.trim())){
						spamWords.put(line.trim(), spamWords.size()+1);
					}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void processSpamEntry(String term, Integer termId) {

		SearchResponse scrollResp = client.prepareSearch(INDEX_NAME).setTypes(DOC_TYPE).setScroll(new TimeValue(60000))
				// 1000 Query hits per shard
				.setQuery(QueryBuilders.matchQuery("text", term)).setSize(1000)
				.setExplain(true).execute().actionGet();

		while (true) {

			for (SearchHit hit : scrollResp.getHits().getHits()) {
				String email = hit.sourceAsMap().get("name").toString();
				String type = hit.sourceAsMap().get("type").toString();
				String tfString = hit.explanation().toHtml();
				Integer tf =  (int) Double.parseDouble(tfString.split("termFreq=")[1].split("<br")[0]);
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

		if(type.equals("TRAIN")){
			if (featureMatrixTrain.containsKey(email)) {
				List<TermUnit> existingTerms = featureMatrixTrain.get(email);
				existingTerms.add(termUnit);
				featureMatrixTrain.put(email, existingTerms);
			} else {
				List<TermUnit> existingTerms = new ArrayList<>();
				existingTerms.add(termUnit);
				featureMatrixTrain.put(email, existingTerms);
			}
		}else if(type.equals("TEST")){
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
