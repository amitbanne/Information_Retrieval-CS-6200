package assignment_3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.tika.language.LanguageIdentifier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {

	private static Set<String> urlsProcessed = new LinkedHashSet<String>();
	// private static Map<String, Long> urlsToBeCrawled = new
	// LinkedHashMap<String, Long>();
	private static List<URLData> urlsToBeCrawled = new ArrayList<URLData>();
	private static Map<String, Boolean> robotVisited = new HashMap<String, Boolean>();
	private static Map<String, String> canonicalizedCache = new HashMap<String, String>();
	private static final String DOCUMENT_NAME = "VAN_";
	private static long documentNumber = 1;
	private static Map<String, String> documentsToBeWritten = new LinkedHashMap<String, String>();

	private static final String CATALOG_NAME = "VAN_CATALOG_";
	private static long catalogFileNumber = 1;
	private static Map<String, CatalogEntry> documentCatalog = new LinkedHashMap<String, CatalogEntry>();

	private static final String OUTLINK_NAME = "VAN_OUTLINK_";
	private static long outLinkFileNumber = 1;
	private static Map<String, Set<String>> urlOutLinkMap = new LinkedHashMap<String, Set<String>>();

	private static final String OUTLINK_CATALOG_NAME = "VAN_OUTLINK_CATALOG_";
	private static long outlinkCatalogFileNumber = 1;
	private static Map<String, CatalogEntry> outlinkCatalog = new LinkedHashMap<String, CatalogEntry>();
	private static Map<String, CatalogEntry> inlinkCatalog = new LinkedHashMap<String, CatalogEntry>();
	private static Map<String, Set<String>> inlinkMap = new LinkedHashMap<String, Set<String>>();
	private static Map<String, Long> domainDelay = new HashMap<String, Long>();
	private static final String FILE_NUMBER_FORMAT = "%04d";
	private static final String DOCUMENT_EXTENSION = ".txt";
	private static final String DOCUMENT_DIRECTORY = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 3/corpus_pass_4/";

	private static final int DOCUMENTS_PER_FILE = 500;
	private static final int TOTAL_CRAWL_SIZE = 20000;
	private static final String[] relevantTermList={"war","battle","holocaust","nazi","allies", "ally","surrender","victo","invasio","army","navy","invad","seige","death","die","dead","troop","milita","force", "conqu","power"};
	private static int WIKIPEDIA_COUNT = 0;
	private static int BRITANNICA_COUNT = 0;
	
	public static void main(String[] args) {
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

		initializingCrawl();
		crawl();

		try {
			flushInLinks();
			flushInlinkCatalog();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println(dateFormat.format(date)); // 2014/08/06 15:59:48
		System.out.println("SIZE: " + urlsProcessed.size());

	}

	private static void initializingCrawl() {

		String seedUrl = "http://www.history.com/topics/world-war-ii";
		String seedUrl2 = "http://en.wikipedia.org/wiki/World_War_II";
		String seedUrl3 = "http://en.wikipedia.org/wiki/Causes_of_World_War_II";
		String seedUrl4 = "http://www.historyonthenet.com/ww2/causes.htm";
		String seedUrl5 = "http://www.britannica.com/technology/military-aircraft";
		String seedUrl6 = "http://www.britannica.com/event/World-War-II/German-occupied-Europe";
		String seedUrl7 = "http://www.nytimes.com/topic/subject/world-war-ii-193945";
		String seedUrl8 = "http://www.britannica.com/event/World-War-II/The-Battle-of-Britain";
		
		/*
		 * String seedUrl = "http://www.history.com/topics/world-war-ii"; String
		 * seedUrl2 = "http://en.wikipedia.org/wiki/World_War_II"; String
		 * seedUrl3 =
		 * "http://en.wikipedia.org/wiki/List_of_World_War_II_battles_involving_the_United_States";
		 * String seedUrl4 =
		 * "http://en.wikipedia.org/wiki/Military_history_of_the_United_States_during_World_War_II";
		 */

		urlsToBeCrawled.add(new URLData(seedUrl, System.currentTimeMillis(), Integer.MAX_VALUE, 0));
		urlsToBeCrawled.add(new URLData(seedUrl5, System.currentTimeMillis(), Integer.MAX_VALUE, 0));
		urlsToBeCrawled.add(new URLData(seedUrl2, System.currentTimeMillis(), Integer.MAX_VALUE, 0));
		urlsToBeCrawled.add(new URLData(seedUrl4, System.currentTimeMillis(), Integer.MAX_VALUE, 0));
		urlsToBeCrawled.add(new URLData(seedUrl6, System.currentTimeMillis(), Integer.MAX_VALUE, 0));
		urlsToBeCrawled.add(new URLData(seedUrl3, System.currentTimeMillis(), Integer.MAX_VALUE, 0));
		urlsToBeCrawled.add(new URLData(seedUrl7, System.currentTimeMillis(), Integer.MAX_VALUE, 0));
		urlsToBeCrawled.add(new URLData(seedUrl8, System.currentTimeMillis(), Integer.MAX_VALUE, 0));

	}

	private static void crawl() {

		int seedCount = 0;
		while (urlsToBeCrawled.size() != 0 && urlsProcessed.size() < TOTAL_CRAWL_SIZE) {
			List<URLData> urlBatch = new ArrayList<>();
			urlBatch.addAll(deQueue());

			Iterator<URLData> batchIterator = urlBatch.iterator();

			while (batchIterator.hasNext()) {
				URLData current = batchIterator.next();
				String url = current.getUrl();
				String canonicalizedURL = "";
				if (canonicalizedCache.containsKey(url)) {
					canonicalizedURL = canonicalizedCache.get(url);
				} else {
					canonicalizedURL = canonicalizeURL(url);
					canonicalizedCache.put(url, canonicalizedURL);
				}
				if (!urlsProcessed.contains(canonicalizedURL)) {
					
					// domain limit for wikipedia and britannica
					if(domainLimitReached(url))
						continue;
					
					seedCount++;
					
					checkDomainDelay(url);
					String title = getTitleForURL(url);
					String bodyText = getBodyTextForURL(url);
					
					if(!englishLanguageCheck(bodyText))
						continue;
						
					
					int relevantCount = checkRelevance(bodyText);
					
					if(relevantCount==0)
						continue;
					
					
					System.out.print(seedCount + "-->" + url +"RCount: "+relevantCount+ " :");
					Set<String> outLinks = getOutlinksForURL(url);
					createDocumentForURL(new DocumentData(url, canonicalizedURL, title, bodyText, outLinks, current.getDepth()));
					if (null != outLinks) {
						// urlOutLinkMap.put(url, outLinks);
						updateInlinks(canonicalizedURL, outLinks, relevantCount, current.getDepth());
						System.out.println(outLinks.size());
					}
					urlsProcessed.add(canonicalizedURL);
				}
			}

			if (seedCount > 8)
				sortQueueElements();

		}
	}

	private static boolean englishLanguageCheck(String bodyText) {
		
		LanguageIdentifier identifier = new LanguageIdentifier(bodyText);
	      String language = identifier.getLanguage();
	      if(language.equals("en"))
	    	  return true;
	      else
	    	  return false;
	}

	private static boolean domainLimitReached(String url) {
		if(url.contains("wikipedia.org")){
			if(WIKIPEDIA_COUNT > 18000)
				return true;
			else{
				++WIKIPEDIA_COUNT;
				return false;
			}
		}else if(url.contains("britannica.com")){
			if(BRITANNICA_COUNT > 8000)
				return true;
			else{
				++BRITANNICA_COUNT;
				return false;
			}
		}
		return false;
		
	}

	private static int checkRelevance(String bodyText) {

		String text = bodyText.toLowerCase();
		int relevantCount=0;
		for(String term : relevantTermList){
			if(text.contains(term))
				++relevantCount;
		}
		
			return relevantCount;
	}

	private static void checkDomainDelay(String url) {

		URL domURL = null;
		long currentTime = System.currentTimeMillis();
		try {
			domURL = new URL(url);
			if (domainDelay.containsKey(domURL.getHost())) {
				long lastTime = domainDelay.get(domURL.getHost());
				if ((currentTime - lastTime) < 1000) {
					Thread.sleep(lastTime + 1000 - currentTime);
				}
			}
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		domainDelay.put(domURL.getHost(), System.currentTimeMillis());
	}

	private static List<URLData> deQueue() {
		if (urlsToBeCrawled.size() >= 1000) {
			List<URLData> batch = new ArrayList<>();
			batch.addAll(urlsToBeCrawled.subList(0, 1000));
			urlsToBeCrawled.removeAll(batch);
			return batch;
		} else if (urlsToBeCrawled.size() >= 100) {
			List<URLData> batch = new ArrayList<>();
			batch.addAll(urlsToBeCrawled.subList(0, 100));
			urlsToBeCrawled.removeAll(batch);
			return batch;
		} else {
			List<URLData> batch = new ArrayList<URLData>();
			batch.addAll(urlsToBeCrawled);
			urlsToBeCrawled.clear();
			return batch;
		}
	}

	private static void sortQueueElements() {

		List<URLData> urlCopy = new ArrayList<>();
		urlCopy.addAll(urlsToBeCrawled);
		Collections.sort(urlCopy, new Comparator<URLData>() {

			@Override
			public int compare(URLData o1, URLData o2) {

				String url1 = o1.getUrl();
				String url2 = o2.getUrl();

				if(o1.getRelevantCount() == o2.getRelevantCount()){

					if (inlinkMap.containsKey(url1) && inlinkMap.containsKey(url2)) {

						if (inlinkMap.get(url1).size() == inlinkMap.get(url2).size()) {
							if (o2.getTimeOfEntry() - o1.getTimeOfEntry() >= 0)
								return -1;
							else
								return 1;
						} else if ((inlinkMap.get(url2).size() - inlinkMap.get(url1).size()) >= 0)
							return 1;
						else
							return -1;
					} else if (inlinkMap.containsKey(url1)) {
						return -1;
					} else {
						return 1;
					}

				}else if(o1.getRelevantCount() > o2.getRelevantCount())
					return -1;
				else 
					return 1;
			}

	});
		urlsToBeCrawled.clear();
		urlsToBeCrawled.addAll(urlCopy);

}

private static void updateInlinks(String url, Set<String> outLinks, int relevantCount, int parentDepth) {

	for (String outLink : outLinks) {
		String canonicalizedOutLink = canonicalizeURL(outLink);
		if (inlinkMap.containsKey(canonicalizedOutLink)) {
			Set<String> existingInlinks = inlinkMap.get(canonicalizedOutLink);
			existingInlinks.add(url);
			inlinkMap.put(canonicalizedOutLink, existingInlinks);
		} else {
			Set<String> newInlinks = new LinkedHashSet<String>();
			newInlinks.add(url);
			inlinkMap.put(canonicalizedOutLink, newInlinks);
		}
		urlsToBeCrawled.add(new URLData(outLink, System.currentTimeMillis(), relevantCount, (parentDepth+1)));
	}
}

private static void createDocumentForURL(DocumentData documentData) {

	String documentText = composeDocumentText(documentData);
	documentsToBeWritten.put(documentData.getUrl(), documentText);

	if (documentsToBeWritten.size() == DOCUMENTS_PER_FILE) {
		try {
			// flush document data
			flushDocumentData();
			// flush catalog data
			// flushDocumentCatalog();
			// flush outlinks
			// flushOutLinks();
			// flush outlink catalog
			// flushOutlinkCatalog();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		documentsToBeWritten = new LinkedHashMap<String, String>();
		// documentCatalog = new LinkedHashMap<String, CatalogEntry>();
		// urlOutLinkMap = new LinkedHashMap<String, Set<String>>();
		// outlinkCatalog = new LinkedHashMap<String, CatalogEntry>();
	}
}

private static void flushOutlinkCatalog() {

	StringBuilder sb = new StringBuilder();

	String endOfLine = "";
	for (Entry<String, CatalogEntry> e : outlinkCatalog.entrySet()) {
		sb.append(endOfLine);
		endOfLine = System.lineSeparator();

		// FORMAT: url:start,end\n
		sb.append(e.getKey());
		sb.append(":");
		sb.append(e.getValue().getFileName());
		sb.append(",");
		sb.append(e.getValue().getStartOffSet());
		sb.append(",");
		sb.append(e.getValue().getEndOffSet());
	}
	String outlinkcatalogNum = String.format(FILE_NUMBER_FORMAT, outlinkCatalogFileNumber++);
	String outlinkcatalogFileName = DOCUMENT_DIRECTORY + OUTLINK_CATALOG_NAME + outlinkcatalogNum
			+ DOCUMENT_EXTENSION;
	File file = new File(outlinkcatalogFileName);
	try {
		FileWriter fw = new FileWriter(file, true);
		fw.write(sb.toString());
		fw.close();
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
}

	private static void flushOutLinks() throws IOException {

		long seekOffSet = 0;
		String docNum = String.format(FILE_NUMBER_FORMAT, outLinkFileNumber++);
		String outputFileName = DOCUMENT_DIRECTORY + OUTLINK_NAME + docNum + DOCUMENT_EXTENSION;
		RandomAccessFile raf = new RandomAccessFile(outputFileName, "rw");
		for (Entry<String, Set<String>> urlOutLinkData : urlOutLinkMap.entrySet()) {
			long startOffSet = seekOffSet;
			raf.seek(startOffSet);

			StringBuffer sb = new StringBuffer();
			sb.append(urlOutLinkData.getKey());
			sb.append("=");
			sb.append(convertOutlinkSetToString(urlOutLinkData.getValue()));
			raf.writeBytes(sb.toString());
			seekOffSet = raf.getFilePointer();
			long endOffSet = seekOffSet;
			outlinkCatalog.put(urlOutLinkData.getKey(),
					new CatalogEntry(urlOutLinkData.getKey(), outputFileName, startOffSet, endOffSet));
		}
		raf.close();
	}

	private static void flushInlinkCatalog() {

		StringBuilder sb = new StringBuilder();

		String endOfLine = "";
		for (Entry<String, CatalogEntry> e : inlinkCatalog.entrySet()) {
			sb.append(endOfLine);
			endOfLine = System.lineSeparator();

			// FORMAT: url:start,end\n
			sb.append(e.getKey());
			sb.append(":");
			sb.append(e.getValue().getStartOffSet());
			sb.append(",");
			sb.append(e.getValue().getEndOffSet());
		}
		// String outlinkcatalogNum = String.format(FILE_NUMBER_FORMAT,
		// outlinkCatalogFileNumber++);
		String inlinkcatalogFileName = DOCUMENT_DIRECTORY + "VAN_INLINK_CATALOG" + DOCUMENT_EXTENSION;
		File file = new File(inlinkcatalogFileName);
		try {
			FileWriter fw = new FileWriter(file, true);
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static void flushInLinks() throws IOException {

		long seekOffSet = 0;
		String docNum = "VAN_INLINK";
		String outputFileName = DOCUMENT_DIRECTORY + docNum + DOCUMENT_EXTENSION;
		RandomAccessFile raf = new RandomAccessFile(outputFileName, "rw");
		String delmiter ="";
				
		for (Entry<String, Set<String>> urlInLinkData : inlinkMap.entrySet()) {
			long startOffSet = seekOffSet;
			raf.seek(startOffSet);

			StringBuffer sb = new StringBuffer();
			sb.append(delmiter);
			delmiter = System.lineSeparator();
			sb.append(urlInLinkData.getKey());
			sb.append("=");
			sb.append(convertInlinkSetToString(urlInLinkData.getValue()));
			raf.writeBytes(sb.toString());
			seekOffSet = raf.getFilePointer();
			long endOffSet = seekOffSet;
			inlinkCatalog.put(urlInLinkData.getKey(),
					new CatalogEntry(urlInLinkData.getKey(), outputFileName, startOffSet, endOffSet));
		}
		raf.close();
	}

	private static String convertInlinkSetToString(Set<String> inlinks) {

		StringBuilder sb = new StringBuilder();
		String delimiter = "";
		for (String inlink : inlinks) {
			sb.append(delimiter);
			delimiter = " ";
			sb.append(inlink);
		}
		return sb.toString();
	}

	private static String convertOutlinkSetToString(Set<String> outlinks) {

		if (null == outlinks)
			return "";

		StringBuilder sb = new StringBuilder();
		String delimiter = "";
		for (String outlink : outlinks) {
			sb.append(delimiter);
			delimiter = System.lineSeparator();
			sb.append(canonicalizeURL(outlink));
		}
		return sb.toString();
	}

	private static void flushDocumentData() throws IOException {

		long seekOffSet = 0;
		String docNum = String.format(FILE_NUMBER_FORMAT, documentNumber++);
		String outputFileName = DOCUMENT_DIRECTORY + DOCUMENT_NAME + docNum + DOCUMENT_EXTENSION;
		RandomAccessFile raf = new RandomAccessFile(outputFileName, "rw");
		for (Entry<String, String> docData : documentsToBeWritten.entrySet()) {
			long startOffSet = seekOffSet;
			raf.seek(startOffSet);
			raf.writeBytes(docData.getValue());
			seekOffSet = raf.getFilePointer();
			long endOffSet = seekOffSet;
			documentCatalog.put(docData.getKey(),
					new CatalogEntry(docData.getKey(), outputFileName, startOffSet, endOffSet));
		}
		raf.close();

	}

	private static void flushDocumentCatalog() {
		StringBuilder sb = new StringBuilder();

		String endOfLine = "";
		for (Entry<String, CatalogEntry> e : documentCatalog.entrySet()) {
			sb.append(endOfLine);
			endOfLine = System.lineSeparator();

			// FORMAT: url:start,end\n
			sb.append(e.getKey());
			sb.append(":");
			sb.append(e.getValue().getFileName());
			sb.append(",");
			sb.append(e.getValue().getStartOffSet());
			sb.append(",");
			sb.append(e.getValue().getEndOffSet());
		}

		String catalogNum = String.format(FILE_NUMBER_FORMAT, catalogFileNumber++);
		String catalogFileName = DOCUMENT_DIRECTORY + CATALOG_NAME + catalogNum + DOCUMENT_EXTENSION;
		File file = new File(catalogFileName);
		try {
			FileWriter fw = new FileWriter(file, true);
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static String composeDocumentText(DocumentData documentData) {

		StringBuilder docText = new StringBuilder();

		docText.append("<DOC>");
		docText.append(System.lineSeparator());

		docText.append("<DOCNO>");
		docText.append(documentData.getCanonicalizedURL());
		docText.append("</DOCNO>");
		docText.append(System.lineSeparator());

		docText.append("<URL>");
		docText.append(documentData.getUrl());
		docText.append("</URL>");
		docText.append(System.lineSeparator());

		docText.append("<TITLE>");
		docText.append(documentData.getTitle());
		docText.append("</TITLE>");
		docText.append(System.lineSeparator());

		docText.append("<TEXT>");
		docText.append(documentData.getBodyText());
		docText.append("</TEXT>");
		docText.append(System.lineSeparator());

		docText.append("<OUTLINKS>");
		docText.append(convertOutlinkSetToString(documentData.getOutLinks()));
		docText.append("</OUTLINKS>");
		docText.append(System.lineSeparator());
		
		docText.append("<DEPTH>");
		docText.append(documentData.getDepth());
		docText.append("</DEPTH>");
		docText.append(System.lineSeparator());
		
		docText.append("</DOC>");
		docText.append(System.lineSeparator());

		return docText.toString();
	}

	private static String getTitleForURL(String url) {

		Document doc;
		String title = "";
		try {
			doc = Jsoup.connect(url).ignoreContentType(true).get();
			Elements titleElement = doc.select("title");
			if (titleElement.size() == 0)
				return "";
			title = titleElement.first().text();
		} catch (Exception e) {
			// TODO Auto-generated catch block

			return "";
		}

		return title;

	}

	private static String getBodyTextForURL(String url) {

		Document doc;
		String text = "";
		try {
			doc = Jsoup.connect(url).ignoreContentType(true).get();
			if (null == doc.body())
				return "";
			text = doc.body().text();
		} catch (Exception e) {

			return "";
		}
		return text;
	}

	public static Set<String> getOutlinksForURL(String url) {

		Document doc;
		Set<String> outLinks = new HashSet<String>();
		try {
			doc = Jsoup.connect(url).ignoreContentType(true).get();
			
			// clean unwanted links
			if(url.contains("wikipedia")){
				//languages
				doc.getElementById("p-lang").remove();
				//category links
				doc.getElementById("catlinks").remove();
				//main page
				doc.getElementById("p-logo").remove();
				doc.getElementById("p-interaction").remove();
				doc.getElementById("p-tb").remove();
				doc.getElementById("p-coll-print_export").remove();
				doc.getElementById("p-wikibase-otherprojects").remove();
				doc.getElementById("toc").remove();
				doc.getElementById("footer").remove();
			}
			
			
			Elements links = doc.select("a");

			for (Element link : links) {
				String absHref = link.attr("abs:href").trim();
				if (absHref.length() > 0 && absHref.contains("http") && !skipURL(absHref)) {
					if (robotVisited.containsKey(absHref)) {
						if (robotVisited.get(absHref))
							outLinks.add(absHref);
					} else {
						boolean isSafe = RobotParser.robotSafe(absHref);
						if (isSafe)
							outLinks.add(absHref);
						robotVisited.put(url, isSafe);
					}
				}
			}
		} catch (Exception e) {
			return null;
		}
		return outLinks;
	}

	private static boolean skipURL(String url) {
		url = url.toLowerCase();
		if (url.contains("File:".toLowerCase()) || url.contains("Portal:Current_events:".toLowerCase())|| url.contains("/videos/".toLowerCase())
				|| url.contains("special:random".toLowerCase()) || url.contains("wikimedia".toLowerCase())
				|| url.contains("mylifetime".toLowerCase()) || url.contains("integrated_authority".toLowerCase())
				|| url.contains("mediawiki".toLowerCase()) || url.contains("wiki/special".toLowerCase())
				|| url.contains("wiki/special".toLowerCase()) || url.contains("wiki/special".toLowerCase())
				|| url.contains("facebook".toLowerCase()) || url.contains("instagram".toLowerCase())
				|| url.contains("play.google".toLowerCase()) || url.contains("youtube".toLowerCase())
				|| url.contains("twitter".toLowerCase()) || url.contains("contact".toLowerCase())
				|| url.contains("support".toLowerCase()) || url.contains("license".toLowerCase())
				|| url.contains(".pdf".toLowerCase()) || url.contains("aenetworks".toLowerCase())
				|| url.contains("careers".toLowerCase()) || url.contains("advertise".toLowerCase())
				|| url.contains("guidelines".toLowerCase()) || url.contains("books.google".toLowerCase())
				|| url.contains("shop".toLowerCase()) || url.contains("privacy".toLowerCase())
				|| url.contains("copyright".toLowerCase()) || url.contains("email".toLowerCase())
				|| url.contains("sign-up".toLowerCase()) || url.contains("plus.google".toLowerCase())
				|| url.contains("aetv.com".toLowerCase()) || url.contains("fyi.tv".toLowerCase())
				|| url.equals("http://www.history.com/mobile:".toLowerCase())
				|| url.contains("foursquare".toLowerCase()) || url.contains("contact_us".toLowerCase())
				|| url.contains("emails".toLowerCase()) || url.contains("wiki/special".toLowerCase())
				|| url.contains("portal:featured_content".toLowerCase())
				|| url.contains("wikipedia:about".toLowerCase())
				|| url.contains("wikipedia:community_portal".toLowerCase())
				|| url.contains("special:recentchanges".toLowerCase())
				|| url.contains("wikipedia:file_upload_wizard".toLowerCase()) || url.contains("en.m".toLowerCase())
				|| url.contains("Help:Category".toLowerCase()) || url.contains("action=edit".toLowerCase())
				|| url.contains("citation".toLowerCase()) || url.contains("ads".toLowerCase())
				|| url.contains("/wiki/Category:Wikipedia_articles_with_GND_identifiers".toLowerCase())
				|| url.contains("/wiki/Category:All_articles_needing_additional_references".toLowerCase())
				|| url.contains("wiki/Help:Introduction_to_referencing_with_Wiki_Markup/".toLowerCase())
				|| url.contains("/wiki/Wikipedia:Citing_sources".toLowerCase())
				|| url.contains("/wiki/Help:Maintenance_template_removal".toLowerCase())
				|| url.contains("www.history.com/videos".toLowerCase())
				|| url.contains("/www.history.com/full-access".toLowerCase())
				|| url.contains("http://www.history.com/schedule".toLowerCase())
				|| url.contains("http://www.history.com/speeches".toLowerCase())
				|| url.contains("http://www.history.com/news".toLowerCase())
				
		)
			return true;

		return false;
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
			e.printStackTrace();
		}
		return urlToBeReturned;
	}
}
