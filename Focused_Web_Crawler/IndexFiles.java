package assignment_3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;


import java.util.Set;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class IndexFiles {

	private static Map<String, CatalogEntry> inlinkCatalog = new HashMap<>();
	private static Map<String, Set<String>> inlinksMap = new HashMap<>();
	private static Client client;
	private static final String ENTRY_DELIMITER = "#$%^&*#$%^&*";
	private static final String AUTHOR = "AMIT";
	private static final String DATASET = "van_dataset_1";
	//private static final String DATASET = "van_dataset_1";
	private static final String TYPE = "document";
	private static final String CLUSTER_NAME = "van_cluster";
	private static final String NODE_NAME = "node2";
	
	private static final String INLINK_FILE = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 3/inlinks/VAN_INLINK.txt";
	private static final String INLINK_CATALOG_FILE = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 3/inlinks/VAN_INLINK_CATALOG.txt";
	private static final String CORPUS = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 3/corpus_pass_4/";
	private static final String IP_ADDRESS = "10.0.0.189";
	
	private static final String htmlDataFile = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 3/HTML_Data.txt";
	private static final String htmlDataCatalogFile = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 3/HTML_Data_Catalog.txt";
	private static Map<String,CatalogEntry> extraDataCatalog = new HashMap<>();
	public static void main(String[] args) throws IOException {
		deserializeInlinkCatalog();
		deserializeHTMLCatalog();
		
		transportClientBuilder();
		try {
			createIndex();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("**** complete ****");
	}

	


	private static void deserializeHTMLCatalog() {

		String inlinlkCatalog = htmlDataCatalogFile;
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(inlinlkCatalog));
			while ((line = reader.readLine()) != null) {
				
				if(line.split(" ").length!=2)
					continue;
			
				CatalogEntry c = parseHTMLCatalogEntry(line.trim());
				if (null != c){
					extraDataCatalog.put(c.getUrl(), c);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static CatalogEntry parseHTMLCatalogEntry(String entry) {
		
		String tokens[] = entry.split(" ");
		
		String url = tokens[0];
		long startOffSet = Long.parseLong(tokens[1].split(",")[0]);
		long endOffSet = Long.parseLong(tokens[1].split(",")[1]);
		
		CatalogEntry c = new CatalogEntry(url, startOffSet, endOffSet);
		return c;
	}




	public static void transportClientBuilder() throws UnknownHostException{
		Settings settings = Settings.settingsBuilder()
		        .put("cluster.name", CLUSTER_NAME)
		        .put("node.name", NODE_NAME)
		        .build();
		
		
		 client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(IP_ADDRESS), 9300));
		 }
	
	
	private static void createIndex() throws IOException, InterruptedException{
		String directory_to_files = CORPUS;
		File directory = new File(directory_to_files);
		
		BulkRequestBuilder brb = client.prepareBulk();
		BulkResponse response;
		
		int count=0;
		for (File file : directory.listFiles()) {
			String fileToString = getStringFromFile(directory_to_files+file.getName());
			
			Map<String, IndexEntry> builderList = getBuilders(fileToString);

			for (Entry<String, IndexEntry> entry : builderList.entrySet()) {
				
				Map<String, Object> exisitingData = getIndexedDocumentIfExists(entry.getKey());
				
				IndexEntry mergedData = mergeDocumentData(exisitingData, entry);
				XContentBuilder builder = XContentFactory.jsonBuilder().startObject()
						.field("docno", entry.getKey())
						.field("text", mergedData.getText())
						.field("url", mergedData.getRawURL())
						.field("title", mergedData.getTitle())
						.field("in_links", mergedData.getInlinks())
						.field("out_links", mergedData.getOutlinks())
						.field("depth", mergedData.getDepth())
						.field("author", mergedData.getAuthors())
						.field("HTTPheader", mergedData.getHttpHeaders())
						.field("html_Source", mergedData.getHtmlSource())
						.endObject();

				brb.add(client.prepareIndex(DATASET, TYPE, "" + entry.getKey()).setSource(builder));
				count++;
				System.out.println(count);
				
				if (brb.numberOfActions() > 0 && count % 500 == 0) {
					response = brb.execute().actionGet();
					brb = client.prepareBulk();
					System.exit(0);
				}
				
				//Thread.sleep(1000);
				
			}
		}

		if (brb.numberOfActions() > 0)
			response = brb.execute().actionGet();
		
		client.close();

	}
	
	private static HTMLFields getHTMLFieldData(String url) throws IOException{
		
		CatalogEntry c = extraDataCatalog.get(url);
		RandomAccessFile raf = new RandomAccessFile(htmlDataFile, "rw");
		
		raf.seek(c.getStartOffSet());
		String data = raf.readLine();
		String tokens[] = data.split(ENTRY_DELIMITER);
		
		String html_Source = tokens[0];
		String httpHeaders = tokens[1];
		
		raf.close();
		HTMLFields hFields = new HTMLFields(html_Source, httpHeaders);
		return hFields;
	}
	
	
	private static IndexEntry mergeDocumentData(Map<String, Object> exisitingData,
			Entry<String, IndexEntry> entry) {

		if(null==exisitingData){
			IndexEntry iEntry = entry.getValue();
			iEntry.addAuthor(AUTHOR);
			try {
				//HTMLFields htmlData = getHTMLSource(entry.getValue().getRawURL());
				HTMLFields htmlData = getHTMLFieldData(entry.getValue().getRawURL());
				iEntry.setHtmlSource(htmlData.getHtmlSource());
				iEntry.setHttpHeaders(htmlData.getHeaders());
			} catch (IOException e) {
				iEntry.setHtmlSource("NO DATA");
				iEntry.setHttpHeaders("NO DATA");
			}
			
			return iEntry;
			
		}
		
		Set<String> inlinks = getLinks(exisitingData.get("in_links"));
		Set<String> outlinks = getLinks(exisitingData.get("out_links"));
		
		Object o = exisitingData.get("depth");
		Long depth = Long.MAX_VALUE;
		if(o!=null)
			depth = Long.parseLong(o.toString());
		String text = exisitingData.get("text").toString();
		Set<String> authors = getAuthors(exisitingData.get("authors"));
		String htmlSource = exisitingData.get("html_Source").toString();
		String httpHeaders = exisitingData.get("HTTPheader").toString();
		
		// merge inlinks
		Set<String> mergedInlinks = new LinkedHashSet<>();
		mergedInlinks.addAll(inlinks);
		mergedInlinks.addAll(entry.getValue().getInlinks());

		// merge outlinks
		Set<String> mergedOutlinks = new LinkedHashSet<>();
		mergedOutlinks.addAll(outlinks);
		mergedOutlinks.addAll(entry.getValue().getOutlinks());
		
		//merge authors
		Set<String> mergedAuthors = new LinkedHashSet<>();
		mergedAuthors.addAll(authors);
		mergedAuthors.add(AUTHOR);
		
		// minimum depth
		long minDepth = Math.min(depth, entry.getValue().getDepth());
		
		IndexEntry iEntry = entry.getValue();
		
		iEntry.setDepth(minDepth);
		iEntry.setInlinks(mergedInlinks);
		iEntry.setOutlinks(mergedOutlinks);
		iEntry.setAuthors(mergedAuthors);
		iEntry.setText(text);
		iEntry.setHtmlSource(htmlSource);
		iEntry.setHttpHeaders(httpHeaders);
		
		return iEntry;
	}



	private static Set<String> getAuthors(Object object) {
		
		if(object==null)
			return new LinkedHashSet<String>();
		
		String authorString = object.toString();
		
		
		authorString = authorString.substring(1, authorString.length()-1);
		Set<String> authors = new LinkedHashSet<>();
		String[] tokens = authorString.split(",");
		for(String token: tokens){
			authors.add(token.trim());
		}
		return authors;
	}


	private static Set<String> getLinks(Object object) {

		if(object==null)
			return new LinkedHashSet<String>();
		
		String linkString = object.toString();
		
		linkString = linkString.substring(1, linkString.length()-1);
		Set<String> links = new LinkedHashSet<>();
		String[] tokens = linkString.split(",");
		for(String token: tokens){
			links.add(token.trim());
		}
		return links;
	}



	public static Map<String,Object> getIndexedDocumentIfExists(String id){
		
		GetResponse response = client.prepareGet(DATASET, TYPE, id)
		        .execute()
		        .actionGet();
		
		if(response.isExists())
			return response.getSourceAsMap();
		
		return null;
	}
	
	
	public static String getStringFromFile(String file) {
		BufferedReader reader = null;
		String line = "";
		StringBuilder stringBuilder = new StringBuilder();
		try {
			reader = new BufferedReader(new FileReader(file));
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
		return stringBuilder.toString();
	}

	

	
	public static Map<String, IndexEntry> getBuilders(String documentContent) {

		Map<String, IndexEntry> builderList = new LinkedHashMap<>();

		Document doc = Jsoup.parse(documentContent, "UTF-8");
		Elements DOCElements = doc.getElementsByTag("DOC");
		Iterator<Element> docElementsIterator = DOCElements.iterator();
		while (docElementsIterator.hasNext()) {

			Element docElement = docElementsIterator.next();
			String docNo = docElement.getElementsByTag("DOCNO").first().text();
			
			String url = docElement.getElementsByTag("URL").first().text();
			
			
			String title = docElement.getElementsByTag("TITLE").first().text();
			String text = docElement.getElementsByTag("TEXT").first().text();
			/*Elements textElements = docElement.getElementsByTag("TEXT");
			StringBuilder docText = new StringBuilder();
			Iterator<Element> textElementsIterator = textElements.iterator();
			while (textElementsIterator.hasNext()) {
				Element textElement = textElementsIterator.next();
				String s = textElement.text();
				docText.append(s);
				docText.append(System.lineSeparator());
				
			}*/
			
			String outlinksAsString="";
			try{
			if(docElement.getElementsByTag("OUTLINKS").size()==0)
				outlinksAsString = null;
			else
			    outlinksAsString = docElement.getElementsByTag("OUTLINKS").first().text();
			}catch(Exception e){
				outlinksAsString="";
			}
			long depth = Long.MAX_VALUE;
			try{
				depth = Long.parseLong(docElement.getElementsByTag("DEPTH").first().text());
			}catch(Exception e){
				depth=Long.MAX_VALUE;
			}
			
			String inlinksAsString = getInlinksAsString(docNo);
			Set<String> inlinks = new LinkedHashSet<String>(inlinksAsSet(inlinksAsString));
			
			Set<String> outlinks = new LinkedHashSet<String>(outlinksAsSet( outlinksAsString));
			
			if(outlinks.size()==0)
				outlinks = getOutlinksForURL(docNo);
			
			builderList.put(docNo, new IndexEntry(url, title, text, outlinks, inlinks, depth));
		}
		return builderList;
	}

	private static String getInlinksAsString(String docNo) {

		String inlinkIndexFile = INLINK_FILE;
		
		if(null==inlinkCatalog.get(docNo))
			return null;
		
		long startIndex = inlinkCatalog.get(docNo).getStartOffSet();
		try {
			RandomAccessFile raf = new RandomAccessFile(inlinkIndexFile, "rw");
			raf.seek(startIndex);
			String inlinks = raf.readLine().trim();
			raf.close();
			if(inlinks.split("=")[0].equals(docNo))
				return inlinks.split("=")[1];
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static Set<String> inlinksAsSet(String linksAsString){
		Set<String> links = new LinkedHashSet<>();
		
		if(linksAsString==null || linksAsString.equals(""))
			return links;
		
		String tokens[] = linksAsString.split(" ");
		for(String link: tokens)
			links.add(link);
		
		return links;
	}
	
	private static Set<String> outlinksAsSet(String linksAsString){
		Set<String> links = new LinkedHashSet<>();
		
		if(linksAsString==null || linksAsString.equals(""))
			return links;
		
		String tokens[] = linksAsString.split(" ");
		for(String link: tokens){
			links.add(link);
		}
			
		
		return links;
	}

	private static void deserializeInlinkCatalog(){
		String inlinlkCatalog = INLINK_CATALOG_FILE;
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(inlinlkCatalog));
			while ((line = reader.readLine()) != null) {
				
				if(line.split(" ").length!=2)
					continue;
			
				CatalogEntry c = parseFileEntry(line.trim());
				if (null != c){
					inlinkCatalog.put(c.getUrl(), c);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static CatalogEntry parseFileEntry(String line) {

		String[] tokens = line.split(" ");
		String url="";
		long startOffSet=0;
		long endOffSet=0;
		
		url = tokens[0];
		startOffSet = Long.parseLong(tokens[1].split(",")[0]);
		endOffSet = Long.parseLong(tokens[1].split(",")[1]);
		
		return new CatalogEntry(url, startOffSet, endOffSet);
	}

	public static String noTags(String str) {
		Document d = Jsoup.parse(str);
		TextNode tn = new TextNode(d.body().html(), "");
		return tn.getWholeText();
	}
	
	private static String canonicalizeURL(String urlString) {

		
		String urlToBeReturned = "";
		try {
			URL url = new URL(urlString);
			String protocol = url.getProtocol();
			String domain = url.getHost().toLowerCase();
			String path = url.getPath().replace("//", "/");
			urlToBeReturned = protocol + "://" + domain + path;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println(urlString);
			e.printStackTrace();
		}
		return urlToBeReturned;
	}
	
	public static Set<String> getOutlinksForURL(String url) {

		Document doc;
		Set<String> outLinks = new LinkedHashSet<String>();
		try {
			doc = Jsoup.connect(url).ignoreContentType(true).get();
			
			Elements links = doc.select("a");

			for (Element link : links) {
				String absHref = link.attr("abs:href").trim();
				if (absHref.length() > 0 && absHref.contains("http") && !absHref.contains("wikimedia")) {
					if(absHref.contains("wikipedia") && !absHref.contains("en.wikipedia"))
						continue;
						
						outLinks.add(canonicalizeURL(absHref));
				}
			}
		} catch (Exception e) {
			return outLinks;
		}
		return outLinks;
	}
	
	public static HTMLFields getHTMLSource(String url) throws IOException{
		
		URL url1 = new URL(url);
		URLConnection conn = url1.openConnection();

		String headers = conn.getHeaderFields().toString();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder htmlSource = new StringBuilder();
        while ((inputLine = in.readLine()) != null)
        	htmlSource.append(inputLine);
        in.close();	
        
        HTMLFields data = new HTMLFields(htmlSource.toString(), headers);
        return data;
	}
}