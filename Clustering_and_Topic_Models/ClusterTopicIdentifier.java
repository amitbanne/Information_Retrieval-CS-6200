package assignment_8;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ClusterTopicIdentifier {
	private static final String DIRECTORY = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 8/data";
	private static String MODEL_FILE = DIRECTORY + "/cluster/Cluster-model.txt";
	private static String VOCAB_FILE = DIRECTORY + "/cluster/VOCAB.txt";
	private static String TOP_TERMS_FILE = DIRECTORY + "/cluster/CLUSTER-TOP_TERMS.txt";
	private static Map<Integer, String> idTermMap = new LinkedHashMap<>();
	private static Map<Integer, List<Double>> topicTermMap = new LinkedHashMap();
	private static Map<Integer, List<TopicEntry>> topTermsForTopic = new LinkedHashMap<>();
	private static final Integer NO_OF_TOPICS = 200;
	private static final Integer NO_OF_TOP_WORDS = 30;
	
	
	public static void main(String[] args) {
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		parseVocabFile();
		System.out.println("*** VOCAB FILE PARSED  ***");
		parseModelFile();
		System.out.println("*** MODEL FILE PARSED  ***");
		for(int k=1;k<=NO_OF_TOPICS;k++)
			identifyTopTermsForTopic(k);
		System.out.println("*** TOP WORDS/TOPIC IDENTIFIED  ***");
		dumpTopTerms();
		System.out.println("*** TOP WORDS DUMP COMPLETE ***");
		
	}
	
	private static void dumpTopTerms() {

		StringBuffer sb = new StringBuffer();
		sb.append(System.lineSeparator());
		String del = "";
		
		for(int i=0;i<NO_OF_TOPICS;i++)
			sb.append("TOPIC-"+(i+1)+"\t");
		
		sb.append(System.lineSeparator());
		
		for(int i=0;i<NO_OF_TOP_WORDS;i++){
			del="";
			for(Integer t: topTermsForTopic.keySet()){
				sb.append(del);
				del=" ";
				sb.append(idTermMap.get(topTermsForTopic.get(t).get(i).getTerm()));
				sb.append(":");
				sb.append(topTermsForTopic.get(t).get(i).getScore());
			}
			sb.append(System.lineSeparator());
		}
		Util.writeToFile(TOP_TERMS_FILE, sb.toString());
		
	}

	private static void identifyTopTermsForTopic(int k) {

		List<TopicEntry> termScore = new ArrayList<>();
		for(Entry<Integer, List<Double>> termEntry : topicTermMap.entrySet()){
			termScore.add(new TopicEntry(termEntry.getKey(), termEntry.getValue().get(k-1)));
		}
		
		Collections.sort(termScore, new Comparator<TopicEntry>() {

			@Override
			public int compare(TopicEntry o1, TopicEntry o2) {
				if((o1.getScore()-o2.getScore())>=0.0)
					return -1;
				else 
					return 1;
			}
		});
		
		List<TopicEntry> topTerms = termScore.subList(0, NO_OF_TOP_WORDS);
		topTermsForTopic.put(k, topTerms);
		
		
	}

	private static void parseModelFile() {
		BufferedReader reader = null;
		String line = "";
		int lineCount = 0;
		try {
			reader = new BufferedReader(new FileReader(MODEL_FILE));
			while ((line = reader.readLine()) != null) {
				lineCount++;
				if (line.trim().length() == 0)
					continue;

				if (lineCount > idTermMap.size()) {
					reader.close();
					return;
				}

				if (lineCount > 11) {
					String[] tokens = line.trim().split(" ");
					Integer termId = Integer.parseInt(tokens[0]);
					List<Double> probabilities = new ArrayList<>();
					for(int i=1;i<tokens.length;i++)
						probabilities.add(Double.parseDouble(tokens[i]));
					topicTermMap.put(termId,probabilities);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	private static void parseVocabFile() {

		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(VOCAB_FILE));
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() == 0)
					continue;

				String[] tokens = line.split("=");
				Integer tId = Integer.parseInt(tokens[1]);
				idTermMap.put(tId, tokens[0]);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}
