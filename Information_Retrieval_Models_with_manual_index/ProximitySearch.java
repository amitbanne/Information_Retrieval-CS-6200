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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.tartarus.snowball.ext.PorterStemmer;

public class ProximitySearch {

	static List<DocumentData> docData;
	static Map<String, ArrayList<DocumentData>> mappedData;
	static Properties queryProp, stopWordProp, docLengthProp;
	static Map<String, CatalogEntry> catalogMap = new HashMap<String, CatalogEntry>();
	//final static String indexFileName = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 2/files/IndexFile.txt";
	final static String indexFileName = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 6/data/IndexFile.txt";
	static Map<Long, String> idToDocMap = new LinkedHashMap<>();
	static Map<Long, String> idToTermMap = new LinkedHashMap<>();
	static Map<String, ArrayList<ProximityEntry>> queryProximityMap = new LinkedHashMap<>();
	static Map<Integer, LinkedHashMap<String, Double>> proximityScores = new HashMap<>();
	static Map<String, Double> docScoreMap;
	
	public static Map<Integer, LinkedHashMap<String, Double>> proximityUtil(){
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();

		//System.out.println(dateFormat.format(date)); // 2014/08/06 15:59:48
		queryProp = new Properties();
		docLengthProp = new Properties();
		stopWordProp = new Properties();
		try {
			queryProp.load(new FileInputStream("FileData.properties"));
			stopWordProp.load(new FileInputStream("stoplist.properties"));
			docLengthProp.load(new FileInputStream("DocumentLength.properties"));

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		processDocuments();
		//System.out.println("completed");
		date = new Date();
		//System.out.println(dateFormat.format(date)); // 2014/08/06 15:59:48

		return proximityScores;
		
	}

	private static void mapDocumentIdsFromFile() {

		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(
					"./DocumentIDs.txt"));
			while ((line = reader.readLine()) != null) {
				Long docId = Long.parseLong(line.split("=")[0]);
				String docNo = line.split("=")[1];
				idToDocMap.put(docId, docNo);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void mapTermIdsFromFile() {

		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(
					"./TermIDs.txt"));
			while ((line = reader.readLine()) != null && line.contains("=")) {
				Long termId = Long.parseLong(line.split("=")[0]);
				String term = line.split("=")[1];
				idToTermMap.put(termId, term);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, ArrayList<DocumentData>> processDocuments() {

		// mappedData = new HashMap<>();
		docData = new ArrayList<>();
		mapTermIdsFromFile();
		mapDocumentIdsFromFile();

		parseCatalogFile();

		List<String> queries = getQueryList();

		for (String query : queries) {
			mappedData = new HashMap<>();
			String temp = query.split(" ")[0];
			String queryId = temp.substring(0, temp.indexOf("."));
			String[] queryTokens = queryTokenizer(query);
			queryProximityMap = new LinkedHashMap<>();
			docScoreMap = new LinkedHashMap<>();
			searchEngine(queryTokens);
			scoreDocumentsByProximity(queryId);
			// OKAPI TF MODEL
			// Okapi_TF.okapiTFUtil(mappedData, queryId);

			// BM25
			// BM25_Util.bm25TFUtil(mappedData, queryId);

		}

		// QueryLaplace.laplaceJelinekInvoke();

		return mappedData;
	}

	private static void scoreDocumentsByProximity(String queryNo) {
		for (Entry<String, ArrayList<ProximityEntry>> e : queryProximityMap.entrySet()) {
			//if(e.getValue().size()>1)
				scoreDocument(e.getKey(), e.getValue());
		}

		List<Entry<String, Double>> proximityScoreEntrySet = new ArrayList<>(docScoreMap.entrySet());
		Collections.sort(proximityScoreEntrySet, new Comparator<Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> e1, Entry<String, Double> e2) {
				if (e1.getValue() >= e2.getValue())
					return -1;
				else
					return 1;
			}

		});

/*		if (proximityScoreEntrySet.size() < 5000)
			writeResultsToFile(queryNo, proximityScoreEntrySet);
		else
			writeResultsToFile(queryNo, proximityScoreEntrySet.subList(0, 5000));
*/
		Map<String, Double> documentsForQuery = new LinkedHashMap<>();
		for(Entry<String, Double> pEntry : proximityScoreEntrySet){
			documentsForQuery.put(pEntry.getKey(), pEntry.getValue());
		}
		
		proximityScores.put(Integer.parseInt(queryNo), (LinkedHashMap<String, Double>) documentsForQuery);
		
	}

	private static void writeResultsToFile(String queryNo, List<Entry<String, Double>> proximityScoreEntrySet) {

		int rank = 1;
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Double> e : proximityScoreEntrySet) {
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
			fileWriterUtil(sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void fileWriterUtil(String contentToFile) throws IOException {

		String outputFilePath = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 2/files/output";
		String fileName = "proximity-score.txt";

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

	private static void scoreDocument(String docNo, ArrayList<ProximityEntry> terms) {

		int minSpan = 1;
		if (terms.size() > 1) {
			long[][] scoreArray = new long[terms.size()][];
			for (int i = 0; i < scoreArray.length; i++)
				scoreArray[i] = new long[terms.get(i).getPositions().size()];

			for (int i = 0; i < scoreArray.length; i++) {
				List<Long> positions = terms.get(i).getPositions();
				for (int j = 0; j < scoreArray[i].length; j++) {
					scoreArray[i][j] = positions.get(j);
				}
			}
			minSpan = minimumSpanUtil(scoreArray);
		}
			
		
		double score = computerScore(docNo, minSpan, terms.size());
		docScoreMap.put(docNo, score);

	}

	private static double computerScore(String docNo, int minSpan, int matchingTerms) {
		double docLength = Integer.parseInt(docLengthProp.get(docNo).toString());
		double C = 1500;
		// long V = 166721;

		long V = 177521;
		double numerator = Double.valueOf(C - minSpan);
		double denominator = Double.valueOf(docLength + V);

		double score = (numerator * matchingTerms) / denominator;

		return score;
	}

	private static int minimumSpanUtil(long[][] scoreArray) {

		List<Integer> indexList = new ArrayList<>(scoreArray.length);
		List<Long> currentIndexPositionList = new ArrayList<>();
		for (int i = 0; i < scoreArray.length; i++)
			indexList.add(0);

		int minSpan = Integer.MAX_VALUE;

		while (true) {
			currentIndexPositionList = new ArrayList<>();
			for (int i = 0; i < indexList.size(); i++) {
				if (indexList.get(i) != -1)
					currentIndexPositionList.add((long) scoreArray[i][indexList.get(i)]);
			}

			if (currentIndexPositionList.size() < 2)
				return minSpan;

			int min = minimumSpan(currentIndexPositionList);

			if (min < minSpan)
				minSpan = min;

			int smallestIndex = smallestElement(indexList, scoreArray);

			int val = indexList.get(smallestIndex);
			indexList.set(smallestIndex, ++val);

			if (indexList.get(smallestIndex) >= scoreArray[smallestIndex].length)
				indexList.set(smallestIndex, -1);

		}
	}

	private static int smallestElement(List<Integer> indexList, long[][] scoreArray) {

		long smallest = Integer.MAX_VALUE;
		int smallestIndex = Integer.MAX_VALUE;
		for (int i = 0; i < scoreArray.length; i++) {
			if (indexList.get(i) != -1) {
				if (scoreArray[i][indexList.get(i)] < smallest)
					smallest = scoreArray[i][indexList.get(i)];
				smallestIndex = i;
			}
		}
		return smallestIndex;
	}

	private static int minimumSpan(List<Long> currentIndexPositionList) {

		Collections.sort(currentIndexPositionList);
		int span = 0;
		for (int i = 1; i < currentIndexPositionList.size(); i++)
			span += (currentIndexPositionList.get(i) - currentIndexPositionList.get(i - 1));

		return span;
	}

	private static void searchEngine(String[] tokens) {

		for (String s : tokens) {
			if (stopWordProp.get(s) == null) {
				getDocumentDataForTerm(s.toLowerCase());
			}
		}
	}

	private static void getDocumentDataForTerm(String queryTerm) {
		String stemmedTerm = getStemOfWord(queryTerm);
		getResponseString(stemmedTerm);
	}

	private static void getResponseString(String term) {

		List<DocIndex> invertedListForTerm = getInvertedListForTerm(term);
		if (invertedListForTerm != null) {
			List<DocumentData> docDataList = new ArrayList<>();

			int docFreq = invertedListForTerm.size();

			for (DocIndex dIdx : invertedListForTerm) {
				DocumentData docData = new DocumentData(dIdx.getDocNo(), term, dIdx.getTermFreq(), docFreq);
				docDataList.add(docData);
				if (queryProximityMap.containsKey(dIdx.getDocNo())) {
					List<ProximityEntry> exisitingSet = queryProximityMap.get(dIdx.getDocNo());
					List<Long> pList = new ArrayList<>();
					pList.addAll(dIdx.getPositions());
					exisitingSet.add(new ProximityEntry(term, pList));
					queryProximityMap.put(dIdx.getDocNo(), (ArrayList<ProximityEntry>) exisitingSet);
				} else {
					List<ProximityEntry> newSet = new ArrayList<>();
					List<Long> pList = new ArrayList<>();
					pList.addAll(dIdx.getPositions());
					newSet.add(new ProximityEntry(term, pList));
					queryProximityMap.put(dIdx.getDocNo(), (ArrayList<ProximityEntry>) newSet);
				}
			}
			mappedData.put(term, (ArrayList<DocumentData>) docDataList);
		}
	}

	private static List<DocIndex> getInvertedListForTerm(String term) {

		if (!catalogMap.containsKey(term)) {
			return null;
		}
		long startIndex = catalogMap.get(term).getStartIndex();
		long endIndex = catalogMap.get(term).getEndIndex();
		String invertedListAsString = getTermData(startIndex, endIndex);
		List<DocIndex> invertedList = parseStringToList(invertedListAsString);
		return invertedList;
	}

	private static List<DocIndex> parseStringToList(String invertedListAsString) {

		// System.out.println("T: "+invertedListAsString);
		List<DocIndex> invertedList = new ArrayList<DocIndex>();
		String[] docIndexs = invertedListAsString.trim().split("=")[1].split(";");

		for (String d : docIndexs) {

			d = d.trim();
			String[] parsedInfo = d.split(":");
			String docIdCode = parsedInfo[0];
			long docId = 0;
			if (docIdCode.contains("^")) {
				String split[] = docIdCode.split("\\^");
				docId = (long) ((Long.parseLong(split[0])) * (Math.pow(10, Long.parseLong(split[1]))));
			} else {
				docId = Long.parseLong(parsedInfo[0]);
			}

			// docId = Integer.parseInt(parsedInfo[0]);

			String docNo = idToDocMap.get(docId);
			String positionsUnparsed = parsedInfo[1];
			String[] positionTokens = positionsUnparsed.split(",");
			Set<Long> positions = new LinkedHashSet<>();
			for (String s : positionTokens) {
				positions.add(Long.parseLong(s));
			}
			// String docNo =parsedInfo[0];

			// long termFreq = Long.parseLong(parsedInfo[1]);
			DocIndex docIdx = new DocIndex(docNo, positions);
			invertedList.add(docIdx);
		}

		return invertedList;
	}

	private static String getTermData(long startIndex, long endIndex) {

		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(indexFileName, "rw");
			raf.seek(startIndex);
			String termData = raf.readLine();

			raf.close();
			return termData;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static void parseCatalogFile() {
		String fileName = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 6/data/CatalogFile.txt";

		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(fileName));
			while ((line = reader.readLine()) != null) {
				parseFileEntry(line.trim());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void parseFileEntry(String catalog) {

		String[] termCatalogEntries = catalog.split("=");
		if (termCatalogEntries.length > 1) {
			String term = termCatalogEntries[0];
			long startIndex = Long.parseLong(termCatalogEntries[1].split(",")[0]);
			long endIndex = Long.parseLong(termCatalogEntries[1].split(",")[1]);

			catalogMap.put(term, new CatalogEntry(term, startIndex, endIndex));
		}
	}

	public static List<String> getQueryList() {

		String queryPath = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 6/data/query_desc.51-100.short_proximitySearch.txt";
		BufferedReader reader = null;
		String line = "";
		List<String> queryList = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(queryPath));
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() > 0)
					queryList.add(line.trim());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return queryList;
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

	private static String[] queryTokenizer(String query) {

		String q = query.substring(query.indexOf(".") + 1, query.length() - 1);
		q = q.replaceAll(", ", " ");
		// q=q.substring(0, q.lastIndexOf("."));
		String[] tokens = q.trim().split("\\s");
		return tokens;
	}

}
