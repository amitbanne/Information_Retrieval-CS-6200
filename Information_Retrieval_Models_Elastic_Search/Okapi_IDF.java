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



public class Okapi_IDF {

	static Map<String, Double> okapiIDFScoreMap = new HashMap<>();
	static Map<String, ArrayList<DocumentData>> mappedData;
	static long avgDocLength = 247;
	static String queryNo;
	static final long TOTAL_DOCUMENTS = 84679;
	static List<DocRank> rankedDocuments = new ArrayList<>();
	static Properties prop;

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static void okapiIDFUtil(Map<String, ArrayList<DocumentData>> datamap, String qNo){
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


		
		computeokapiIDFScoreForDocuments();
		
	}
	
	
	private static void computeokapiIDFScoreForDocuments(){
		
		
		for(Entry<String, ArrayList<DocumentData>> entry: mappedData.entrySet()){
			
			List<DocumentData> docList = entry.getValue();
			scoreDocuments(docList);
		}
		
		List<Entry<String, Double>> scoreEntrySet = new ArrayList<>(okapiIDFScoreMap.entrySet());
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
			String output = queryNo+" Q0 "+e.getKey()+" "+rank +" "+ e.getValue()+" Ranked ";
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
		String fileName = "okapi-idf.txt";
		
		File file = new File(outputFilePath+"\\"+fileName);
		// if file doesnt exists, then create it
					if (!file.exists()) {
						file.createNewFile();
					}		
					FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(contentToFile);
					bw.close();

	}


	private static void computeScoreForDocument(DocumentData docData) {

		String docNo = docData.getDocNo();
		long termFreq = docData.getTermFreq();
		double okapiIDFScore = 0.0;
		if(termFreq > 0){
			
			okapiIDFScore = getokapiIDFScore(docData);
		
		
		if(okapiIDFScoreMap.containsKey(docNo)){
			double okapiIDFScoreToBeUpdated = okapiIDFScoreMap.get(docNo);			
			okapiIDFScoreToBeUpdated+=okapiIDFScore;
			okapiIDFScoreMap.put(docNo, okapiIDFScoreToBeUpdated);
		}else
			okapiIDFScoreMap.put(docNo, okapiIDFScore);
				
		}
	}
	
	public  static double getokapiIDFScore(DocumentData docData) {
		
		

		String docNo = docData.getDocNo();
		
		if(docNo.contains("\""))
			docNo = docNo.substring(1, docNo.lastIndexOf("\""));	
		
		int docLength = Integer.parseInt(prop.get(docNo).toString());
		
		double lengthFactor = 1.5 * (docLength/avgDocLength);
		double denominator = (docData.getTermFreq()+0.5+lengthFactor);
		double okapiScore = (Double.valueOf(docData.getTermFreq()))/denominator;
		double logFactor = Math.log10(Double.valueOf(TOTAL_DOCUMENTS)/docData.getDocFreq());
		double okapiIDFScore = okapiScore * logFactor;
		
		
			return okapiIDFScore;
	}

	
}
