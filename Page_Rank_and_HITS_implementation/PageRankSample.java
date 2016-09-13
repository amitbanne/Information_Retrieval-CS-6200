package assignment_4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

public class PageRankSample {

	private static Map<String, Set<String>> inlinkMap = new HashMap<>();
	private static Map<String, Set<String>> outlinkMap = new HashMap<>();
	private static List<Double> convergence = new ArrayList<>();
	private static double pageCount;
	private static Map<String, Double> docPageRank = new HashMap<>();
	private static Set<String> sinkNodes = new HashSet<>();
	private static final Double TELEPORTATION_FACTOR = 0.85;
	private static int iteration = 1;
	private static Set<String> pagesInCorpus = new LinkedHashSet<>();
	
	private static final String CATALOG_DELIMITER_1 = "$#";
	private static final String INLINK_FILE_1 = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 4/corpus_inlinks_3.txt";
	
	private static final String INLINK_FILE_2 = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 4/inlinks.txt";
	private static final String CATALOG_DELIMITER_2 = " ";
	
	private static final String INLINK_FILE = INLINK_FILE_2;
	private static final String CATALOG_DELIMITER = CATALOG_DELIMITER_2;
	
	private static final String RESULT_FILE_1 = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 4/RESULT_CRAWLED.txt";
	private static final String PERPLEXITY_RESULT_FILE_1 = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 4/PERLEXITY_RESULT_CRAWLED.txt";
	
	private static final String RESULT_FILE_2 = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 4/RESULT_WT2G.txt";
	private static final String PERPLEXITY_RESULT_FILE_2 = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 4/PERPEXITY_WT2G.txt";
	
	private static StringBuffer perplexityValues = new StringBuffer();
	
	private static final String RESULT_FILE = RESULT_FILE_2;
	private static final String PERPLEXITY_RESULT_FILE = PERPLEXITY_RESULT_FILE_2;
	
	public static void main(String[] args) {

		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

		deserializeInlinks();
		sinkNodeComputation();
		pageRankComputation();
		printResult();
	}

	private static void printResult() {

		List<Entry<String, Double>> pageRankEntries = new ArrayList<>(docPageRank.entrySet());
		Collections.sort(pageRankEntries, new Comparator<Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {

				if ((o1.getValue() - o2.getValue()) >= 0)
					return -1;
				else
					return 1;
			}

		});

		int rank = 0;
		StringBuffer sb = new StringBuffer();
		String delimeter = "";
		
		List<Entry<String, Double>> topEntries = pageRankEntries.subList(0, 500);
		 for (Entry<String, Double> e : topEntries) {
			 
			 sb.append(delimeter);
			 delimeter = System.lineSeparator();
			 sb.append(++rank);
			 sb.append("\t");
			 sb.append(e.getKey());
			 sb.append("\t");
			 sb.append(e.getValue());
			}
		 
		 writeToFileUtil(sb.toString(), RESULT_FILE);
		 writeToFileUtil(perplexityValues.toString(), PERPLEXITY_RESULT_FILE);

		double sum = 0.0;
		for (Entry<String, Double> e : pageRankEntries) {
			sum += e.getValue();
		}
		System.out.println("Sum= " + sum);
	}


	private static void writeToFileUtil(String content, String fileName) {
		
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

	private static void sinkNodeComputation() {

		System.out.println("Corpus size: " + pagesInCorpus.size());
		System.out.println("Inlink size: " + inlinkMap.size());
		System.out.println("Outlink size: " + outlinkMap.size());
		/*
		 * System.out.println(inlinkMap.size());
		 * System.out.println(outlinkMap.size());
		 */
		// Set<String> inlinkKeys = inlinkMap.keySet();
		Set<String> outlinkKeys = outlinkMap.keySet();

		Set<String> tmp = new HashSet<>(pagesInCorpus);

		tmp.removeAll(outlinkKeys);
		System.out.println("Sink size: " + tmp.size());
		sinkNodes.addAll(tmp);

	}

	private static void pageRankComputation() {

		pageCount = pagesInCorpus.size();
		// initiate values for all documents to 1/N
		initializeDocumentScores();
		pageRankUtil();

		// System.out.println("Complete");
	}

	private static void pageRankUtil() {

		while (!pageRankConverged()) {

			double sinkPR = 0.0;
			/* calculate total sink PR */
			for (String sink : sinkNodes)
				sinkPR += docPageRank.get(sink);

			Map<String, Double> newPageRank = new HashMap<>();
			newPageRank.clear();
			for (String pageRankEntry : pagesInCorpus) {
				String currentPage = pageRankEntry;
				/* teleportation */
				double newPR = Double.valueOf((1 - TELEPORTATION_FACTOR)) / pageCount;
				/* spread remaining sink PR evenly */
				newPR += ((TELEPORTATION_FACTOR * sinkPR) / pageCount);

				if (null != inlinkMap.get(currentPage)) {
					for (String inlink : inlinkMap.get(currentPage)) {
						/* add share of PageRank from in-links */

						if (null == outlinkMap.get(inlink) || null == docPageRank.get(inlink))
							continue;

						Double inlinkPageRank = docPageRank.get(inlink);
						int outLinksCount = outlinkMap.get(inlink).size();

						newPR += (Double.valueOf((TELEPORTATION_FACTOR * inlinkPageRank)) / outLinksCount);
					}
				}
				newPageRank.put(currentPage, newPR);
			}

			Set<String> pages = docPageRank.keySet();
			for (String page : pages) {
				docPageRank.put(page, newPageRank.get(page));
			}

		}
	}

	private static boolean pageRankConverged() {

		double entropy = getShanonEntropy();
		if (convergence.size() < 4)
			convergence.add(entropy);
		else {
			convergence.remove(0);
			convergence.add(entropy);
		}

		return checkConvergence();
	}

	private static boolean checkConvergence() {

		if (convergence.size() < 4)
			return false;
		else {

			/*
			 * int firstEntry = (convergence.get(0).intValue())%10; int
			 * secondEntry = (convergence.get(1).intValue())%10; int thirdEntry
			 * = (convergence.get(2).intValue())%10; int fourthEntry =
			 * (convergence.get(3).intValue())%10;
			 */

			int firstEntry = (convergence.get(0).intValue());
			int secondEntry = (convergence.get(1).intValue());
			int thirdEntry = (convergence.get(2).intValue());
			int fourthEntry = (convergence.get(3).intValue());

			if (firstEntry == secondEntry && secondEntry == thirdEntry & thirdEntry == fourthEntry)
				return true;
		}
		return false;
	}

	private static double getShanonEntropy() {

		double temp = 0.0;
		double log2 = Math.log(2);
		for (Entry<String, Double> pageRankEntry : docPageRank.entrySet()) {
			double currentValue = pageRankEntry.getValue();
			temp += (currentValue * ((Math.log(currentValue)) / log2));
		}

		Double shanonEntropy = Math.pow(2, (-1 * temp));
		//System.out.println((iteration++) + " " + shanonEntropy);
		
		perplexityValues.append(iteration++);
		perplexityValues.append("\t");
		perplexityValues.append(shanonEntropy);
		perplexityValues.append(System.lineSeparator());
		
		return shanonEntropy;
	}

	private static void initializeDocumentScores() {

		double initialValue = Double.valueOf(1.0 / Double.valueOf(pageCount));

		for (String doc : pagesInCorpus)
			docPageRank.put(doc, initialValue);

	}

	private static void deserializeInlinks() {

		System.out.println("Deserialize inlink started");
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(INLINK_FILE));
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() == 0)
					continue;

				// System.out.println(line);
				line = line.replace(CATALOG_DELIMITER, "123_AMIT_456");
				String[] tokens = line.split("123_AMIT_456");
				// System.out.println(tokens.length);
				// System.exit(0);
				String doc = tokens[0].trim();
				pagesInCorpus.add(doc);
				Set<String> sourceNodes = new LinkedHashSet<>();
				for (int i = 1; i < tokens.length; i++) {
					sourceNodes.add(tokens[i].trim());
					// outlink
					addOutlink(tokens[i].trim(), doc);

					pagesInCorpus.add(tokens[i].trim());
				}
				if (inlinkMap.containsKey(doc)) {
					Set<String> existingLinks = inlinkMap.get(doc);
					existingLinks.addAll(sourceNodes);
				} else {
					inlinkMap.put(doc, new HashSet<>(sourceNodes));
				}

			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Deserialize inlink completed");
	}

	private static void addOutlink(String current, String outLink) {

		if (outlinkMap.containsKey(current)) {
			Set<String> existingOutLinks = outlinkMap.get(current);
			existingOutLinks.add(outLink);
			outlinkMap.put(current, existingOutLinks);
		} else {
			Set<String> newOutLinks = new HashSet<>();
			newOutLinks.add(outLink);
			outlinkMap.put(current, newOutLinks);
		}

	}

}
