package assignment_4;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

public class ElasticSearchInlink {
	private static final String INLINK_FILE = "corpus_inlinks_3.txt";
	private static final String DOCUMENT_DIRECTORY = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 4/";
	private static Client client;
	private static final String ELEMENT_DELIMETER = "$#";
	private static final String INDEX_NAME = "van_dataset";
	private static final String DOC_TYPE = "document";
	private static final String NODE_NAME = "AMIT-NODE";
	private static final String CLUSTER_NAME = "van_cluster";
	private static final String IP_ADDRESS = "localhost";
	private static Map<String, Set<String>> inlinkMap = new HashMap<>();
	private static Map<String, Set<String>> outlinkMap = new HashMap<>();
	private static Set<String> documentsInCorpus = new HashSet<>();
	
	public static void main(String[] args) throws IOException {

		transportClientBuilder();
		scrollSearch();
		linkGraph();
		// dumpInlinkFile();

	}

	private static void linkGraph() {

		for (String doc : documentsInCorpus) {
			Map<String, Object> docData = getIndexedDocument(doc);

			if (null == docData)
				continue;

			updateLinks(doc, docData.get("in_links").toString());
		}
	}

	private static void fileWriterUtil(String content) {
		String inlinkFile = DOCUMENT_DIRECTORY + INLINK_FILE;
		File file = new File(inlinkFile);
		boolean flag = false;
		try {
			// if file doesnt exists, then create it
			if (!file.exists()){
				file.createNewFile();
				flag=true;
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			if(!flag)
				bw.write(System.lineSeparator());
			bw.write(content);
			bw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void dumpInlinkFile() {
		StringBuilder sb = new StringBuilder();
		String endOfLine = "";
		for (Entry<String, Set<String>> e : inlinkMap.entrySet()) {
			sb.append(endOfLine);
			endOfLine = System.lineSeparator();
			sb.append(e.getKey());
			sb.append(ELEMENT_DELIMETER);
			sb.append(inlinksToString(e.getValue()));
		}
		String inlinkFile = DOCUMENT_DIRECTORY + INLINK_FILE;
		File file = new File(inlinkFile);
		try {
			FileWriter fw = new FileWriter(file, true);
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private static String inlinksToString(Set<String> inlinks) {

		StringBuilder sb = new StringBuilder();
		String delimiter = "";
		for (String inlink : inlinks) {
			sb.append(delimiter);
			delimiter = System.lineSeparator();
			sb.append(inlink);
		}

		return sb.toString();
	}

	public static void transportClientBuilder() throws UnknownHostException {
		Settings settings = Settings.settingsBuilder().put("cluster.name", CLUSTER_NAME).put("node.name", NODE_NAME)
				.build();

		client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(IP_ADDRESS), 9300));
	}

	private static void scrollSearch() throws IOException {

		int count = 0;
		SearchResponse scrollResp = client.prepareSearch(INDEX_NAME).setTypes(DOC_TYPE).setScroll(new TimeValue(60000))
				.setQuery(QueryBuilders.matchAllQuery()).setSize(500).execute().actionGet(); // 100
		// hits
		// per
		// shard
		// will
		// be returned for each
		// scroll

		// Scroll until no hits are returned
		while (true) {
			for (SearchHit hit : scrollResp.getHits().getHits()) {
				// Handle the hit...
				// String docNo = parseDocNo(hit.getSourceAsString());
				System.out.println(++count);
				
				//String docno = hit.getId();
				//String inLinksAsString = hit.sourceAsMap().get("in_links").toString();
				//updateLinks(docno, inLinksAsString);
				documentsInCorpus.add(hit.getId());
			}
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute()
					.actionGet();
			// Break condition: No hits are returned
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}

	}

	/*
	 * private static void updateLinks(String docId, String inLinksAsString) {
	 * 
	 * Set<String> inlinks = parseInlinkString(inLinksAsString);
	 * updateInlinkMap(docId, inlinks); //updateOutlinkMap(docId, inlinks);
	 * 
	 * }
	 */

	private static void updateLinks(String docId, String inLinksAsString) {

		String inlinks = getInlinkString(inLinksAsString);
		// updateInlinkMap(docId, inlinks);
		// updateOutlinkMap(docId, inlinks);

		StringBuffer sb = new StringBuffer();
		sb.append(docId);
		sb.append(ELEMENT_DELIMETER);
		sb.append(inlinks);
		
		fileWriterUtil(sb.toString());

	}

	private static String getInlinkString(String inLinksAsString) {

		if (inLinksAsString.equals("[]"))
			return "";

		String links = inLinksAsString.substring(1, inLinksAsString.length() - 1);

		String[] tokens = links.split(",");
		
		StringBuffer inlinkString = new StringBuffer();
		String delimiter = "";
		for (String link : tokens) {
			inlinkString.append(delimiter);
			delimiter = ELEMENT_DELIMETER;
			inlinkString.append(link.trim());
		}

		return inlinkString.toString();
	}

	private static void updateOutlinkMap(String docId, Set<String> inlinks) {

		for (String inlink : inlinks) {
			if (outlinkMap.containsKey(inlink)) {
				Set<String> existingLinks = outlinkMap.get(inlink);
				existingLinks.add(docId);
				outlinkMap.put(inlink, existingLinks);
			} else {
				Set<String> newLinks = new HashSet<>();
				newLinks.add(docId);
				outlinkMap.put(inlink, newLinks);
			}
		}

	}

	private static void updateInlinkMap(String docId, Set<String> inlinks) {

		if (inlinkMap.containsKey(docId)) {
			Set<String> existingLinks = inlinkMap.get(docId);
			existingLinks.addAll(inlinks);
			inlinkMap.put(docId, existingLinks);
		} else {
			Set<String> newLinks = new HashSet<>();
			newLinks.addAll(inlinks);
			inlinkMap.put(docId, newLinks);
		}

	}

	private static Set<String> parseInlinkString(String inLinksAsString) {

		if (inLinksAsString.equals("[]"))
			return new HashSet<String>();

		String links = inLinksAsString.substring(1, inLinksAsString.length() - 1);

		String[] tokens = links.split(",");

		Set<String> inlinks = new HashSet<>();
		for (String link : tokens) {
			inlinks.add(link);
		}

		return inlinks;
	}

	public static Map<String, Object> getIndexedDocument(String id) {

		GetResponse response = client.prepareGet(INDEX_NAME, DOC_TYPE, id).execute().actionGet();
		if (response.isExists())
			return response.getSourceAsMap();
		return null;
	}

}
