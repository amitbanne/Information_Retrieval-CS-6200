package assignment_1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

public class QueryLaplace {

	public QueryLaplace() {
		// TODO Auto-generated constructor stub
	}

	final static String INDEX_NAME = "ap_dataset";
	final static String DOC_TYPE = "document";
	static Client client;
	static List<DocumentData> docData;
	static Map<String, ArrayList<DocumentData>> mappedData;
	static Set<String> duplicateDocuments;
	static Properties queryProp, lengthProp;
	static List<String> allDocumentsList;
	static Map<String,Long> totalTermFreqMap;
	static Map<String,Double> docLengthMap;

	/*	public static void main(String[] args) throws IOException {

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

		processDocuments();

		System.out.println("completed");
		date = new Date();
		System.out.println(dateFormat.format(date)); // 2014/08/06 15:59:48

	}
*/
	
	public static void laplaceJelinekInvoke(){
		
		// TODO Auto-generated method stub
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();

		//System.out.println(dateFormat.format(date)); // 2014/08/06 15:59:48
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

		processDocuments();

		date = new Date();
		//System.out.println(dateFormat.format(date)); // 2014/08/06 15:59:48

	}
	
	public static Map<String, ArrayList<DocumentData>> processDocuments() {

		duplicateDocuments = new HashSet<>();
		totalTermFreqMap = new HashMap<>();
		docLengthMap = new HashMap<>();
		// mappedData = new HashMap<>();
		docData = new ArrayList<>();
		try {
			client = getClient();
			getDocumentLengths();
			//retrieveAllDocuments();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<String> queries = getQueryList();

		for (String query : queries) {
			mappedData = new HashMap<>();
			String temp = query.split(" ")[0];
			String queryId = temp.substring(0, temp.indexOf("."));
			String[] queryTokens = queryTokenizer(query);

			searchEngine(queryTokens);

			// Laplace Smoothing
			LaplaceSmoothing.laplaceUtil(mappedData, queryId);

			// Jelinek-Mercer
			Jelinek_Mercer.jelinekMercerUtil(mappedData, totalTermFreqMap, queryId);
			totalTermFreqMap = new HashMap<>();
		}

		return mappedData;
	}

	private static void searchEngine(String[] tokens) {

		for (String s : tokens) {
			getDocumentDataForTerm(s.toLowerCase());
		}

	}

	private static void getDocumentDataForTerm(String queryTerm) {
		String stemmedTerm = getStemOfWord(queryTerm);
		try {
			getResponseString(stemmedTerm);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void getResponseString(String term) throws UnknownHostException {

		SearchResponse scrollResp = client.prepareSearch(INDEX_NAME).setTypes(DOC_TYPE).setScroll(new TimeValue(60000))
				.setQuery(QueryBuilders.matchQuery("text", term)).setSize(100) // 1000
																				// Query
																				// hits
																				// per
																				// shard
				.setExplain(true).execute().actionGet();

		// scroll

		// Scroll until no hits are returned
		List<DocumentData> docDataList = new ArrayList<>();
		List<String> documentsContainingTerm = new ArrayList<>();
		// duplicateData = new HashSet<>();
		duplicateDocuments = new HashSet<>();

		long totalTermFreq = 0;
		long docFreq = scrollResp.getHits().getTotalHits();
		while (true) {

			for (SearchHit hit : scrollResp.getHits().getHits()) {
				DocumentData docData = parseDataForDocument(term.trim(), hit, docFreq);
				if (!duplicateDocuments.contains(docData.getDocNo())) {
					totalTermFreq+= docData.getTermFreq();
					docDataList.add(docData);
					documentsContainingTerm.add(docData.getDocNo());
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

		Set<String> allDocs = new HashSet<>();
		allDocs.addAll(docLengthMap.keySet());
		allDocs.removeAll(documentsContainingTerm);
		for (String s : allDocs) {
			if(docLengthMap.get(s) > 0)
				docDataList.add(new DocumentData(s, term, 0, docFreq));
		}
		totalTermFreqMap.put(term,totalTermFreq);
		mappedData.put(term, (ArrayList<DocumentData>) docDataList);
	}

	private static DocumentData parseDataForDocument(String term, SearchHit hit, long docFreq) {

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

	private static Client getClient() throws UnknownHostException {

		/*Settings settings = Settings.settingsBuilder().put("client.transport.sniff", false).build();

		client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
*/		
		return QuerySearch.client;
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

	public static List<String> getQueryList() {

		String queryPath = "C:\\Users\\NishantRatnakar\\Desktop\\Lecture Notes\\IR\\AP89_DATA\\AP_DATA\\query_desc.51-100.short.txt";

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
	
	
	public static void getDocumentLengths() {

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
	}

	private static void retrieveAllDocuments() throws IOException {
		
		allDocumentsList = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		SearchResponse scrollResp = client.prepareSearch(INDEX_NAME).setTypes(DOC_TYPE).setScroll(new TimeValue(60000))
				.setQuery(QueryBuilders.matchAllQuery()).setSize(1000)
				.execute().actionGet();

		String prefix = "";
		// Scroll until no hits are returned
		while (true) {

			for (SearchHit hit : scrollResp.getHits().getHits()) {

				String source = hit.getSourceAsString();
				String docNo = parseDocNo(source);
				sb.append(prefix);
				prefix="\n";
				sb.append(docNo);
				allDocumentsList.add(docNo);
			}
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute()
					.actionGet();
			// Break condition: No hits are returned
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}
//		fileWriterUtil(sb.toString());
		
	}
	
}
