package assignment_6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;


public class Jelinek_Mercer {

	static Map<String, Double> jelinekScoreMap;
	static Map<String, ArrayList<DocumentData>> mappedData;
	static long avgDocLength = 247;
	static final long TOTAL_DOCUMENTS = 84679;
	static String queryNo;
	static Properties prop;
	static Map<String, Long> overallTermFreq;

	public static List<DocRank> jelinekMercerUtil(Map<String, ArrayList<DocumentData>> datamap,Map<String,Long> totalTermFreq, String qNo){
		mappedData = datamap;
		queryNo = qNo;
		overallTermFreq = totalTermFreq;
		prop = Util.loadDocLengthProperties(); 		
		return computeJelinekScoreForDocuments();
		
	}
	
	private static List<DocRank> computeJelinekScoreForDocuments(){
		jelinekScoreMap = new HashMap<>();
		for(Entry<String, ArrayList<DocumentData>> entry: mappedData.entrySet()){
			List<DocumentData> docList = entry.getValue();
			scoreDocuments(docList);
		}
		
		List<Entry<String, Double>> scoreEntrySet = new ArrayList<>(jelinekScoreMap.entrySet());
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
		double jelinekScore = 0.0;

		jelinekScore = jelinekScore(docData);
		
		if(jelinekScoreMap.containsKey(docNo)){
			double okapiScoreToBeUpdated = jelinekScoreMap.get(docNo);			
			okapiScoreToBeUpdated+=jelinekScore;
			jelinekScoreMap.put(docNo, okapiScoreToBeUpdated);
		}else
			jelinekScoreMap.put(docNo, jelinekScore);
	}
	
	public  static double jelinekScore(DocumentData docData) {
		
		String docNo = docData.getDocNo();
		double jelinekScore =0.0;
		//docNo = docNo.substring(1, docNo.lastIndexOf("\""));	
		double docLength = Integer.parseInt(prop.get(docNo).toString());
		double lambda = 0.7; 		
		
		double term1 = lambda * ((Double.valueOf(docData.getTermFreq()))/docLength);
		
		long totalTermFreq = overallTermFreq.get(docData.getTerm());
		
		double numerator = Double.valueOf(totalTermFreq-docData.getTermFreq());
		double denominator = Double.valueOf((avgDocLength*TOTAL_DOCUMENTS) - docLength);
		double term2 = (1-lambda) * (numerator / denominator); 
		
		double temp = (term1+term2);

		if(term1 > 0 || term2 >0)
			jelinekScore = Math.log10(temp);
		
		return jelinekScore;
	}

}
