package assignment_6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class Okapi_TF {
	static final long TOTAL_DOCUMENTS = 84679;
	static Map<String, Double> okapiScoreMap;
	static Map<String, ArrayList<DocumentData>> mappedData;
	static long avgDocLength = 247;
	static String queryNo;
	static Properties prop;

	public static List<DocRank> okapiTFUtil(Map<String, ArrayList<DocumentData>> datamap, String qNo) {
		mappedData = datamap;
		queryNo = qNo;
		prop = Util.loadDocLengthProperties();
		return computeOkapiScoreForDocuments();

	}

	private static List<DocRank> computeOkapiScoreForDocuments() {
		okapiScoreMap = new HashMap<>();
		for (Entry<String, ArrayList<DocumentData>> entry : mappedData.entrySet()) {

			List<DocumentData> docList = entry.getValue();
			scoreDocuments(docList);
		}

		// okapi TF
		List<Entry<String, Double>> okapiTFScoreEntrySet = new ArrayList<>(okapiScoreMap.entrySet());
		Collections.sort(okapiTFScoreEntrySet, new Comparator<Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> e1, Entry<String, Double> e2) {
				if (e1.getValue() >= e2.getValue())
					return -1;
				else
					return 1;
			}

		});
		
		List<DocRank> rankedDocuments = new ArrayList<>();
		for(Entry<String, Double> docEntry : okapiTFScoreEntrySet){
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
		double okapiScore = 0.0;
		if (termFreq > 0) {
			okapiScore = getOkapiScore(docData);

			if (okapiScoreMap.containsKey(docNo)) {

				double okapiScoreToBeUpdated = okapiScoreMap.get(docNo);
				okapiScoreToBeUpdated += okapiScore;
				okapiScoreMap.put(docNo, okapiScoreToBeUpdated);
			} else
				okapiScoreMap.put(docNo, okapiScore);
		}
	}

	public static double getOkapiScore(DocumentData docData) {

		String docNo = docData.getDocNo();

		double docLength = Integer.parseInt(prop.get(docNo).toString());

		double lengthFactor = 1.5 * (docLength / avgDocLength);
		double denominator = (docData.getTermFreq() + 0.5 + lengthFactor);
		double okapiScore = (Double.valueOf(docData.getTermFreq())) / denominator;

		return okapiScore;
	}

}
