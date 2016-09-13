package assignment_6;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;



public class BM25_Util {

	static Map<String, Double> bm25ScoreMap;
	static Map<String, ArrayList<DocumentData>> mappedData;
	static long avgDocLength = 247;
	static final long TOTAL_DOCUMENTS = 84679;
	static String queryNo;
	static Properties prop;

	public static List<DocRank> bm25TFUtil(Map<String, ArrayList<DocumentData>> datamap, String qNo){
		mappedData = datamap;
		queryNo = qNo;
		prop = Util.loadDocLengthProperties();
		return computeBM25ScoreForDocuments();
	}
	
	private static List<DocRank> computeBM25ScoreForDocuments(){
		bm25ScoreMap = new HashMap<>();
		for(Entry<String, ArrayList<DocumentData>> entry: mappedData.entrySet()){
			
			List<DocumentData> docList = entry.getValue();
			scoreDocuments(docList);
		}
		
		List<Entry<String, Double>> scoreEntrySet = new ArrayList<>(bm25ScoreMap.entrySet());
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
		long termFreq = docData.getTermFreq();
		double bm25Score = 0.0;
		if(termFreq > 0){
			bm25Score = getBM25Score(docData);
		
		if(bm25ScoreMap.containsKey(docNo)){
			double okapiScoreToBeUpdated = bm25ScoreMap.get(docNo);			
			okapiScoreToBeUpdated+=bm25Score;
			bm25ScoreMap.put(docNo, okapiScoreToBeUpdated);
		}else
			bm25ScoreMap.put(docNo, bm25Score);
				
		}
	}
	
	public  static double getBM25Score(DocumentData docData) {
		
		String docNo = docData.getDocNo();
		double bm25Score =0.0;
		//docNo = docNo.substring(1, docNo.lastIndexOf("\""));	
		int docLength = Integer.parseInt(prop.get(docNo).toString());
		double k1 = 1.2;
		double k2 = 100;
		double b = 0.4;
		
		double term1 = (TOTAL_DOCUMENTS+0.5)/(docData.getDocFreq()+0.5); 
		double logFactor = Math.log10(term1);
		
		double lengthFactor = Double.valueOf(docLength)/avgDocLength;
		double term2Numerator = Double.valueOf((docData.getTermFreq()+k1*docData.getTermFreq()));
		double term2Denominator = docData.getTermFreq()+k1 *((1-b)+b*lengthFactor);
		
		double term2 = term2Numerator/term2Denominator;
		
		int termQueryFreq = 1;
		
		double term3 = Double.valueOf((termQueryFreq+(k2*termQueryFreq)))/(termQueryFreq+k2);
		
		bm25Score = logFactor * term2 * term3;
		
		return bm25Score;
	}
	
}
