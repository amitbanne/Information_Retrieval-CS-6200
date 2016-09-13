package assignment_8;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;

public class ClusterDocumentRanker {

	private static final String DIRECTORY = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 8/data";
	final static String INDEX_NAME = "ap_dataset";
	final static String DOC_TYPE = "document";
	private static Map<String, String> documentTextMap = new HashMap<>();
	private static Client client;

	public static void main(String[] args) {

		client = Util.getClient();
		getDocumentText();
		System.out.println("*** TEXT RETRIEVAL COMPLETE ***");
		rankDocuments();
		System.out.println("*** PROCESS COMPLETE ***");
	}

	public static void getDocumentText() {

		for (int i = 1; i < 84678; i++) {
			GetResponse response = client.prepareGet(INDEX_NAME, DOC_TYPE, i + "").execute().actionGet();
			String text = response.getSourceAsMap().get("text").toString();
			String docno = response.getSourceAsMap().get("docno").toString();
			documentTextMap.put(docno, text);
		}
	}

	private static void rankDocuments() {

		Set<String> docs = parseAllDocs();
		Map<Integer, Map<String, Double>> topicTerms = parseTopTerms();
		Map<String, Map<Integer, Double>> documentTopicScoreMap = new HashMap<>();
		Map<Integer, Double> topicScore = new LinkedHashMap<>();
		for (String doc : docs) {
			String text = documentTextMap.get(doc);
			List<String> tokens = tokenizeDocumentText(text);
			for (Entry<Integer, Map<String, Double>> tEntry : topicTerms.entrySet()) {
				Double score = 0.0;
				Map<String, Double> relevantTerms = tEntry.getValue();

				for (String token : tokens) {
					if (relevantTerms.containsKey(token)) {
						score += relevantTerms.get(token);

						if (topicScore.containsKey(tEntry.getKey())) {
							Double existingScore = topicScore.get(tEntry.getKey());
							existingScore += relevantTerms.get(token);
							topicScore.put(tEntry.getKey(), existingScore);
						} else {
							topicScore.put(tEntry.getKey(), relevantTerms.get(token));
						}
					}
				}
				if (documentTopicScoreMap.containsKey(doc)) {
					Map<Integer, Double> existingTopics = documentTopicScoreMap.get(doc);
					existingTopics.put(tEntry.getKey(), score);
					documentTopicScoreMap.put(doc, existingTopics);
				} else {
					Map<Integer, Double> newTopics = new HashMap<>();
					newTopics.put(tEntry.getKey(), score);
					documentTopicScoreMap.put(doc, newTopics);
				}
			}
		}
		dumpDocumentTopicDistribution(documentTopicScoreMap);
		System.out.println("*** DISTRIBUTION DUMP COMPLETE ***");
		dumpTopicScores(topicScore);
		System.out.println("*** TOPIC SCORE DUMP COMPLETE ***");
	}

	private static void dumpDocumentTopicDistribution(Map<String, Map<Integer, Double>> documentTopicScoreMap) {
		StringBuffer sb = new StringBuffer();
		String del = "";
		for (Entry<String, Map<Integer, Double>> dEntry : documentTopicScoreMap.entrySet()) {
			/*if(dEntry.getValue().size()==0)
				continue;*/
			
			sb.append(del);
			del = System.lineSeparator();
			sb.append(dEntry.getKey());
			sb.append(",");
			List<Entry<Integer, Double>> topics = new ArrayList<>(dEntry.getValue().entrySet());

			/*Collections.sort(topics, new Comparator<Entry<Integer, Double>>() {

				@Override
				public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {

					if ((o1.getValue() - o2.getValue()) >= 0)
						return -1;
					else
						return 1;
				}
			});
			
*/			String sep = "";
			for (int i = 0; i < topics.size(); i++) {
				/*if(topics.get(i).getValue() <= 0.0)
					continue;*/
				
				sb.append(sep);
				sep = ",";
//				sb.append(topics.get(i).getKey());
//				sb.append("(");
				sb.append(topics.get(i).getValue());
//				sb.append(")");
			}
		}

		String fileName = DIRECTORY + "/cluster/CLUSTER_DOC_TOPIC_DISTRIBUTION.txt";
		Util.writeToFile(fileName, sb.toString());
	}

	
	private static void dumpTopicScores(Map<Integer, Double> topicScoreForQuery) {
		StringBuffer sb = new StringBuffer();
		String del = "";

		List<Entry<Integer, Double>> topics = new ArrayList<>(topicScoreForQuery.entrySet());
		Collections.sort(topics, new Comparator<Entry<Integer, Double>>() {

			@Override
			public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {

				if ((o1.getValue() - o2.getValue()) >= 0.0)
					return -1;
				else
					return 1;
			}
		});

		for (Entry<Integer, Double> tEntry : topics) {
			sb.append(del);
			del = System.lineSeparator();
			sb.append("TOPIC-" + tEntry.getKey());
			sb.append("=");
			sb.append(tEntry.getValue());
		}
		String fileName = DIRECTORY + "/cluster/CLUSTER-TOPIC_SCORES.txt";
		Util.writeToFile(fileName, sb.toString());
	}

	private static List<String> tokenizeDocumentText(String text) {
		Pattern pattern = Pattern.compile("[A-Za-z0-9]+(\\.?[A-Za-z0-9]+)*");
		Matcher matcher = pattern.matcher(text);
		List<String> alltokens = new ArrayList<>();
		while (matcher.find()) {
			String word = matcher.group().toLowerCase();
			alltokens.add(word);
		}
		return alltokens;
	}

	private static Map<Integer, Map<String, Double>> parseTopTerms() {
		String fileName = DIRECTORY + "/cluster/CLUSTER-TOP_TERMS.txt";
		Map<Integer, Map<String, Double>> topicTerms = new LinkedHashMap<>();
		int count = 0;
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(fileName));
			while ((line = reader.readLine()) != null) {
				count++;
				if (count < 3)
					continue;

				String[] tokens = line.split(" ");
				for (int i = 0; i < tokens.length; i++) {
					String term = tokens[i].split(":")[0];
					Double score = Double.parseDouble(tokens[i].split(":")[1]);

					if (topicTerms.containsKey(i + 1)) {
						Map<String, Double> existingTerms = topicTerms.get(i + 1);
						existingTerms.put(term, score);
						topicTerms.put(i + 1, existingTerms);
					} else {
						Map<String, Double> newTerms = new HashMap<>();
						newTerms.put(term, score);
						topicTerms.put(i + 1, newTerms);
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return topicTerms;
	}

	private static Set<String> parseAllDocs() {
		String fileName = DIRECTORY + "/cluster/DOC-LIST.txt";
		Set<String> docs = new LinkedHashSet<>();
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(fileName));
			while ((line = reader.readLine()) != null) {
				docs.add(line.trim());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return docs;
	}

}
