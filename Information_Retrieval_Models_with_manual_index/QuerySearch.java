package assignment_2;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.tartarus.snowball.ext.PorterStemmer;


public class QuerySearch {

	final static String INDEX_NAME = "ap_dataset";
	final static String DOC_TYPE = "document";
	static Client client;
	static List<DocumentData> docData;
	static Map<String, ArrayList<DocumentData>> mappedData;
	static Properties queryProp,stopWordProp;
	static Map<String, CatalogEntry> catalogMap = new HashMap<String, CatalogEntry>();
	final static String indexFileName = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 2/files/IndexFile.txt";
	static Map<Long,String> idToDocMap = new LinkedHashMap<>();
	static Map<Long,String> idToTermMap = new LinkedHashMap<>();
	
	public static void main(String[] args) throws UnknownHostException {
		// TODO Auto-generated method stub
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();

		System.out.println(dateFormat.format(date)); // 2014/08/06 15:59:48
		queryProp = new Properties();
		try {
			queryProp.load(new FileInputStream("FileData.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		stopWordProp = new Properties();
		try {
			stopWordProp.load(new FileInputStream("stoplist.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		processDocuments();
		System.out.println("completed");
		date = new Date();
		System.out.println(dateFormat.format(date)); // 2014/08/06 15:59:48

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

			searchEngine(queryTokens);

			// OKAPI TF MODEL
			Okapi_TF.okapiTFUtil(mappedData, queryId);

			// BM25
			BM25_Util.bm25TFUtil(mappedData, queryId);
		
		}

		QueryLaplace.laplaceJelinekInvoke();

		return mappedData;
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
	
	
	private static void getResponseString(String term){
		
		List<DocIndex> invertedListForTerm = getInvertedListForTerm(term);
		if(invertedListForTerm!=null){
			List<DocumentData> docDataList = new ArrayList<>();
			
			int docFreq = invertedListForTerm.size();
			
			for(DocIndex dIdx : invertedListForTerm){
				DocumentData docData = new DocumentData(dIdx.getDocNo(), term, dIdx.getTermFreq(), docFreq);
				docDataList.add(docData);
			}
			
			mappedData.put(term, (ArrayList<DocumentData>) docDataList);
		}
	}
	
	
	private static List<DocIndex> getInvertedListForTerm(String term) {

		if(!catalogMap.containsKey(term)){
			System.out.println(term);
			return null;
		}
		long startIndex = catalogMap.get(term).getStartIndex();
		long endIndex = catalogMap.get(term).getEndIndex();
		String invertedListAsString = getTermData(startIndex,endIndex);
		List<DocIndex> invertedList = parseStringToList(invertedListAsString);
		return invertedList;
	}

	private static List<DocIndex> parseStringToList(String invertedListAsString) {

		//System.out.println("T: "+invertedListAsString);
		List<DocIndex> invertedList = new ArrayList<DocIndex>();
		String[] docIndexs = invertedListAsString.trim().split("=")[1].split(";");

		for (String d : docIndexs) {

			d = d.trim();
			String[] parsedInfo = d.split(":");
			String docIdCode =parsedInfo[0];
			long docId = 0;
			if(docIdCode.contains("^")){
				String split[] = docIdCode.split("\\^");
				docId = (long) ((Long.parseLong(split[0]))*(Math.pow(10, Long.parseLong(split[1]))));
			}else{
				docId = Long.parseLong(parsedInfo[0]);
			}
			
			//docId = Integer.parseInt(parsedInfo[0]);
			
			String docNo = idToDocMap.get(docId);
			String positionsUnparsed = parsedInfo[1];
			String[] positionTokens = positionsUnparsed.split(",");
			Set<Long> positions = new LinkedHashSet<>();
			for(String s: positionTokens){
				positions.add(Long.parseLong(s));
			}
			//String docNo =parsedInfo[0];
			
			//long termFreq = Long.parseLong(parsedInfo[1]);
			DocIndex docIdx = new DocIndex(docNo,positions);
			invertedList.add(docIdx);
		}

		return invertedList;
		
	}


	private static String getTermData(long startIndex, long endIndex) {

		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(indexFileName,"rw");
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
		String fileName = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 2/files/CatalogFile.txt";

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
			long startIndex = Long
					.parseLong(termCatalogEntries[1].split(",")[0]);
			long endIndex = Long.parseLong(termCatalogEntries[1].split(",")[1]);

			catalogMap.put(term, new CatalogEntry(term, startIndex, endIndex));
		}
	}

	/*private static void getResponseString(String term) throws UnknownHostException {

		
		 * SearchResponse response = null; response =
		 * getClient().prepareSearch("ap_dataset").setTypes("document")
		 * .setSearchType(SearchType.DEFAULT)
		 * .setQuery(QueryBuilders.matchQuery("text", term)) // Query
		 * .setFrom(0).setExplain(true).execute().actionGet();
		 

		SearchResponse scrollResp = client.prepareSearch(INDEX_NAME).setTypes(DOC_TYPE).setScroll(new TimeValue(60000))
				.setQuery(QueryBuilders.matchQuery("text", term)).setSize(100) // 100
																				// Query
																				// hits
																				// per
																				// shard
				.setExplain(true).execute().actionGet();

		// scroll

		// Scroll until no hits are returned
		List<DocumentData> docDataList = new ArrayList<>();
		// duplicateData = new HashSet<>();
		duplicateDocuments = new HashSet<>();
		long docFreq = scrollResp.getHits().getTotalHits();
		while (true) {
			for (SearchHit hit : scrollResp.getHits().getHits()) {
				DocumentData docData = parseDataForDocument(term.trim(), hit, docFreq);

					if (((int) docFreq) == 0)
						System.out.println(docData.getTermFreq());

					if (!duplicateDocuments.contains(docData.getDocNo())) {
						docDataList.add(docData);
						duplicateDocuments.add(docData.getDocNo());
					}
			}
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute()
					.actionGet();
			// Break condition: No hits are returned
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}

		mappedData.put(term, (ArrayList<DocumentData>) docDataList);
	}

*/	private static DocumentData parseDataForDocument(String term, SearchHit hit, long docFreq) {

		String docNo = parseDocNo(hit.getSourceAsString());
		// System.out.println(docNo);
		long termFreq = parseTermFreq(hit.getExplanation().toHtml());
		DocumentData docData = new DocumentData(docNo, term, termFreq, docFreq);
		return docData;

	}

	private static long parseTermFreq(String explanation) {

		String t = explanation.split("termFreq=")[1];
		long tf = (long) Double.parseDouble(t.substring(0, t.indexOf("<")));
		return tf;
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
		String[] tokens = q.trim().split(" ");
		return tokens;
	}

	private static String parseDocNo(String sourceAsString) {
		String docNo = sourceAsString.split(",")[0].split(":")[1];
		docNo = docNo.replaceAll("\"", "");
		return docNo;
	}

	public static void processQuery(String query) {

		String s = query.split(" ")[0];
		String queryId = s.substring(0, s.indexOf("."));
		String q = query.substring(query.indexOf(".") + 1, query.length() - 1);
		q = q.replaceAll(", ", " ");
		// q=q.substring(0, q.lastIndexOf("."));
		String[] tokens = q.trim().split(" ");

	}

	public static List<String> getQueryList() {

		String queryPath = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 2/files/query_desc.51-100.short.txt";

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

}
