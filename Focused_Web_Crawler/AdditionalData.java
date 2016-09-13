package assignment_3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AdditionalData {

	private static int count=0;
	private static Map<String, Long> domainDelay = new HashMap<String, Long>();
	private static Map<String,CatalogEntry> extraDataCatalog = new HashMap<>();
	private static final String ENTRY_DELIMITER = "#$%^&*#$%^&*";
	private static List<ExtraDataUnit> dataList = new ArrayList<>();
	private static final String htmlDataFile = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 3/HTML_Data.txt";
	private static final String htmlDataCatalogFile = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 3/HTML_Data_Catalog.txt";
	private static final String CORPUS = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 3/corpus_pass_4/";
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		extractData();
		dumpToFile();
		dumpCatalog();
	}
	
	private static void dumpToFile() throws IOException {

		RandomAccessFile raf = new RandomAccessFile(htmlDataFile,"rw");
		long seekOffSet = 0;
		String delimiter ="";
		for(ExtraDataUnit e : dataList){
			long startOffSet = seekOffSet;
			raf.seek(seekOffSet);
			StringBuilder sb = new StringBuilder();
			sb.append(delimiter);
			delimiter = System.lineSeparator();
			sb.append(e.getHtml_Source());
			sb.append(ENTRY_DELIMITER);
			sb.append(e.getHtml_Source());
			
			raf.writeBytes(sb.toString());
			seekOffSet = raf.getFilePointer();
			long endOffSet = seekOffSet;
			
			extraDataCatalog.put(e.getRawURL(), new CatalogEntry(e.getRawURL(), startOffSet, endOffSet));
		}
			raf.close();
		
	}

	public static void extractData() throws IOException, InterruptedException{
		String directory_to_files = CORPUS;
		File directory = new File(directory_to_files);

		for (File file : directory.listFiles()) {
			String fileToString = getStringFromFile(directory_to_files+file.getName());
			
			fetchHTMLData(fileToString);
		}
		
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

	public static void fetchHTMLData(String documentContent) throws IOException, InterruptedException {
		Document doc = Jsoup.parse(documentContent, "UTF-8");
		Elements DOCElements = doc.getElementsByTag("DOC");
		Iterator<Element> docElementsIterator = DOCElements.iterator();
		while (docElementsIterator.hasNext()) {

			count++;
			System.out.println(count);
			Element docElement = docElementsIterator.next();
			String docNo = docElement.getElementsByTag("DOCNO").first().text();
			String rawURL = docElement.getElementsByTag("URL").first().text();
			
			HTMLFields htmlData = getHTMLSource(rawURL);
			dataList.add(new ExtraDataUnit(docNo, rawURL, htmlData.getHeaders(),htmlData.getHtmlSource()));
		}
	}
	
	public static HTMLFields getHTMLSource(String url) throws IOException{
		
		checkDomainDelay(url);
		
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
	

	private static void dumpCatalog() {

		StringBuilder sb = new StringBuilder();

		String endOfLine = "";
		for (Entry<String, CatalogEntry> e : extraDataCatalog.entrySet()) {
			sb.append(endOfLine);
			endOfLine = System.lineSeparator();

			CatalogEntry c = e.getValue();
			// FORMAT: url:start,end\n
			sb.append(c.getUrl());
			sb.append(" ");
			sb.append(c.getStartOffSet());
			sb.append(",");
			sb.append(c.getEndOffSet());
		}
		
		String inlinkcatalogFileName = htmlDataCatalogFile;
		File file = new File(inlinkcatalogFileName);
		try {
			FileWriter fw = new FileWriter(file, true);
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	
}
