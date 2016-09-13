package assignment_6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

public class LaplaceSmoothing {

	static Map<String, Double> laplaceScoreMap;
	static Map<String, ArrayList<DocumentData>> mappedData;
	static long avgDocLength = 247;
	static final long TOTAL_DOCUMENTS = 84679;
	static String queryNo;
	static Properties prop;

	public static List<DocRank> laplaceUtil(Map<String, ArrayList<DocumentData>> datamap, String qNo){
		mappedData = datamap;
		queryNo = qNo;
		
		prop = Util.loadDocLengthProperties(); 
		return computeLaplaceScoreForDocuments();
		
	}
	
	private static List<DocRank> computeLaplaceScoreForDocuments(){
		laplaceScoreMap = new HashMap<>();
		for(Entry<String, ArrayList<DocumentData>> entry: mappedData.entrySet()){
			
			List<DocumentData> docList = entry.getValue();
			scoreDocuments(docList);
		}
		List<Entry<String, Double>> scoreEntrySet = new ArrayList<>(laplaceScoreMap.entrySet());
		Collections.sort(scoreEntrySet, new Comparator<Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> e1, Entry<String, Double> e2) {
				if(e1.getValue() >= e2.getValue())
					return -1;
				else 
					return 1;	
			}

		});
		
		List<DocRank> rankedDocuments = new ArrayList<>();
		for(Entry<String, Double> docEntry : scoreEntrySet){
			rankedDocuments.add(new DocRank(queryNo, docEntry.getKey(), docEntry.getValue()));
		}
		
		return rankedDocuments;
		
	}


	private static void scoreDocuments(List<DocumentData> docList) {
		for(DocumentData docData : docList){
			computeScoreForDocument(docData);
		}
	}
	
	private static void computeScoreForDocument(DocumentData docData) {

		String docNo = docData.getDocNo();
		double laplaceScore = 0.0;
		laplaceScore = getLaplaceScore(docData);
		
		if(laplaceScoreMap.containsKey(docNo)){
			double laplaceToBeUpdated = laplaceScoreMap.get(docNo);			
			laplaceToBeUpdated+=laplaceScore;
			laplaceScoreMap.put(docNo, laplaceToBeUpdated);
		}else
			laplaceScoreMap.put(docNo, laplaceScore);
				
	}
	
	public  static double getLaplaceScore(DocumentData docData) {
		
		String docNo = docData.getDocNo();
		double laplaceScore = 0.0;
		//double docLength = Double.parseDouble(prop.get(docNo).toString());
		double docLength = Integer.parseInt(prop.get(docNo).toString());
		long V = 178081;
		
		double numerator = Double.valueOf((docData.getTermFreq()+1));
		double denominator = (docLength+V);
		laplaceScore = Math.log10(numerator/denominator);
		//laplaceScore =(numerator/denominator);
		return laplaceScore;
	}
		
}
