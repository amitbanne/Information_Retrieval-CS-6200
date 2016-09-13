package assignment_7;

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

public class TopEmails {

//	private static final String probFile = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/liblinear-2.1/liblinear-2.1/windows/UNIGRAM-OUTPUT.txt";
//	private static final String documentList = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/Unigram_Documents.txt";
//	private static final String OUTPUT_FILE = "./UNIGRAM_SPAM_MAILS.txt";

	private static final String probFile = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/liblinear-2.1/liblinear-2.1/windows/SPAM-OUTPUT.txt";
	private static final String documentList = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/SPAM_LIST_Documents.txt";
	private static final String OUTPUT_FILE = "./SPAM_MAILS.txt";

	/*private static final String probFile = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/liblinear-2.1/liblinear-2.1/windows/CUSTOM-SPAM-Output.txt";
	private static final String documentList = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/CUSTOM_SPAM_LIST_Documents.txt";
	private static final String OUTPUT_FILE = "./CUSTOM_SPAM_SCORE_MAP.txt";
	*/
	//private static final String coefficients = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/co-efficients_spam.txt";
	private static final String coefficients = "./co-efficients_spam.txt";
//	private static final String words = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/SPAM_SET.txt";
	private static final String words = "./SPAM_SET.txt";
	private static final String OUTPUT_WORDS = "./TOP_SPAM_WORDS.txt";
	
	private static final String DOC = words;
	private static final String SCORE = coefficients;
	private static final String OUTPUT = OUTPUT_WORDS;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		List<String> docs = deserializeDocs();
		List<Double> scores = deserializeScores();
		
		Map<String, Double> docScoreMap = new LinkedHashMap<>();
		
		for(int i=0;i<scores.size();i++){
			docScoreMap.put(docs.get(i), scores.get(i));
		}
		
		List<Entry<String,Double>> eList = new ArrayList<>(docScoreMap.entrySet());
		Collections.sort(eList, new Comparator<Entry<String,Double>>() {

			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				if((o1.getValue()-o2.getValue()) >=0)
					return -1;
				else
					return 1;
			}
		});
		
		printDocScores(eList);
		System.out.println("Completed");
	}
	
	private static void printDocScores(List<Entry<String, Double>> eList) {

		StringBuilder dataString = new StringBuilder();
		String delimiter = "";

		for (Entry<String, Double> uEntry : eList) {
			dataString.append(delimiter);
			delimiter = System.lineSeparator();
			dataString.append(uEntry.getKey());
			dataString.append(" ");
			dataString.append(uEntry.getValue());
		}
		Util.writeToFile(OUTPUT, dataString.toString());

	}


	
	public static List<String> deserializeDocs(){
		
		List<String> docs = new ArrayList<String>();
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(DOC));
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
	
	public static List<Double> deserializeScores(){
		
		List<Double> scores = new ArrayList<Double>();
		int count=0;
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(SCORE));
			while ((line = reader.readLine()) != null) {
//				if(count++!=0)
//					scores.add(Double.parseDouble(line.trim().split(" ")[1]));
				
				scores.add(Double.parseDouble(line.trim()));
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return scores;
	}

	
}
