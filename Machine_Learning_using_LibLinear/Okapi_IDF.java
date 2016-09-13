package assignment_6;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;


public class Okapi_IDF {

	static Map<String, Double> okapiIDFScoreMap;
	static Map<String, ArrayList<DocumentData>> mappedData;
	static long avgDocLength = 247;
	static String queryNo;
	static final long TOTAL_DOCUMENTS = 84679;
	static List<DocRank> rankedDocuments = new ArrayList<>();
	static Properties prop;

	
	public static List<DocRank> okapiIDFUtil(Map<String, ArrayList<DocumentData>> datamap, String qNo) {
		mappedData = datamap;
		queryNo = qNo;
		prop = Util.loadDocLengthProperties();
		return computeOkapiScoreForDocuments();

	}
	
	private static List<DocRank> computeOkapiScoreForDocuments() {

		okapiIDFScoreMap = new HashMap<>();
		for (Entry<String, ArrayList<DocumentData>> entry : mappedData.entrySet()) {

			List<DocumentData> docList = entry.getValue();
			scoreDocuments(docList);
		}
		
		//okapi IDF
		List<Entry<String, Double>> okapiIDFScoreEntrySet = new ArrayList<>(okapiIDFScoreMap.entrySet());
		Collections.sort(okapiIDFScoreEntrySet, new Comparator<Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> e1, Entry<String, Double> e2) {
				if (e1.getValue() >= e2.getValue())
					return -1;
				else
					return 1;
			}

		});

		List<DocRank> rankedDocuments = new ArrayList<>();
		for(Entry<String, Double> docEntry : okapiIDFScoreEntrySet){
			rankedDocuments.add(new DocRank(queryNo, docEntry.getKey(), docEntry.getValue()));
		}
		
		return rankedDocuments;

		
		
	}

	private static void scoreDocuments(List<DocumentData> docList) {

		for (DocumentData docData : docList) {
			computeScoreForDocument(docData);
		}
	}


	private static void computeScoreForDocument(DocumentData docData) {

		String docNo = docData.getDocNo();
		long termFreq = docData.getTermFreq();
		double okapiIDFScore = 0.0;
		if (termFreq > 0) {
			
			okapiIDFScore = getokapiIDFScore(docData);
			if (okapiIDFScoreMap.containsKey(docNo)) {
				double okapiIDFScoreToBeUpdated = okapiIDFScoreMap.get(docNo);
				okapiIDFScoreToBeUpdated += okapiIDFScore;
				okapiIDFScoreMap.put(docNo, okapiIDFScoreToBeUpdated);
			} else
				okapiIDFScoreMap.put(docNo, okapiIDFScore);

		}
	}

	public static double getokapiIDFScore(DocumentData docData) {

		String docNo = docData.getDocNo();

		double docLength = Integer.parseInt(prop.get(docNo).toString());

		double lengthFactor = 1.5 * (docLength / avgDocLength);
		double denominator = (docData.getTermFreq() + 0.5 + lengthFactor);
		double okapiScore = (Double.valueOf(docData.getTermFreq())) / denominator;

		
		double logFactor = Math.log10(Double.valueOf(TOTAL_DOCUMENTS) / docData.getDocFreq());
		double okapiIDFScore = okapiScore * logFactor;

		return okapiIDFScore;
	}

}
