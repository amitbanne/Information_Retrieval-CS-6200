package assignment_5;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

public class DocumentRetrieval {

	private static Client client;
	private static final String INDEX_NAME = "van_dataset";
	private static final String DOC_TYPE = "document";
	private static final String IP_ADDRESS = "localhost"	;
	private static Map<Long, Set<String>> queryDocumentMap = new LinkedHashMap<>();
	private static Map<String, Long> queryIdMap = new LinkedHashMap<>();
	private static final String rankedFileName = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 5/result_files/RankFile.txt";
	public static void main(String[] args) {

		try {
			transportClientBuilder();
			setupQueries();
			processQueries();
			createRankedFile();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void createRankedFile() {
		StringBuffer sb = new StringBuffer();
		String newLine = "";
		for (Entry<Long, Set<String>> e : queryDocumentMap.entrySet()) {

			for (String doc : e.getValue()) {
				sb.append(newLine);
				newLine = System.lineSeparator();
				sb.append(e.getKey());
				sb.append(" ");
				sb.append(doc);
			}
		}

		writeToFileUtil(sb.toString());
	}

	private static void writeToFileUtil(String content) {
		
		File file = new File(rankedFileName);
		try {
			// if file doesnt exists, then create it
			if (!file.exists())
				file.createNewFile();

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void processQueries() throws IOException {

		for (String query : queryIdMap.keySet()){
			System.out.println("Processing query: "+query);
			retrieveDocuments(query);
		}

	}

	private static void setupQueries() {

		queryIdMap.put("what caused world war 2", 151801L);
		queryIdMap.put("united states battles won in ww2", 151802L);
		queryIdMap.put("battle of stalingrad", 151803L);

	}

	public static void transportClientBuilder() throws UnknownHostException {
		Settings settings = Settings.settingsBuilder().build();

		client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(IP_ADDRESS), 9300));
	}

	private static void retrieveDocuments(String query) throws IOException {
		int count = 0;

		SearchResponse scrollResp = client.prepareSearch(INDEX_NAME).setTypes(DOC_TYPE).setScroll(new TimeValue(60000))
				.setQuery(QueryBuilders.matchQuery("text", query)).setSize(500).execute().actionGet(); // 100
		// hits
		// per
		// shard
		// will
		// be returned for each
		// scroll

		Set<String> documentsForQuery = new LinkedHashSet<>();
		long queryId = queryIdMap.get(query);
		// Scroll until no hits are returned
		while (true) {
			for (SearchHit hit : scrollResp.getHits().getHits()) {
				// Handle the hit...

				String doc = hit.getId();

/*				if (doc.contains("File:") || (!doc.contains("http")))
					continue;
*/
				documentsForQuery.add(doc);
				count++;
				// System.out.println(hit.getId());
				if (count >= 200) {
					queryDocumentMap.put(queryId, documentsForQuery);
					return;
				}
			}
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute()
					.actionGet();
			// Break condition: No hits are returned
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}

	}

}
