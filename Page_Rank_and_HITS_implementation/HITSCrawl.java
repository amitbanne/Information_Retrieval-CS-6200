package assignment_4;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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

public class HITSCrawl {

	private static Client client;
	private static final String QUERY = "world war 2";
	private static final String INDEX_NAME = "van_dataset";
	private static final String DOC_TYPE = "document";
	private static final String NODE_NAME = "AMIT-NODE";
	private static final String CLUSTER_NAME = "van_cluster";
	private static final String IP_ADDRESS = "localhost";
	private static final int INLINK_CAP = 50;
	private static Set<String> rootSetPages = new LinkedHashSet<>();
	private static Set<String> baseSetPages = new LinkedHashSet<>();
	
	private static Map<String, Double> hubScore = new HashMap<>();
	private static Map<String, Double> authorityScore = new HashMap<>();
	
	private static Map<String, Double> prevhubScore = new HashMap<>();
	private static Map<String, Double> prevauthorityScore = new HashMap<>();
	
	private static final double EPSILON = 0.000001;
	private static Map<String, Set<String>> inlinkMap = new HashMap<>();
	private static Map<String, Set<String>> outlinkMap = new HashMap<>();
	
	private static final String HUB_FILE = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 4/HUBS.txt";
	private static final String AUTHORITY_FILE = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 4/AUTHORITY.txt";
	public static void main(String[] args) throws IOException {

		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		transportClientBuilder();
		
		System.out.println("Root set start");
		prepareRootSet();
		System.out.println("Root set end");
		
		System.out.println("Base set start");
		prepareBaseSet();
		System.out.println("Base set end");
		
		System.out.println("base set size: "+ baseSetPages.size());
		
		initializeAuthorityScores();
		initializeHubScores();
		scoreHubsAndAuthorities();
		writeHubsToFile();
		writeAuthoritiesToFile();
		client.close();
	}

	
	private static void writeAuthoritiesToFile() {

		List<Entry<String, Double>> authEntryList = new ArrayList<>(authorityScore.entrySet());
		Collections.sort(authEntryList, new Comparator<Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				if(o1.getValue()-o2.getValue() >= 0)
					return -1;
				else
					return 1;

			}
		});
		
		List<Entry<String, Double>> top500List = authEntryList.subList(0, 501);
		StringBuffer sb = new StringBuffer();
		String newLine = "";
		for(Entry<String, Double> e : top500List){
			sb.append(newLine);
			newLine = System.lineSeparator();
			sb.append(e.getKey());
			sb.append("\t");
			sb.append(e.getValue());
		}
		writeToFileUtil(AUTHORITY_FILE, sb.toString());
	}

	private static void writeToFileUtil(String fileName, String content) {
		
		File file = new File(fileName);
		// if file doesnt exists, then create it
					
						try {
							if (!file.exists()) 
								file.createNewFile();
							
							FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
							BufferedWriter bw = new BufferedWriter(fw);
							bw.write(content);
							bw.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	}


	private static void writeHubsToFile() {
		
		List<Entry<String, Double>> hubEntryList = new ArrayList<>(hubScore.entrySet());
		Collections.sort(hubEntryList, new Comparator<Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				if(o1.getValue()-o2.getValue() >= 0)
					return -1;
				else
					return 1;
			}
		});
		
		List<Entry<String, Double>> top500List = hubEntryList.subList(0, 501);
		StringBuffer sb = new StringBuffer();
		String newLine = "";
		for(Entry<String, Double> e : top500List){
			sb.append(newLine);
			newLine = System.lineSeparator();
			sb.append(e.getKey());
			sb.append("\t");
			sb.append(e.getValue());
		}
		writeToFileUtil(HUB_FILE, sb.toString());
	}


	private static void scoreHubsAndAuthorities() {

		int count = 0;
		do{
			copyScores();
			System.out.println(++count);
			for(String link : baseSetPages){
				updateAuthorityScoreForPage(link);
				updateHubScoreForPage(link);
			}
			
			normaliseAuthorityScoreForPages();
			normaliseHubScoreForPages();
			
		}while(!converged());
		
	}

	private static void copyScores() {

		prevauthorityScore.clear();
		prevauthorityScore.putAll(authorityScore);
		
		prevhubScore.clear();
		prevhubScore.putAll(hubScore);
		
	}


	private static void normaliseHubScoreForPages() {

		//System.out.println("Normalize hub score started");
		double normalizationFactor = 0.0;
		
		for(Double hubScore : hubScore.values())
			normalizationFactor+= (hubScore * hubScore);
		
		normalizationFactor = Math.sqrt(normalizationFactor);
		Map<String, Double> tmpHubMap = new HashMap<>();
		for(Entry<String, Double> e : hubScore.entrySet()){
			Double newHubScore = (e.getValue()/normalizationFactor);
			tmpHubMap.put(e.getKey(), newHubScore);
		}
		
		hubScore.clear();
		hubScore.putAll(tmpHubMap);
		//System.out.println("Normalize hub score complete");
		
	}

	private static void normaliseAuthorityScoreForPages() {

		//System.out.println("Normalize auth score started");
		double normalizationFactor = 0.0;
		
		for(Double authScore : authorityScore.values())
			normalizationFactor+= (authScore * authScore);
		
		
		normalizationFactor = Math.sqrt(normalizationFactor);
		Map<String, Double> tmpAuthorityMap = new HashMap<>();
		for(Entry<String, Double> e : authorityScore.entrySet()){
			Double newAuthScore = (e.getValue()/normalizationFactor);
			tmpAuthorityMap.put(e.getKey(), newAuthScore);
		}
		
		authorityScore.clear();
		authorityScore.putAll(tmpAuthorityMap);
		//System.out.println("Normalize auth score complete");
	}

	private static void updateHubScoreForPage(String link) {

		//System.out.println("Hub score update started for "+link);
		
		Set<String> inlinksForPage = inlinkMap.get(link);
		if(null==inlinksForPage){
			/*inlinksForPage = fetchInLinksForPage(link);
			updateInLinkMap(link, inlinksForPage);*/
			return;
		}
		
		Double hubScoreForPage = 0.0;
		
		// save previous score
		//prevhubScore.put(link, hubScore.get(link));
		for(String inlink: inlinksForPage){
			if(baseSetPages.contains(inlink)){
				hubScoreForPage+=prevauthorityScore.get(inlink);
			}
		}
		hubScore.put(link, hubScoreForPage);
		//System.out.println("Hub score update completed for "+link);
	}


	private static void updateAuthorityScoreForPage(String link) {
		
		//System.out.println("Authroity score update started for "+link);
		Set<String> outlinksForPage = outlinkMap.get(link);
		if(null==outlinksForPage){
			return;
		}
		
		Double authorityScoreForPage = 0.0;
		
		// save previous score
		//prevauthorityScore.put(link, authorityScore.get(link));
		
		for(String outlink: outlinksForPage){
			if(baseSetPages.contains(outlink)){
				authorityScoreForPage+=prevhubScore.get(outlink);
			}
		}
		authorityScore.put(link, authorityScoreForPage);
		//System.out.println("Authroity score update complete for "+link);
	}

	
	private static boolean converged() {

		for(Entry<String, Double> e : authorityScore.entrySet()){
			double prevScore = prevauthorityScore.get(e.getKey());
				if(Math.abs((e.getValue() - prevScore))> EPSILON)
					return false;
		}

		for(Entry<String, Double> e : hubScore.entrySet()){
			double prevScore = prevhubScore.get(e.getKey());
				if(Math.abs((e.getValue() - prevScore)) > EPSILON)
					return false;
		}
		
		return true;
	}

	private static void initializeHubScores() {

		for(String link : baseSetPages){
				hubScore.put(link, 1.0);
//				prevhubScore.put(link, 0.0);
		}
	}

	private static void initializeAuthorityScores() {

		for(String link : baseSetPages){
			authorityScore.put(link, 1.0);
//			prevauthorityScore.put(link, 0.0);
		}
	}


	private static void prepareBaseSet() {

		baseSetPages.addAll(rootSetPages);
		for(String page : rootSetPages){
			extractLinksForPage(page);
		}
	}


	private static void extractLinksForPage(String id) {

		GetResponse response = client.prepareGet(INDEX_NAME, DOC_TYPE, id).execute().actionGet();
		if(response!=null){
			
			Map<String, Object> existingData = response.getSourceAsMap();
			
			String outlinksAsString = null;
			if(null != existingData.get("out_links"))
				outlinksAsString = existingData.get("out_links").toString();
			
			String inlinksAsString = null;
			if(null != existingData.get("in_links"))
				inlinksAsString =existingData.get("in_links").toString();
			
			Set<String> outlinks = new LinkedHashSet<>();
			if(null!= outlinksAsString)
				outlinks = parseLinksFromString(outlinksAsString);
			
			baseSetPages.addAll(outlinks);
			updateOutLinkMap(id, outlinks);
			
			Set<String> tmp = new LinkedHashSet<>();
			tmp.add(id);
			for(String link:outlinks){
				updateInLinkMap(link, tmp);
			}
			
			Set<String> inlinks = new LinkedHashSet<>();
			if(null!= inlinksAsString)
				inlinks = parseLinksFromString(inlinksAsString);
			
			if(inlinks.size() > INLINK_CAP){
				List<String> tmpList = new ArrayList<>(inlinks).subList(0,(INLINK_CAP+1));
				baseSetPages.addAll(tmpList);
				updateInLinkMap(id, new LinkedHashSet<>(tmpList));
				
				for(String link:tmpList){
					updateOutLinkMap(link, tmp);
				}
				
			}else{
				baseSetPages.addAll(inlinks);
				updateInLinkMap(id, inlinks);
				
				for(String link:inlinks){
					updateOutLinkMap(link, tmp);
				}
			}
		}
	}
	
	private static void updateInLinkMap(String id, Set<String> inlinks) {

		if(inlinkMap.containsKey(id)){
			Set<String> existingInLinks = inlinkMap.get(id);
			existingInLinks.addAll(inlinks);
			inlinkMap.put(id, existingInLinks);
		}else{
			Set<String> newInLinks = new HashSet<>();
			newInLinks.addAll(inlinks);
			inlinkMap.put(id, newInLinks);
		}
	}


	private static void updateOutLinkMap(String id, Set<String> outlinks) {
		
		if(outlinkMap.containsKey(id)){
			Set<String> existingOutLinks = outlinkMap.get(id);
			existingOutLinks.addAll(outlinks);
			outlinkMap.put(id, existingOutLinks);
		}else{
			Set<String> newOutLinks = new HashSet<>();
			newOutLinks.addAll(outlinks);
			outlinkMap.put(id, newOutLinks);
		}
	}


	private static Set<String> parseLinksFromString(String linksAsString) {
		
		Set<String> links = new LinkedHashSet<>();
		if (null == linksAsString || linksAsString.equals("[]"))
			return links;

		String trimmedlinks = linksAsString.substring(1, linksAsString.length() - 1);

		String[] tokens = trimmedlinks.split(",");
		
		for (String link : tokens) {
			if(link.contains("http") && (!link.contains("File:")))
				links.add(link.trim());
		}
		
		return links;
	}


	public static void transportClientBuilder() throws UnknownHostException {
		Settings settings = Settings.settingsBuilder().put("cluster.name", CLUSTER_NAME).put("node.name", NODE_NAME)
				.build();

		client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(IP_ADDRESS), 9300));
	}
	
	private static void prepareRootSet() throws IOException {

		int count = 0;
		
		SearchResponse scrollResp = client.prepareSearch(INDEX_NAME).setTypes(DOC_TYPE).setScroll(new TimeValue(60000))
				.setQuery(QueryBuilders.matchQuery("text", QUERY))
				//.setQuery(QueryBuilders.queryStringQuery(QUERY))
				.setSize(500).execute().actionGet(); // 100
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
				count++;
				String doc = hit.getId();
				
				if(doc.contains("File:") || (!doc.contains("http")))
					continue;
				
				rootSetPages.add(hit.getId());
				//System.out.println(hit.getId());
				if(count >=1000){
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
