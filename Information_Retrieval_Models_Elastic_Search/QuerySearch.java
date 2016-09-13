package assignment_1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

public class QuerySearch {

	final static String INDEX_NAME = "ap_dataset";
	final static String DOC_TYPE = "document";
	static Client client;
	static List<DocumentData> docData;
	static Map<String, ArrayList<DocumentData>> mappedData;
	static Set<DuplicateCheck> duplicateData;
	static Set<String> duplicateDocuments;
	static Properties queryProp;

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

		processDocuments();

		System.out.println("completed");
		date = new Date();
		System.out.println(dateFormat.format(date)); // 2014/08/06 15:59:48

	}

	public static Map<String, ArrayList<DocumentData>> processDocuments() {

		duplicateData = new HashSet<>();
		duplicateDocuments = new HashSet<>();
		// mappedData = new HashMap<>();
		docData = new ArrayList<>();
		try {
			client = getClient();
		} catch (UnknownHostException e) {
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

			// OKAPI TF MODEL
			Okapi_TF.okapiTFUtil(mappedData, queryId);

			// OKAPI IDF MODEL
			// Okapi_IDF.okapiIDFUtil(mappedData, queryId);

			// BM25
			BM25_Util.bm25TFUtil(mappedData, queryId);

		}

		QueryLaplace.laplaceJelinekInvoke();

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

		/*
		 * SearchResponse response = null; response =
		 * getClient().prepareSearch("ap_dataset").setTypes("document")
		 * .setSearchType(SearchType.DEFAULT)
		 * .setQuery(QueryBuilders.matchQuery("text", term)) // Query
		 * .setFrom(0).setExplain(true).execute().actionGet();
		 */

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

		Settings settings = Settings.settingsBuilder().put("client.transport.sniff", false).build();

		client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		return client;
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

		String queryPath = queryProp.get("QUERY_PATH").toString();

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
