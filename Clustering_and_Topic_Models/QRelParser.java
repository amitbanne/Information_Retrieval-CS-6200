package assignment_8;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QRelParser {

	public static final String QREL_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 8/data/qrels.adhoc.51-100.AP89.txt";
	public static final String QREL_DELIMITER = " ";
	public static Map<Integer, HashMap<String, Integer>> parseQRel(){
		
		Map<Integer, HashMap<String, Integer>> qRelData = new HashMap<>();
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(QREL_FILE));
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.trim().split(QREL_DELIMITER);
				
				if(tokens.length!=4)
					continue;
				
				Integer queryID = Integer.parseInt(tokens[0]);
				String docID = tokens[2];
				Integer relevance = Integer.parseInt(tokens[3]);
				
				if(qRelData.containsKey(queryID)){
					Map<String, Integer> existingDocMap =qRelData.get(queryID);
					existingDocMap.put(docID, relevance);
					qRelData.put(queryID, (HashMap<String, Integer>) existingDocMap);
				}else{
					Map<String, Integer> newDocMap = new HashMap<>();
					newDocMap.put(docID, relevance);
					qRelData.put(queryID, (HashMap<String, Integer>) newDocMap);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return qRelData;
	}

	
}
