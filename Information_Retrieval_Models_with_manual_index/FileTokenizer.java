package assignment_2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tartarus.snowball.ext.PorterStemmer;

public class FileTokenizer {

	static Map<String, ArrayList<DocIndex>> indexMap = new LinkedHashMap<String, ArrayList<DocIndex>>();
	static Properties stopWordProp;
	static boolean flag = false;
	static Map<String, CatalogEntry> catalog = new LinkedHashMap<String, CatalogEntry>();
	final static String directory = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 2/files/no_stem_stop/intermediate";
	final static String fileName = directory + "/NSS_IndexFile_";
	final static String catalogFileName = directory + "/NSS_CatalogFile_";
	static Map<String, String> docsToBeProcessed = new LinkedHashMap<String, String>();
	static int fileCounter = 1;
	static Map<String, Long> documentsIDMap = new LinkedHashMap<>();
	static Map<String, Long> termsIDMap = new LinkedHashMap<>();
	static long termIdCount = 1;
	static long documentIdCount = 1;
	static Set<String> terms = new LinkedHashSet<>();

	public static void main(String[] args) {

		/*stopWordProp = new Properties();
		try {
			stopWordProp.load(new FileInputStream("stoplist.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
*/
		processFiles();
		storeDocumentIdMapToFile();
		storeTermIdMapToFile();

		System.out.println("completed..");
	}


	private static void storeTermIdMapToFile() {

		StringBuilder sb = new StringBuilder();

		for (Entry<String, Long> e : termsIDMap.entrySet()) {
			String term = e.getKey();
			Long termId = e.getValue();

			sb.append(termId + "=" + term + "\n");
		}

		String outputFileName = "./TermIDs.txt";
		File file = new File(outputFileName);
		// if file doesnt exists, then create it

		try {
			if (!file.exists())
				file.createNewFile();

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(sb.toString());
			bw.close();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static void storeDocumentIdMapToFile() {

		StringBuilder sb = new StringBuilder();

		for (Entry<String, Long> e : documentsIDMap.entrySet()) {
			String doc = e.getKey();
			Long docId = e.getValue();
			sb.append(docId + "=" + doc + "\n");
		}
		String outputFileName = "./DocumentIDs.txt";
		File file = new File(outputFileName);
		// if file doesnt exists, then create it
		try {
			if (!file.exists())
				file.createNewFile();

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(sb.toString());
			bw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void processFiles() {
		// String directory_to_files = "G:/Northeastern
		// University/COURSES/Summer 2016/Information
		// Retrieval/Assignments/Assignment 2/source files/src";
		String directory_to_files = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 2/ap89_collection";

		File directory = new File(directory_to_files);

		for (File file : directory.listFiles()) {
			fileTokenize(directory_to_files + "/" + file.getName());
		}

		if (docsToBeProcessed.size() > 0) {
			parseDocuments();
			serializeIndexMap();
			storeCatalog();
		}
	}

	public static void fileTokenize(String fName) {

		// String fileName = "ap890102";

		BufferedReader reader = null;
		String line = "";
		StringBuilder stringBuilder = new StringBuilder();
		try {
			reader = new BufferedReader(new FileReader(fName));
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(System.lineSeparator());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		jSoupDocumentParser(stringBuilder.toString());

	}

	private static void jSoupDocumentParser(String fileData) {
		Document doc = Jsoup.parse(fileData, "UTF-8");
		Elements DOCElements = doc.getElementsByTag("DOC");
		Iterator<Element> docElementsIterator = DOCElements.iterator();
		while (docElementsIterator.hasNext()) {

			Element docElement = docElementsIterator.next();
			String docNo = docElement.getElementsByTag("DOCNO").first().text();

			Elements textElements = docElement.getElementsByTag("TEXT");
			StringBuilder docText = new StringBuilder();
			Iterator<Element> textElementsIterator = textElements.iterator();
			while (textElementsIterator.hasNext()) {
				Element textElement = textElementsIterator.next();
				String s = textElement.text();

				docText.append(s);
				docText.append(System.lineSeparator());

			}
			docsToBeProcessed.put(docNo, docText.toString());
			if (docsToBeProcessed.size() == 1000) {
				parseDocuments();
				serializeIndexMap();
				storeCatalog();
				indexMap = new LinkedHashMap<String, ArrayList<DocIndex>>();
				catalog = new LinkedHashMap<String, CatalogEntry>();
				docsToBeProcessed = new LinkedHashMap<String, String>();
				fileCounter++;
			}
			// parseDocument(docNo, docText.toString());
		}
	}

	private static void parseDocuments() {

		for (Entry<String, String> e : docsToBeProcessed.entrySet()) {
			parseDocument(e.getKey(), e.getValue());
		}

	}

	private static void parseDocument(String docNo, String docText) {

		Map<String, LinkedHashSet<Long>> freqCountMap = new HashMap<String, LinkedHashSet<Long>>();
		long pos = 1;
		String[] tokens = docText.split("[^(\\w+(\\.?\\w+)*)]");
		for (String s : tokens) {
			s = s.toLowerCase().trim();
			s = cleanUpTerm(s);
			if (s.length() > 1 && !s.equals("")) {
				if (stopWordProp.get(s) == null) {
					String stem = getStemOfWord(s);
					//String stem = s;
					if (freqCountMap.containsKey(stem)) {
						Set<Long> existingPositions = freqCountMap.get(stem);
						existingPositions.add(pos);
						freqCountMap.put(stem, (LinkedHashSet<Long>) existingPositions);
					} else {
						Set<Long> newPositions = new LinkedHashSet<>();
						newPositions.add(pos);
						freqCountMap.put(stem, (LinkedHashSet<Long>) newPositions);
					}
				}
			}
			pos++;
		}

		mapTermsToDocuments(docNo, freqCountMap);
	}

	private static String cleanUpTerm(String term) {

		term = term.replace(",", "");
		if(term.contains(".")){
			if(term.charAt(0)=='.')
				term = term.substring(1,term.length());
			
			if(term.trim().length()>0 && term.charAt(term.length()-1)=='.')
				term = term.substring(0,term.length()-1);
		}
		
		/*if(term.contains("(")){
			if(term.charAt(0)=='(')
				term = term.substring(1,term.length());
			
			if(term.trim().length()>0 && term.charAt(term.length()-1)=='(')
				term = term.substring(0,term.length()-1);
		}
		
		if(term.contains(")")){
			if(term.charAt(0)==')')
				term = term.substring(1,term.length());
			
			if(term.trim().length()>0 && term.charAt(term.length()-1)==')')
				term = term.substring(0,term.length()-1);
		}*/
		
		return term;
	}

	
	private static void mapTermsToDocuments(String docNo, Map<String, LinkedHashSet<Long>> freqCountMap) {

		for (Entry<String, LinkedHashSet<Long>> e : freqCountMap.entrySet()) {

			if (e.getValue().size() == 0) // no matching docs present
				continue;

			if (e.getKey().trim().length() == 0) // remove blank lines
				continue;
			if (indexMap.containsKey(e.getKey())) {
				List<DocIndex> sortedDocList = indexMap.get(e.getKey());
				sortedDocList.add(new DocIndex(docNo, e.getValue()));

				Collections.sort(sortedDocList, new Comparator<DocIndex>() {

					@Override
					public int compare(DocIndex o1, DocIndex o2) {
						if (o1.getTermFreq() >= o2.getTermFreq())
							return -1;
						else
							return 1;
					}
				});

				indexMap.put(e.getKey(), (ArrayList<DocIndex>) sortedDocList);
			} else {

				List<DocIndex> newList = new ArrayList<DocIndex>();
				newList.add(new DocIndex(docNo, e.getValue()));
				indexMap.put(e.getKey(), (ArrayList<DocIndex>) newList);
			}
		}
	}

	public static String getStemOfWord(String input) {

		String temp = input;

		temp = temp.trim();
		PorterStemmer stemmer = new PorterStemmer();
		stemmer.setCurrent(temp);
		stemmer.stem();
		return stemmer.getCurrent();
	}

	public static void serializeIndexMap() {

		RandomAccessFile file = null;
		long seekOffSet = 0;
		try {
			file = new RandomAccessFile(fileName + fileCounter + ".txt", "rw");
			String eof = System.lineSeparator();
			for (Entry<String, ArrayList<DocIndex>> e : indexMap.entrySet()) {

				StringBuilder sb = new StringBuilder();
				String term = e.getKey();

				long termId = 0;
				if (termsIDMap.containsKey(term)) {
					termId = termsIDMap.get(term);
				} else {
					termId = termIdCount++;
					termsIDMap.put(term, termId);
				}
				ArrayList<DocIndex> invertedList = e.getValue();

				String delimeter = "";
				StringBuilder invSb = new StringBuilder();
				for (DocIndex dIndex : invertedList) {

					long docId = 0;
					if (documentsIDMap.containsKey(dIndex.getDocNo())) {
						docId = documentsIDMap.get(dIndex.getDocNo());
					} else {
						docId = documentIdCount++;
						documentsIDMap.put(dIndex.getDocNo(), docId);
					}

					invSb.append(delimeter);
					delimeter = ";";

					// compressing docId
					String compressed = "";
					if (docId % 10000 == 0) {
						long x = docId / 10000;
						compressed = x + "^" + 4;
						invSb.append(compressed);
					} else if (docId % 1000 == 0) {
						long x = docId / 1000;
						compressed = x + "^" + 3;
						invSb.append(compressed);
					} else
						invSb.append(docId);

					/*
					 * invSb.append(":"); invSb.append(dIndex.getTermFreq());
					 */
					invSb.append(":");
					StringBuilder positionBuilder = new StringBuilder();
					String del = "";
					for (Long pos : dIndex.getPositions()) {
						positionBuilder.append(del);
						del = ",";
						positionBuilder.append(pos);
					}
					invSb.append(positionBuilder);
				}

				// compressing termId
				String compressed = "";
				if (termId % 100000 == 0) {
					long x = termId / 100000;
					compressed = x + "^" + 5;
					sb.append(compressed);
				} else if (termId % 10000 == 0) {
					long x = termId / 10000;
					compressed = x + "^" + 4;
					sb.append(compressed);
				} else if (termId % 1000 == 0) {
					long x = termId / 1000;
					compressed = x + "^" + 3;
					sb.append(compressed);
				} else
					sb.append(termId);

				sb.append("=" + invSb.toString() + eof);

				// sb.append(termId + "=" + invSb.toString() + eof);
				// sb.append(term + "=" + invSb.toString() + eof);

				if (sb.toString().trim().length() >= 1) {
					long startIndex = seekOffSet;
					file.seek(seekOffSet);
					file.writeBytes(sb.toString());
					seekOffSet = file.getFilePointer();
					long endIndex = seekOffSet;
					catalog.put(term, new CatalogEntry(term, startIndex, endIndex));
				}
			}
			file.close();

		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private static void storeCatalog() {
		StringBuilder sb = new StringBuilder();
		String delimiter = System.lineSeparator();
		for (Entry<String, CatalogEntry> e : catalog.entrySet()) {

			String s = e.getKey() + "=" + e.getValue().getStartIndex() + "," + e.getValue().getEndIndex() + delimiter;
			if (s.trim().length() > 1)
				sb.append(s);
		}
		try {
			fileWriterUtil(sb.toString());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void fileWriterUtil(String contentToFile) throws IOException {

		String outputFileName = catalogFileName + fileCounter + ".txt";
		File file = new File(outputFileName);
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(contentToFile);
		bw.close();
	}
}
