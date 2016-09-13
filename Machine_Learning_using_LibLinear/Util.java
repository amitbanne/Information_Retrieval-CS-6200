package assignment_6;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.tartarus.snowball.ext.PorterStemmer;

public class Util {

	public static String getStemOfWord(String input) {

		String temp = input;
		if (temp.contains("\"")) {
			temp = temp.substring(1, temp.length() - 1);
		}
		temp = temp.trim();
		PorterStemmer stemmer = new PorterStemmer();
		stemmer.setCurrent(temp);
		stemmer.stem();
		return stemmer.getCurrent();

	}

	public static long parseTermFreq(String explanation) {

		String t = explanation.split("termFreq=")[1];
		long tf = (long) Double.parseDouble(t.substring(0, t.indexOf("<")));
		return tf;
	}
	
	public static String parseDocNo(String sourceAsString) {
		String docNo = sourceAsString.split(",")[0].split(":")[1];
		docNo = docNo.replaceAll("\"", "");
		return docNo;
	}
	
	public static Properties loadDocLengthProperties() {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream("DocumentLength.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return prop;
	}
	
	public static Map<String, Double> getDocumentLengths() {

		Map<String,Double> docLengthMap = new HashMap<>();
		String queryPath = "DocumentLength.properties";
		
		BufferedReader reader = null;
		String line = "";
		List<String> queryList = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(queryPath));
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() > 0){
					String[] info = line.trim().split("=");
					String docNo = info[0];
					Double docLength = Double.parseDouble(info[1]);
					docLengthMap.put(docNo, docLength);
				}
					
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return docLengthMap;
	}
	
	public static void writeToFile(String fileName,String contentToFile) {

		String outputFile = fileName;

		File file = new File(outputFile);
			try {
				//if file doesnt exists, then create it
				if (!file.exists()) 
					file.createNewFile();
				FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(contentToFile);
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}


}
