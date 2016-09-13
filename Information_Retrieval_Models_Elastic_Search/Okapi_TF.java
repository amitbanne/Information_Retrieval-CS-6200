package assignment_1;

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
import java.util.Map.Entry;

import java.util.Properties;

public class Okapi_TF {
	static final long TOTAL_DOCUMENTS = 84679;
	static Map<String, Double> okapiScoreMap = new HashMap<>();
	static Map<String, Double> okapiIDFScoreMap = new HashMap<>();
	static Map<String, ArrayList<DocumentData>> mappedData;
	static long avgDocLength = 247;
	static String queryNo;
	static List<DocRank> rankedDocuments = new ArrayList<>();
	static Properties prop;

	public static void main(String[] args) {

	}

	public static void okapiTFUtil(Map<String, ArrayList<DocumentData>> datamap, String qNo) {
		mappedData = datamap;
		queryNo = qNo;

		prop = new Properties();

		try {
			prop.load(new FileInputStream("DocumentLength.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		computeOkapiScoreForDocuments();

	}

	private static void computeOkapiScoreForDocuments() {

		for (Entry<String, ArrayList<DocumentData>> entry : mappedData.entrySet()) {

			List<DocumentData> docList = entry.getValue();
			scoreDocuments(docList);
		}

		//okapi TF
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


		if (okapiTFScoreEntrySet.size() < 2000)
			writeResultsToFile("okapi-tf.txt", okapiTFScoreEntrySet);
		else
			writeResultsToFile("okapi-tf.txt", okapiTFScoreEntrySet.subList(0, 2000));

		if (okapiIDFScoreEntrySet.size() < 2000)
			writeResultsToFile("okapi-idf.txt", okapiIDFScoreEntrySet);
		else
			writeResultsToFile("okapi-idf.txt", okapiIDFScoreEntrySet.subList(0, 2000));

		
	}

	private static void scoreDocuments(List<DocumentData> docList) {

		for (DocumentData docData : docList) {
			computeScoreForDocument(docData);
		}

	}

	public static void writeResultsToFile(String fileName, List<Entry<String, Double>> rankedDocuments) {

		int rank = 1;
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Double> e : rankedDocuments) {
			String temp = e.getKey();
			String docNo = temp;
			if (temp.contains("\""))
				docNo = temp.substring(1, temp.lastIndexOf("\""));
			String output = queryNo + " Q0 " + docNo + " " + rank + " " + e.getValue() + " Exp";
			sb.append(output + "\n");
			rank++;
		}
		sb.append("\n");
		try {
			fileWriterUtil(fileName, sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		okapiScoreMap = new HashMap<>();
		okapiIDFScoreMap = new HashMap<>();
	}

	public static void fileWriterUtil(String fileName, String contentToFile) throws IOException {

		/*
		 * try { prop.load(new FileInputStream("FileData.properties")); } catch
		 * (FileNotFoundException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); } String outputFilePath =
		 * prop.get("OUTPUT_FILE_PATH").toString(); String fileName =
		 * prop.get("OKAPI_TF").toString();
		 */

		String outputFilePath = "C:\\Users\\NishantRatnakar\\Desktop\\Lecture Notes\\IR\\AP89_DATA\\AP_DATA\\output";

		File file = new File(outputFilePath + "\\" + fileName);
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(contentToFile);
		bw.close();

	}

	private static void computeScoreForDocument(DocumentData docData) {

		String docNo = docData.getDocNo();
		long termFreq = docData.getTermFreq();
		double okapiScore = 0.0;
		double okapiIDFScore = 0.0;
		if (termFreq > 0) {
			okapiScore = getOkapiScore(docData);
			
			if (okapiScoreMap.containsKey(docNo)) {

				double okapiScoreToBeUpdated = okapiScoreMap.get(docNo);
				okapiScoreToBeUpdated += okapiScore;
				okapiScoreMap.put(docNo, okapiScoreToBeUpdated);
			} else
				okapiScoreMap.put(docNo, okapiScore);

			
			okapiIDFScore = getokapiIDFScore(okapiScore, docData);
			if (okapiIDFScoreMap.containsKey(docNo)) {

				double okapiIDFScoreToBeUpdated = okapiIDFScoreMap.get(docNo);
				okapiIDFScoreToBeUpdated += okapiScore;
				okapiIDFScoreMap.put(docNo, okapiIDFScoreToBeUpdated);
			} else
				okapiIDFScoreMap.put(docNo, okapiIDFScore);

		}
	}

	public static double getOkapiScore(DocumentData docData) {

		String docNo = docData.getDocNo();
		// docNo = docNo.substring(1, docNo.lastIndexOf("\""));
		
		double docLength = Integer.parseInt(prop.get(docNo).toString());

		double lengthFactor = 1.5 * (docLength / avgDocLength);
		double denominator = (docData.getTermFreq() + 0.5 + lengthFactor);
		double okapiScore = (Double.valueOf(docData.getTermFreq())) / denominator;

		return okapiScore;
	}

	public static double getokapiIDFScore(double okapiScore, DocumentData docData) {

		double logFactor = Math.log10(Double.valueOf(TOTAL_DOCUMENTS) / docData.getDocFreq());
		double okapiIDFScore = okapiScore * logFactor;

		return okapiIDFScore;
	}

}
