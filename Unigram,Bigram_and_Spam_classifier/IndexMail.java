package assignment_7;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public class IndexMail {

	private static final String SPAM_INFO = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/trec07_spam/trec07p/full/index";
	private static final String EMAIL_CORPUS_DIRECTORY = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/trec07_spam/trec07p/data/";
	private static Map<String, String> emailSpamMap = new HashMap<>();
	private static Integer SPAM_TRAIN_COUNT, currentSpamTrainCount = 0;
	private static Integer HAM_TRAIN_COUNT, currentHamTrainCount = 0;
	private static Client client;
	private static String IP_ADDRESS = "localhost";
	private static BulkRequestBuilder brb;
	private static BulkResponse response;
	private static final String DATA_SET = "spam_classifier";
	private static final String TYPE = "document";

	public static void main(String[] args) {

		deserializeSpamInfo();
		SPAM_TRAIN_COUNT = Integer.parseInt(Math.round((0.67 * 0.80 * emailSpamMap.size())) + "");
		HAM_TRAIN_COUNT = Integer.parseInt(Math.round((0.33 * 0.80 * emailSpamMap.size())) + "");
		transportClientBuilder();
		deserializeEmailData();
		checkEmailsRemainingForIndex();

	}

	private static void checkEmailsRemainingForIndex() {
		if (brb.numberOfActions() > 0)
			response = brb.execute().actionGet();
		client.close();

	}

	private static void deserializeEmailData() {
		File directory = new File(EMAIL_CORPUS_DIRECTORY);
		int emailIndexId = 0;
		for (File file : directory.listFiles()) {
			String fileToString = getStringFromFile(EMAIL_CORPUS_DIRECTORY + file.getName());
/*
			if (!fileToString.contains("<html>"))
				fileToString = "";*/
			if (emailSpamMap.get(file.getName()).equals("spam")) {
				currentSpamTrainCount++;
				if (currentSpamTrainCount <= SPAM_TRAIN_COUNT)
					pushToElasticSearch(file.getName(), ++emailIndexId, fileToString, "SPAM", "TRAIN");
				else
					pushToElasticSearch(file.getName(), ++emailIndexId, fileToString, "SPAM", "TEST");
			} else {
				currentHamTrainCount++;
				if (currentHamTrainCount <= HAM_TRAIN_COUNT)
					pushToElasticSearch(file.getName(), ++emailIndexId, fileToString, "HAM", "TRAIN");
				else
					pushToElasticSearch(file.getName(), ++emailIndexId, fileToString, "HAM", "TEST");
			}
		}
	}

	private static void pushToElasticSearch(String fileName, int id, String fileContent, String spam, String type) {

		XContentBuilder builder;
		try {
			builder = XContentFactory.jsonBuilder().startObject().field("id", id).field("name", fileName)
					.field("text", fileContent).field("type", type).field("spam", spam).endObject();

			System.out.println("ID: " + id);
			brb.add(client.prepareIndex(DATA_SET, TYPE, id + "").setSource(builder));
			if (brb.numberOfActions() > 0 && id % 2000 == 0) {
				response = brb.execute().actionGet();
				brb = client.prepareBulk();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static String getStringFromFile(String fileName) {

		BufferedReader reader = null;
		String line = "";
		StringBuilder fileText = new StringBuilder();
		try {
			reader = new BufferedReader(new FileReader(fileName));
			while ((line = reader.readLine()) != null) {
				if (line.endsWith("=")) {
					try {
						line = line.substring(0, line.length() - 2);
						fileText.append(line);
					} catch (IndexOutOfBoundsException e) {
						fileText.append(line);
					}
				} else {
					fileText.append(line);
					fileText.append(System.lineSeparator());
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return cleanText(fileText.toString());
	}

	private static String cleanText(String docText) {
		docText = docText.replace("'", "");
		docText = docText.replace(":", "");
		docText = docText.replace("-", " ");
		docText = docText.replace("-", " ");
		docText = docText.replaceAll("\"", "");
		docText = docText.replace("(", "");
		docText = docText.replace(")", "");
		docText = docText.replace("[", "");
		docText = docText.replace("{", "");
		docText = docText.replace("}", "");
		docText = docText.replace("]", "");
		docText = docText.replace(";", "");
		docText = docText.replace("-", " ");
		docText = docText.replace("/", "");
		docText = docText.replace("#", "");
		docText = docText.replace("?", "");
		docText = docText.replace("@", "");
		docText = docText.replace("<", "");
		docText = docText.replace(">", "");
		docText = docText.replace(",", "");
		docText = docText.replace("+", "");
		docText = docText.replace("=", " ");
		docText = docText.replace("$", "");
		docText = docText.replace("%", "");
		docText = docText.replace("_", "");
		docText = docText.replace("-", " ");
		docText = docText.replace("\t", "");
		
		return docText;
	}

	private static void deserializeSpamInfo() {

		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(SPAM_INFO));
			while ((line = reader.readLine()) != null) {
				String tokens[] = line.split(" ");
				String spamStatus = tokens[0].trim();
				String emailId = tokens[1].split("/")[2].trim();

				emailSpamMap.put(emailId, spamStatus);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void transportClientBuilder() {

		try {
			Settings settings = Settings.settingsBuilder().build();
			client = TransportClient.builder().settings(settings).build()
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(IP_ADDRESS), 9300));
			brb = client.prepareBulk();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
