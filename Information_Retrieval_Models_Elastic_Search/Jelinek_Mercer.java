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
import java.util.Properties;
import java.util.Map.Entry;


public class Jelinek_Mercer {

	static Map<String, Double> jelinekScoreMap = new HashMap<>();
	static Map<String, ArrayList<DocumentData>> mappedData;
	static long avgDocLength = 247;
	static final long TOTAL_DOCUMENTS = 84679;
	static String queryNo;
	static List<DocRank> rankedDocuments = new ArrayList<>();
	static Properties prop;
	static Map<String, Long> overallTermFreq;

	
	public Jelinek_Mercer() {
		// TODO Auto-generated constructor stub
	}
	

	public static void jelinekMercerUtil(Map<String, ArrayList<DocumentData>> datamap,Map<String,Long> totalTermFreq, String qNo){
		mappedData = datamap;
		queryNo = qNo;
		overallTermFreq = totalTermFreq;
		prop = new Properties();
		InputStream input = null;
		try {
			prop.load(new FileInputStream("DocumentLength.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		computeLaplaceScoreForDocuments();
		
	}
	
	
	private static void computeLaplaceScoreForDocuments(){
		
		
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
		
		if(scoreEntrySet.size() < 1500)
			writeResultsToFile(scoreEntrySet);
		else
			writeResultsToFile(scoreEntrySet.subList(0, 1500));
		
		
	}


	private static void scoreDocuments(List<DocumentData> docList) {

		
		for(DocumentData docData : docList){
			computeScoreForDocument(docData);
		}
		

	}
	
	
	public static void writeResultsToFile(List<Entry<String, Double>> rankedDocuments){
		
		int rank = 1;
		StringBuilder sb = new StringBuilder();
		for(Entry<String, Double> e: rankedDocuments){
			String temp = e.getKey();
			String docNo=temp;
			if(temp.contains("\""))
				docNo = temp.substring(1, temp.lastIndexOf("\""));
			String output = queryNo+" Q0 "+docNo+" "+rank +" "+ e.getValue()+" Exp";
			sb.append(output+"\n");
			rank++;
		}
		sb.append("\n");
		try {
			fileWriterUtil(sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static void fileWriterUtil(String contentToFile) throws IOException{
		String outputFilePath = "C:\\Users\\NishantRatnakar\\Desktop\\Lecture Notes\\IR\\AP89_DATA\\AP_DATA\\output";
		String fileName = "jelinek-mercer.txt";
		
		File file = new File(outputFilePath+"\\"+fileName);
		// if file doesnt exists, then create it
					if (!file.exists()) {
						file.createNewFile();
					}		
					FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(contentToFile);
					bw.close();
					jelinekScoreMap = new HashMap<>();

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
		double docLength = QueryLaplace.docLengthMap.get(docNo);//Integer.parseInt(prop.get(docNo).toString());
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
