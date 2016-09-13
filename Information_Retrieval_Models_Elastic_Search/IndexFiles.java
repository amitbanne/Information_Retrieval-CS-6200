package assignment_1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class IndexFiles {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		String directory_to_files = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 1/AP89_DATA/AP_DATA/ap89_collection";
		File directory = new File(directory_to_files);

		Client client = TransportClient.builder().build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		int id = 1;
		
		BulkRequestBuilder brb = client.prepareBulk();
		BulkResponse response;
		for (File file : directory.listFiles()) {
			String fileToString = getStringFromFile(file.getName());
			Map<String, String> builderList = getBuilders(fileToString);
			for (Entry<String, String> entry : builderList.entrySet()) {
				XContentBuilder builder = XContentFactory.jsonBuilder().startObject().field("docno", entry.getKey())
						.field("text", entry.getValue()).endObject();

				System.out.println("ID: " + id);
				brb.add(client.prepareIndex("ap_dataset", "document", "" + id).setSource(builder));
				++id;
				if (brb.numberOfActions() > 0 && id % 2000 == 0) {
					response = brb.execute().actionGet();
					brb = client.prepareBulk();
				}
			}
		}

		if (brb.numberOfActions() > 0)
			response = brb.execute().actionGet();
		client.close();

		System.out.println("**** complete ****");
	}

	public static String getStringFromFile(String file) {
		String path = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 1/AP89_DATA/AP_DATA/ap89_collection";
		BufferedReader reader = null;
		String line = "";
		StringBuilder stringBuilder = new StringBuilder();
		try {
			reader = new BufferedReader(new FileReader(path + "/" + file));
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

	public static Map<String, String> getBuilders(String documentContent) {

		Map<String, String> builderList = new LinkedHashMap<>();
		
		Document doc = Jsoup.parse(documentContent, "UTF-8");
		Elements DOCElements = doc.getElementsByTag("DOC");
		Iterator<Element> docElementsIterator = DOCElements.iterator();
		while (docElementsIterator.hasNext()) {

			Element docElement = docElementsIterator.next();
			String docNo = docElement.getElementsByTag("DOCNO").first().text();
			Elements textElements = docElement.getElementsByTag("TEXT");
			StringBuilder docText = new StringBuilder();
			Iterator<Element> textElementsIterator = textElements.iterator();
			while (textElementsIterator.hasNext()) {
				Element textElement = textElementsIterator.next();
				String s = textElement.text();

				docText.append(s);
				docText.append(System.lineSeparator());
				
			}
			
			builderList.put(docNo, docText.toString());
		}
		return builderList;
	}

	public static String noTags(String str) {
		Document d = Jsoup.parse(str);
		TextNode tn = new TextNode(d.body().html(), "");
		return tn.getWholeText();
	}

}
