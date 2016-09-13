package assignment_6;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import assignment_2.CatalogEntry;
import assignment_2.DocIndex;
import assignment_2.ProximitySearch;

public class IRFeatureData {

	private static Properties stopWordProp;
	private static Map<Long,String> idToDocMap = new LinkedHashMap<>();
	private static Map<Long,String> idToTermMap = new LinkedHashMap<>();
	private static Map<String, CatalogEntry> catalogMap = new HashMap<String, CatalogEntry>();
	private static Map<Integer, HashMap<String, Integer>> qRelData;
	private static List<String> queries;
	private static Map<String, ArrayList<DocumentData>> mappedData;
	private static Map<String, Double> docLengthMap;
	private static Map<String, Long> totalTermFreqMap;
	final static String indexFileName = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 6/data/IndexFile.txt";
	final static String CATALOG_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 6/data/CatalogFile.txt";
	private static Map<Integer, LinkedHashMap<String, ModelScore>> modelScores = new LinkedHashMap<>();
	
	public static void main(String[] args) {

		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		initialize();
		System.out.println("*** Initialization complete ***");
		processQueries();
		System.out.println("*** Processing complete ***");
		dumpResults();
		System.out.println("*** Dumping complete ***");
	}

	private static void dumpResults() {
		
		int sum=0;
		for(Entry<Integer, LinkedHashMap<String, ModelScore>>  e : modelScores.entrySet()){
			sum+=e.getValue().keySet().size();
		}
		
		System.out.println("Total Model: "+sum);
		
		
		StringBuilder dataString = new StringBuilder();
		String delimiter = ""; 
		
		for(Entry<Integer,LinkedHashMap<String, ModelScore>> mEntry : modelScores.entrySet()){
			dataString.append(delimiter);
			delimiter=System.lineSeparator();
			
			Integer qId = mEntry.getKey();
			String del1= "";
			for(Entry<String,ModelScore> dEntry : mEntry.getValue().entrySet()){
				dataString.append(del1);
				del1=System.lineSeparator();
				
				String docId = dEntry.getKey();
				String modelScoreAsString = modelScoresToString(dEntry.getValue());
				
				dataString.append(qId);
				dataString.append(" ");
				dataString.append(docId);
				dataString.append(" ");
				dataString.append(modelScoreAsString);
			}
			
		}
		
		writeToFile(dataString.toString());
		
	}

	private static void writeToFile(String contentToFile) {

		String outputFile = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 6/data/output.txt";

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

	private static String modelScoresToString(ModelScore model) {

		StringBuilder sb = new StringBuilder();
		sb.append(model.getOkapiTF());
		sb.append(" ");
		sb.append(model.getTfIDF());
		sb.append(" ");
		sb.append(model.getBm25());
		sb.append(" ");
		sb.append(model.getJelinek());
		sb.append(" ");
		sb.append(model.getLaplace());
		sb.append(" ");
		sb.append(model.getProximity());
		sb.append(" ");
		sb.append(model.getLabel());
		
		return sb.toString();
	}

	private static void processQueries() {
		
		for (String query : queries) {
			mappedData = new LinkedHashMap<>();
			totalTermFreqMap = new LinkedHashMap<>();
			String temp = query.split(" ")[0];
			String queryId = temp.substring(0, temp.indexOf("."));
			String[] queryTokens = QueryParser.queryTokenizer(query);
			
			processQuery(queryTokens);
			executeIRModels(queryId);
			System.out.println("*** Processing " + queryId+" complete***");
			
		}
			
		updatePromityScores();
		System.out.println("*** Proximity complete ***");
	}
	
	private static void updatePromityScores() {

		
		//fetch proximity scores
		Map<Integer, LinkedHashMap<String, Double>> proximityScores = ProximitySearch.proximityUtil();

		//iterate over each of queries
		for (Entry<Integer, LinkedHashMap<String, Double>> qEntry : proximityScores.entrySet()) {
			//fetch existing data with other model scores
			Map<String, ModelScore> documentScoresForQuery = modelScores.get(qEntry.getKey());
			// for every document obtained from proximity util, 
			// update the corresponding proximity score in modelScores map
			for (Entry<String, Double> dEntry : qEntry.getValue().entrySet()) {
				// if document is present in qrel file, only then update the score
				if (qRelData.get(qEntry.getKey()).containsKey(dEntry.getKey())) {
					if (documentScoresForQuery.containsKey(dEntry.getKey())) {
						ModelScore mScore = documentScoresForQuery.get(dEntry.getKey());
						mScore.setProximity(dEntry.getValue());
						documentScoresForQuery.put(dEntry.getKey(), mScore);
					} else {
						ModelScore mScore = new ModelScore();
						mScore.setProximity(dEntry.getValue());
						documentScoresForQuery.put(dEntry.getKey(), mScore);
					}
				}
			}
			
			// get minimum proximity score
			List<Double> scores = new ArrayList<>(qEntry.getValue().values());
			double defaultProximityScore = scores.get(scores.size()-1);
			//iterate over qRelDocs for query, and
			// if any doc from qRel is not retrieved by model,
			// set a defauly value
			for(Entry<String,Integer> qRelDocEntry : qRelData.get(qEntry.getKey()).entrySet()){
				if(!qEntry.getValue().containsKey(qRelDocEntry.getKey())){
					if (documentScoresForQuery.containsKey(qRelDocEntry.getKey())) {
						ModelScore mScore = documentScoresForQuery.get(qRelDocEntry.getKey());
						mScore.setProximity(defaultProximityScore);
     					documentScoresForQuery.put(qRelDocEntry.getKey(), mScore);
					} else {
						ModelScore mScore = new ModelScore();
						mScore.setProximity(defaultProximityScore);
						mScore.setLabel(qRelDocEntry.getValue());
						documentScoresForQuery.put(qRelDocEntry.getKey(), mScore);
					}
				}
			}
			
			modelScores.put(qEntry.getKey(), (LinkedHashMap<String, ModelScore>) documentScoresForQuery);
			
		}

	}
	
	private static void executeIRModels(String queryId) {
 
		// Okapi TF
		List<DocRank> rankedOkapiTFDocs = Okapi_TF.okapiTFUtil(mappedData, queryId);
		updateScoreMap(queryId, rankedOkapiTFDocs, "OKAPI_TF");
		// TF-IDF
		List<DocRank> rankedTFIDFDocs = Okapi_IDF.okapiIDFUtil(mappedData, queryId);
		updateScoreMap(queryId, rankedTFIDFDocs, "TF_IDF");

		// BM-25
		List<DocRank> rankedBM25Docs = BM25_Util.bm25TFUtil(mappedData, queryId);
		updateScoreMap(queryId, rankedBM25Docs, "BM_25");

		// Laplace Smoothing
		List<DocRank> rankedLaplaceDocs = LaplaceSmoothing.laplaceUtil(mappedData, queryId);
		updateScoreMap(queryId, rankedLaplaceDocs, "LAPLACE");

		// Jelinek Mercer
		List<DocRank> rankedJelinekDocs = Jelinek_Mercer.jelinekMercerUtil(mappedData, totalTermFreqMap, queryId);
		updateScoreMap(queryId, rankedJelinekDocs, "JELINEK");
		
		
	}

	private static void updateScoreMap(String queryId, List<DocRank> rankedDocs, String model) {

		Integer qID = Integer.parseInt(queryId);
		Set<String> tmpDocSet = new LinkedHashSet<>();
		Map<String, ModelScore> documentScoresForQuery = new LinkedHashMap<>();
		if (modelScores.containsKey(qID)) 
			documentScoresForQuery = modelScores.get(qID);

		switch (model) {
		case "OKAPI_TF":
			for (DocRank dRank : rankedDocs) {
				if (qRelData.get(qID).containsKey(dRank.getDocNo())) {
					if (documentScoresForQuery.containsKey(dRank.getDocNo())) {
						ModelScore mScore = documentScoresForQuery.get(dRank.getDocNo());
						mScore.setOkapiTF(dRank.getScore());
						documentScoresForQuery.put(dRank.getDocNo(), mScore);
					} else {
						ModelScore mScore = new ModelScore();
						mScore.setOkapiTF(dRank.getScore());
						mScore.setLabel(qRelData.get(qID).get(dRank.getDocNo()));
						documentScoresForQuery.put(dRank.getDocNo(), mScore);
					}
				}
			}
			
			for(DocRank doc : rankedDocs){
				tmpDocSet.add(doc.getDocNo());
			}
			double defaultOkapiScore = rankedDocs.get(rankedDocs.size()-1).getScore();
			for(Entry<String,Integer> qRelEntry : qRelData.get(qID).entrySet()){
				if(!tmpDocSet.contains(qRelEntry.getKey())){
					if (documentScoresForQuery.containsKey(qRelEntry.getKey())) {
						ModelScore mScore = documentScoresForQuery.get(qRelEntry.getKey());
						mScore.setOkapiTF(defaultOkapiScore);
						documentScoresForQuery.put(qRelEntry.getKey(), mScore);
					} else {
						ModelScore mScore = new ModelScore();
						mScore.setOkapiTF(defaultOkapiScore);
						mScore.setLabel(qRelEntry.getValue());
						documentScoresForQuery.put(qRelEntry.getKey(), mScore);
					}
				}
			}

			modelScores.put(qID, (LinkedHashMap<String, ModelScore>) documentScoresForQuery);
			break;

		case "TF_IDF":
			for (DocRank dRank : rankedDocs) {
				if (qRelData.get(qID).containsKey(dRank.getDocNo())) {
					if (documentScoresForQuery.containsKey(dRank.getDocNo())) {
						ModelScore mScore = documentScoresForQuery.get(dRank.getDocNo());
						mScore.setTfIDF(dRank.getScore());
						documentScoresForQuery.put(dRank.getDocNo(), mScore);
					} else {
						ModelScore mScore = new ModelScore();
						mScore.setTfIDF(dRank.getScore());
						mScore.setLabel(qRelData.get(qID).get(dRank.getDocNo()));
						documentScoresForQuery.put(dRank.getDocNo(), mScore);
					}
				}
			}
			
			for(DocRank doc : rankedDocs){
				tmpDocSet.add(doc.getDocNo());
			}
			
			double defaultTFIDFcore = rankedDocs.get(rankedDocs.size()-1).getScore();
			for(Entry<String,Integer> qRelEntry : qRelData.get(qID).entrySet()){
				if(!tmpDocSet.contains(qRelEntry.getKey())){
					if (documentScoresForQuery.containsKey(qRelEntry.getKey())) {
						ModelScore mScore = documentScoresForQuery.get(qRelEntry.getKey());
						mScore.setTfIDF(defaultTFIDFcore);
						documentScoresForQuery.put(qRelEntry.getKey(), mScore);
					} else {
						ModelScore mScore = new ModelScore();
						mScore.setTfIDF(defaultTFIDFcore);
						mScore.setLabel(qRelData.get(qID).get(qRelEntry.getKey()));
						documentScoresForQuery.put(qRelEntry.getKey(), mScore);
					}
				}
				
			}
		
			modelScores.put(qID, (LinkedHashMap<String, ModelScore>) documentScoresForQuery);
			break;

		case "BM_25":
			for (DocRank dRank : rankedDocs) {
				if (qRelData.get(qID).containsKey(dRank.getDocNo())) {
					if (documentScoresForQuery.containsKey(dRank.getDocNo())) {
						ModelScore mScore = documentScoresForQuery.get(dRank.getDocNo());
						mScore.setBm25(dRank.getScore());
						documentScoresForQuery.put(dRank.getDocNo(), mScore);
					} else {
						ModelScore mScore = new ModelScore();
						mScore.setBm25(dRank.getScore());
						mScore.setLabel(qRelData.get(qID).get(dRank.getDocNo()));
						documentScoresForQuery.put(dRank.getDocNo(), mScore);
					}
				}
			}
			
			for(DocRank doc : rankedDocs){
				tmpDocSet.add(doc.getDocNo());
			}
			double defaultBM25core = rankedDocs.get(rankedDocs.size()-1).getScore();
			for(Entry<String,Integer> qRelEntry : qRelData.get(qID).entrySet()){
				if(!tmpDocSet.contains(qRelEntry.getKey())){
					if (documentScoresForQuery.containsKey(qRelEntry.getKey())) {
						ModelScore mScore = documentScoresForQuery.get(qRelEntry.getKey());
						mScore.setBm25(defaultBM25core);
						documentScoresForQuery.put(qRelEntry.getKey(), mScore);
					} else {
						ModelScore mScore = new ModelScore();
						mScore.setBm25(defaultBM25core);
						mScore.setLabel(qRelData.get(qID).get(qRelEntry.getKey()));
						documentScoresForQuery.put(qRelEntry.getKey(), mScore);
					}
				}
				
			}
		
			modelScores.put(qID, (LinkedHashMap<String, ModelScore>) documentScoresForQuery);
			break;

		case "JELINEK":
			for (DocRank dRank : rankedDocs) {
				if (qRelData.get(qID).containsKey(dRank.getDocNo())) {
					if (documentScoresForQuery.containsKey(dRank.getDocNo())) {
						ModelScore mScore = documentScoresForQuery.get(dRank.getDocNo());
						mScore.setJelinek(dRank.getScore());
						documentScoresForQuery.put(dRank.getDocNo(), mScore);
					} else {
						ModelScore mScore = new ModelScore();
						mScore.setJelinek(dRank.getScore());
						mScore.setLabel(qRelData.get(qID).get(dRank.getDocNo()));
						documentScoresForQuery.put(dRank.getDocNo(), mScore);
					}
				}
			}
			
			for(DocRank doc : rankedDocs){
				tmpDocSet.add(doc.getDocNo());
			}
			double defaultJelinekScore = rankedDocs.get(rankedDocs.size()-1).getScore();
			for(Entry<String,Integer> qRelEntry : qRelData.get(qID).entrySet()){
				if(!tmpDocSet.contains(qRelEntry.getKey())){
					if (documentScoresForQuery.containsKey(qRelEntry.getKey())) {
						ModelScore mScore = documentScoresForQuery.get(qRelEntry.getKey());
						mScore.setJelinek(defaultJelinekScore);
						documentScoresForQuery.put(qRelEntry.getKey(), mScore);
					} else {
						ModelScore mScore = new ModelScore();
						mScore.setJelinek(defaultJelinekScore);
						mScore.setLabel(qRelData.get(qID).get(qRelEntry.getKey()));
						documentScoresForQuery.put(qRelEntry.getKey(), mScore);
					}
				}
				
			}
			modelScores.put(qID, (LinkedHashMap<String, ModelScore>) documentScoresForQuery);
			break;

		case "LAPLACE":
			for (DocRank dRank : rankedDocs) {
				if (qRelData.get(qID).containsKey(dRank.getDocNo())) {
					if (documentScoresForQuery.containsKey(dRank.getDocNo())) {
						ModelScore mScore = documentScoresForQuery.get(dRank.getDocNo());
						mScore.setLaplace(dRank.getScore());
						documentScoresForQuery.put(dRank.getDocNo(), mScore);
					} else {
						ModelScore mScore = new ModelScore();
						mScore.setLaplace(dRank.getScore());
						mScore.setLabel(qRelData.get(qID).get(dRank.getDocNo()));
						documentScoresForQuery.put(dRank.getDocNo(), mScore);
					}
				}
			}
			
			for(DocRank doc : rankedDocs){
				tmpDocSet.add(doc.getDocNo());
			}
			
			double defaultLaplaceScore = rankedDocs.get(rankedDocs.size()-1).getScore();
			for(Entry<String,Integer> qRelEntry : qRelData.get(qID).entrySet()){
				if(!tmpDocSet.contains(qRelEntry.getKey())){
					if (documentScoresForQuery.containsKey(qRelEntry.getKey())) {
						ModelScore mScore = documentScoresForQuery.get(qRelEntry.getKey());
						mScore.setLaplace(defaultLaplaceScore);
						mScore.setLabel(qRelData.get(qID).get(qRelEntry.getKey()));
						documentScoresForQuery.put(qRelEntry.getKey(), mScore);
					} else {
						ModelScore mScore = new ModelScore();
						mScore.setLaplace(defaultLaplaceScore);
						mScore.setLabel(qRelData.get(qID).get(qRelEntry.getKey()));
						documentScoresForQuery.put(qRelEntry.getKey(), mScore);
					}
				}
				
			}
			
			modelScores.put(qID, (LinkedHashMap<String, ModelScore>) documentScoresForQuery);
			break;

		}

	}
	

	private static void processQuery(String[] tokens) {

		for (String s : tokens) {
			if (stopWordProp.get(s) == null) {
				getDocumentDataForTerm(s.toLowerCase());
			}
		}
	}

	private static void getDocumentDataForTerm(String queryTerm) {
		String stemmedTerm = Util.getStemOfWord(queryTerm);
		getResponseString(stemmedTerm);
	}

	
	private static void getResponseString(String term) {

		List<DocIndex> invertedListForTerm = getInvertedListForTerm(term);
		if (invertedListForTerm != null) {
			List<DocumentData> docDataList = new ArrayList<>();
			List<String> documentsContainingTerm = new ArrayList<>();
			int docFreq = invertedListForTerm.size();
			long totalTermFreq = 0;
			for (DocIndex dIdx : invertedListForTerm) {
				totalTermFreq += dIdx.getTermFreq();
				documentsContainingTerm.add(dIdx.getDocNo());
				DocumentData docData = new DocumentData(dIdx.getDocNo(), term, dIdx.getTermFreq(), docFreq);
				docDataList.add(docData);
			}
			Set<String> allDocs = new HashSet<>();
			allDocs.addAll(docLengthMap.keySet());
			allDocs.removeAll(documentsContainingTerm);
			for (String s : allDocs) {
				if (docLengthMap.get(s) > 0)
					docDataList.add(new DocumentData(s, term, 0, docFreq));
			}
			totalTermFreqMap.put(term, totalTermFreq);
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

	private static void initialize() {
		stopWordProp = loadStopWordProperties();
		qRelData = QRelParser.parseQRel();
		
		int sum=0;
		for(Entry<Integer, HashMap<String, Integer>>  e : qRelData.entrySet()){
			sum+=e.getValue().keySet().size();
		}
		
		System.out.println("Total qRel: "+sum);
		queries = QueryParser.getQueryList();
		docLengthMap = Util.getDocumentLengths();
		mapTermIdsFromFile();
		mapDocumentIdsFromFile();
		parseCatalogFile();

	}

	public static void parseCatalogFile() {
		String fileName = CATALOG_FILE;

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

	private static Properties loadStopWordProperties() {

		Properties sWProp = new Properties();
		try {
			sWProp.load(new FileInputStream("stoplist.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sWProp;
	}


	private static void mapDocumentIdsFromFile() {

		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader("./DocumentIDs.txt"));
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
			reader = new BufferedReader(new FileReader("./TermIDs.txt"));
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

}
