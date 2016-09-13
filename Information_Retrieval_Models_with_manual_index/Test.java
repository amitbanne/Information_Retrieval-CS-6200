package assignment_2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import org.tartarus.snowball.ext.PorterStemmer;

import assignment_2.CatalogEntry;

public class Test {
	
	static String catalogFile = "C:\\Users\\NishantRatnakar\\Desktop\\Lecture Notes\\IR\\Amit\\Assignment 2\\files\\CatalogFile.txt";
	//static String catalogFile = "C:\\Users\\NishantRatnakar\\Desktop\\Lecture Notes\\IR\\Amit\\Assignment 2\\files\\CatalogFile.txt";
	static String validationFile = "C:\\Users\\NishantRatnakar\\Desktop\\Lecture Notes\\IR\\Amit\\Assignment 2\\files\\in.0";
	//in.0
	static String indexFile = "C:\\Users\\NishantRatnakar\\Desktop\\Lecture Notes\\IR\\Amit\\Assignment 2\\files\\IndexFile.txt";
	//static String indexFile = "C:\\Users\\NishantRatnakar\\Desktop\\Lecture Notes\\IR\\Amit\\Assignment 2\\files\\IndexFile.txt";
	static List<String> words = new ArrayList<>();
	static Map<String, CatalogEntry> catalog = new LinkedHashMap<>();
	public static void main(String[] args){
		
		readCatalogFile();
		verifyCatalog();
		
	}
	
	
	private static void verifyCatalog() {

		BufferedReader reader = null;
		String line = "";
		StringBuilder sb = new StringBuilder();
		try {
			RandomAccessFile raf = new RandomAccessFile(indexFile, "rw");
			reader = new BufferedReader(new FileReader(validationFile));
			while ((line = reader.readLine()) != null) {
				String word = getStemOfWord(line.trim().toLowerCase());
				if(catalog.containsKey(word)){
					long startIndex = catalog.get(word).getStartIndex();
					raf.seek(startIndex);
					String entry = raf.readLine().trim();
					String format = line.trim()+" "+parseTerm(entry);
					sb.append(format);
					sb.append(System.lineSeparator());
				}else{
					sb.append(line.trim()+" "+0+" "+0);
					sb.append(System.lineSeparator());
				}
			}
			
			reader.close();
			raf.close();
			writeToFile(sb.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		

	private static void writeToFile(String string) throws IOException {

		String outputFileName = "./output.100.txt";
		File file = new File(outputFileName);
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(string);
		bw.close();
		
	}


	private static String parseTerm(String entry) {

		String t = entry.split("=")[1];
		String[] docs = t.split(";");
		String docFreq = ""+docs.length;
		int tf = 0;
		for(String d: docs){
			String[] tfs = d.split(":")[1].split(",");
			tf+=tfs.length;
		}
		
		return (docFreq+" "+tf);
	}


	public static void readCatalogFile(){
		BufferedReader reader = null;
		String line = "";
		
		try {
			reader = new BufferedReader(new FileReader(catalogFile));
			while ((line = reader.readLine()) != null) {
				String word = line.split("=")[0].trim().toLowerCase();
				Long startIndex = Long.parseLong(line.split("=")[1].split(",")[0]);
				Long endIndex = Long.parseLong(line.split("=")[1].split(",")[1]);
				catalog.put(word, new CatalogEntry(word, startIndex, endIndex));
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
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

}
