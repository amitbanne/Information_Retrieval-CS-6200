package assignment_5;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TrecEval {

	// assignment 1 files
/*	private static String Q_REL_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 5/result_files/QrelFileMerged.txt";
	private static String RANKED_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 5/result_files/RankFile.txt";
*/
	
	private static String Q_REL_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 5/result_files/qrels.adhoc.51-100.AP89.txt";
	private static String RANKED_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 5/result_files/Trec-Text.txt";
	
	// assignment 5 files
	// private static String Q_REL_FILE = "F:/Northeastern
	// University/COURSES/Summer 2016/Assignments/Assignment
	// 5/result_files/qRelFile.txt";
	// private static String RANKED_FILE = "F:/Northeastern
	// University/COURSES/Summer 2016/Assignments/Assignment
	// 5/result_files/rankedFile.txt";

	private static Map<Long, LinkedHashMap<String, List<Integer>>> qRelMap = new LinkedHashMap<>();
	//private static Map<Long, ArrayList<String>> rankedDocumentMap = new LinkedHashMap<>();
	private static Map<Long, Map<String, Double>> rankedDocumentMap = new LinkedHashMap<>();
	private static Map<Long, ScoreUnit> queryScoreMap = new LinkedHashMap<>();
	private static final int[] ranksToCheck = { 5, 10, 20, 50, 100 };
	private static final int DOCS_TO_BE_CONSIDERED = 200;
	
//	private static final int[] ranksToCheck = { 5, 10, 15, 20, 50, 100, 200, 500, 1000 };
//	private static final int DOCS_TO_BE_CONSIDERED = 1000;
	
	private static Map<Long, LinkedHashMap<String, Integer>> relevanceVector = new LinkedHashMap<>();
	private static Map<Long, LinkedHashMap<String, Integer>> gradeVector = new LinkedHashMap<>();
	
	private static Map<Long, HashMap<Integer, Double>> precisionAtKMap = new LinkedHashMap<>();
	private static Map<Long, HashMap<Integer, Double>> interpolatedPrecisionAtKMap = new LinkedHashMap<>();
	private static Map<Long, HashMap<Integer, Double>> recallAtKMap = new LinkedHashMap<>();

	public static void main(String[] args) {
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		/*if(args.length <2 || args.length>3){
			System.out.println("Invalid number of parameters");
			System.out.println("Permitted parameters: -q[optional, to view results for each query] QRelFileName RankedFileName");
			System.exit(0);
		}
	*/	boolean qMode = true;
		/*if(args.length ==2){
			Q_REL_FILE = args[0];
			RANKED_FILE = args[1];
		}else if(args.length==3){
			Q_REL_FILE = args[1];
			RANKED_FILE = args[2];
			if(args[0].equals("-q"))
				qMode = true;
		}
		*/
		
		deserializeQRel();
		deserializeRankedFile();
		computeScores();
		if (qMode) {
			printQModeResults();
		} else {
			printCombinedResults();
		}
		dumpPrecisionRecallScores();
	}

	private static void printCombinedResults() {

		Double rPrecisionMean = 0.0;
		Double avgPrecisionMean = 0.0;
		Double[] nDCGMean = new Double[ranksToCheck.length];
		Double[] precisionAtKMean = new Double[ranksToCheck.length];
		Double[] recallAtKMean = new Double[ranksToCheck.length];
		Double[] f1AtKMean = new Double[ranksToCheck.length];

		for (int i = 0; i < ranksToCheck.length; i++) {
			precisionAtKMean[i] = 0.0;
			recallAtKMean[i] = 0.0;
			f1AtKMean[i] = 0.0;
			nDCGMean[i] = 0.0;
		}

		long docsRetrieved = 0;
		long relevantDocs = 0;
		long relevantDocsRetrieved = 0;

		// compute sums over all queries
		for (Entry<Long, ScoreUnit> e : queryScoreMap.entrySet()) {
			rPrecisionMean += e.getValue().getrPrecision();
			avgPrecisionMean += e.getValue().getAvgPrecision();

			for (int k = 0; k < ranksToCheck.length; k++) {
				precisionAtKMean[k] += e.getValue().getPrecisionAtK()[k];
				recallAtKMean[k] += e.getValue().getRecallAtK()[k];
				f1AtKMean[k] += e.getValue().getF1AtK()[k];
				nDCGMean[k] += e.getValue().getnDCG()[k];				
			}

			docsRetrieved += e.getValue().getRetrievedDocs();
			relevantDocs += e.getValue().getRelevantDocs();
			relevantDocsRetrieved += e.getValue().getRelevantDocsRetrieved();
		}

		// compute average by dividing sums by number of queries
		rPrecisionMean /= queryScoreMap.size();
		avgPrecisionMean /= queryScoreMap.size();
		for (int k = 0; k < ranksToCheck.length; k++) {
			precisionAtKMean[k] /= queryScoreMap.size();
			recallAtKMean[k] /= queryScoreMap.size();
			f1AtKMean[k] /= queryScoreMap.size();
			nDCGMean[k] /= queryScoreMap.size();
		}

		// print results
		System.out.println("Queryid(num): " + queryScoreMap.size());
		System.out.println("Total number of documents over all queries");
		System.out.println("Retrieved:  " + docsRetrieved);
		System.out.println("Relevant:  " + relevantDocs);
		System.out.println("Rel_ret:  " + relevantDocsRetrieved);
		System.out.println(
				"Average precision (non-interpolated) for all rel docs(averaged over queries):   " + avgPrecisionMean);

		System.out.println();
		System.out.println("Precision:");
		for (int k = 0; k < ranksToCheck.length; k++) {
			System.out.println("At " + ranksToCheck[k] + " docs: " + precisionAtKMean[k]);
		}
		System.out.println();
		System.out.println("Recall:");
		for (int k = 0; k < ranksToCheck.length; k++) {
			System.out.println("At " + ranksToCheck[k] + " docs: " + recallAtKMean[k]);
		}
		System.out.println();
		System.out.println("F1:");
		for (int k = 0; k < ranksToCheck.length; k++) {
			System.out.println("At " + ranksToCheck[k] + " docs: " + f1AtKMean[k]);
		}
		System.out.println();
		System.out.println("R-Precision (precision after R (= num_rel for a query) docs retrieved):  ");
		System.out.println("Exact: " + rPrecisionMean);
		System.out.println();
		System.out.println("nDCG:");
		for (int k = 0; k < ranksToCheck.length; k++) {
			System.out.println("At " + ranksToCheck[k] + " docs: " + nDCGMean[k]);
		}
	}

	private static void printQModeResults() {

		
		
		for (Entry<Long, ScoreUnit> e : queryScoreMap.entrySet()) {
			
			Double rPrecisionMean = 0.0;
			Double avgPrecisionMean = 0.0;
			Double[] nDCGMean = new Double[ranksToCheck.length];
			Double[] precisionAtKMean = new Double[ranksToCheck.length];
			Double[] recallAtKMean = new Double[ranksToCheck.length];
			Double[] f1AtKMean = new Double[ranksToCheck.length];

			for (int i = 0; i < ranksToCheck.length; i++) {
				precisionAtKMean[i] = 0.0;
				recallAtKMean[i] = 0.0;
				f1AtKMean[i] = 0.0;
				nDCGMean[i] = 0.0;
			}

			long docsRetrieved = 0;
			long relevantDocs = 0;
			long relevantDocsRetrieved = 0;

			
			rPrecisionMean += e.getValue().getrPrecision();
			avgPrecisionMean += e.getValue().getAvgPrecision();

			for (int k = 0; k < ranksToCheck.length; k++) {
				precisionAtKMean[k] = e.getValue().getPrecisionAtK()[k];
				recallAtKMean[k] = e.getValue().getRecallAtK()[k];
				f1AtKMean[k] = e.getValue().getF1AtK()[k];
				nDCGMean[k] = e.getValue().getnDCG()[k];				
			}

			docsRetrieved = e.getValue().getRetrievedDocs();
			relevantDocs = e.getValue().getRelevantDocs();
			relevantDocsRetrieved = e.getValue().getRelevantDocsRetrieved();
			
			System.out.println("Queryid(num): " + e.getKey());
			System.out.println("Total number of documents over all queries");
			System.out.println("Retrieved:  " + docsRetrieved);
			System.out.println("Relevant:  " + relevantDocs);
			System.out.println("Rel_ret:  " + relevantDocsRetrieved);
			System.out.println(
					"Average precision (non-interpolated) for all rel docs(averaged over queries):   " + avgPrecisionMean);

			System.out.println();
			System.out.println("Precision:");
			for (int k = 0; k < ranksToCheck.length; k++) {
				System.out.println("At " + ranksToCheck[k] + " docs: " + precisionAtKMean[k]);
			}
			System.out.println();
			System.out.println("Recall:");
			for (int k = 0; k < ranksToCheck.length; k++) {
				System.out.println("At " + ranksToCheck[k] + " docs: " + recallAtKMean[k]);
			}
			System.out.println();
			System.out.println("F1:");
			for (int k = 0; k < ranksToCheck.length; k++) {
				System.out.println("At " + ranksToCheck[k] + " docs: " + f1AtKMean[k]);
			}
			System.out.println();
			System.out.println("R-Precision (precision after R (= num_rel for a query) docs retrieved):  ");
			System.out.println("Exact: " + rPrecisionMean);
			System.out.println();
			System.out.println("nDCG:");
			for (int k = 0; k < ranksToCheck.length; k++) {
				System.out.println("At " + ranksToCheck[k] + " docs: " + nDCGMean[k]);
			}
			
			System.out.println("***********************************************************");
		}
		
	}

	private static void deserializeRankedFile() {
		String fileName = RANKED_FILE;
		BufferedReader reader = null;
		String line = "";
		try {

			reader = new BufferedReader(new FileReader(fileName));
			while ((line = reader.readLine()) != null) {

				if (line.trim().length() == 0)
					continue;

				long queryId = 0;
				String docNo = "";
				String[] tokens = line.split(" ");
				Double score = 0.0;
				if(tokens.length==2){
					// assignment 5 format
					 queryId = Long.parseLong(tokens[0].trim());
					 docNo = tokens[1];
				}else{
					// assignment 1 format
					queryId = Long.parseLong(tokens[0].trim());
					docNo = tokens[2].trim();
					score = Double.parseDouble(tokens[4].trim());
				}
				
				if (rankedDocumentMap.containsKey(queryId)) {
					Map<String, Double> existingDocList = rankedDocumentMap.get(queryId);
					/*if (existingDocList.size() < 1000) {
						existingDocList.p(docNo);
						rankedDocumentMap.put(queryId, (ArrayList<String>) existingDocList);
					}*/
					existingDocList.put(docNo, score);
					rankedDocumentMap.put(queryId, existingDocList);
				} else {
					Map<String, Double> newDocList = new HashMap<>();
					newDocList.put(docNo, score);
					rankedDocumentMap.put(queryId, newDocList);
				}
			}
			reader.close();
			Map<Long, Map<String, Double>> tmpMap = new HashMap<>();
			tmpMap.putAll(rankedDocumentMap);
			for(Entry<Long, Map<String, Double>> qEntry : tmpMap.entrySet()){
				
				Map<String, Double> docMap = qEntry.getValue();
				List<Entry<String, Double>> docs = new ArrayList<>(docMap.entrySet());
				Collections.sort(docs, new Comparator<Entry<String, Double>>() {

					@Override
					public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {

						if((o1.getValue() - o2.getValue())>=0)
							return -1;
						else
							return 1;
					}
					
				});
				
				List<Entry<String, Double>> topDocs = docs.subList(0, DOCS_TO_BE_CONSIDERED);
				Map<String, Double> sortedMap = new LinkedHashMap<>();
				for(Entry<String, Double> e: topDocs){
					sortedMap.put(e.getKey(), e.getValue());
				}
				rankedDocumentMap.put(qEntry.getKey(), sortedMap);
			}
			
			computeGradeVectors();
			computeRelevanceVectors();

		} catch (FileNotFoundException e) {
			System.out.println("Oops..Problem in reading Ranked File.. Please try again..");
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Oops..Problem in processing Ranked File.. Please try again..");
			System.exit(0);
		}
	}

	private static void deserializeQRel() {
		// TODO Auto-generated method stub
		String fileName = Q_REL_FILE;
		BufferedReader reader = null;
		String line = "";
		try {

			reader = new BufferedReader(new FileReader(fileName));
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(" ");

				// assignment 1 format

				long queryId = Long.parseLong(tokens[0]);
				String docNo = tokens[2];
				// int relevance = Integer.parseInt(tokens[3]);
				int grade = Integer.parseInt(tokens[3]);
				// assignment 5 format
				/*
				 * long queryId = Long.parseLong(tokens[0]); String docNo =
				 * tokens[1]; int relevance = Integer.parseInt(tokens[2]);
				 */
				if (qRelMap.containsKey(queryId)) {
					Map<String, List<Integer>> existingDocs = qRelMap.get(queryId);
					if (existingDocs.containsKey(docNo)) {
						List<Integer> existingGrades = existingDocs.get(docNo);
						existingGrades.add(grade);
						existingDocs.put(docNo, existingGrades);
					} else {
						List<Integer> newGrades = new ArrayList<>();
						newGrades.add(grade);
						existingDocs.put(docNo, newGrades);
					}
					qRelMap.put(queryId, (LinkedHashMap<String, List<Integer>>) existingDocs);
				} else {
					LinkedHashMap<String, List<Integer>> newDocs = new LinkedHashMap<>();
					List<Integer> grades = new ArrayList<>();
					grades.add(grade);
					newDocs.put(docNo, grades);
					qRelMap.put(queryId, newDocs);
				}

			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.out.println("Oops..Problem in reading QREL File.. Please try again..");
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Oops..Problem in processing QREL File.. Please try again..");
			System.exit(0);
		}
	}

	private static void computeScores() {

		// compute scores for individual queries
		for (Long queryId : rankedDocumentMap.keySet()) {

			List<String> docs = new ArrayList<>(rankedDocumentMap.get(queryId).keySet());
			
			//compute precisionAtk for all ranks k
			computePrecisionAtK(queryId, docs);

			//compute recallAtk for all ranks k
			computeRecallAtK(queryId, docs);
			
			
			//compute interpolated precision values for all ranks k
			computeInterpolatedPrecisionAtk(queryId);
			
			Double rPrecision = rPrecision(queryId, docs);
			Double avgPrecision = averagePrecision(queryId, docs);
			Double[] f1AtK = f1AtKUtil(queryId, docs);
			Double[] precisionAtK = precisionAtKUtil(queryId, docs);
			Double[] recallAtK = recallAtKUtil(queryId, docs);
			Double[] nDCG = nDCGUtil(queryId, docs);

			long retrievedDocs = docs.size();
			long relevant = getRelevantCountFromQrel(queryId);
			long relevantRetrieved = getRelevantDocCount(queryId, docs);

			queryScoreMap.put(queryId, new ScoreUnit(queryId, rPrecision, avgPrecision, precisionAtK, recallAtK, f1AtK, nDCG, retrievedDocs, relevant, relevantRetrieved));
		}
	}

	/* MODELS START */

	public static double rPrecision(long queryId, List<String> docs) {

		int relevantCountFromQrel = getRelevantCountFromQrel(queryId);
		// compute precision at rank equal to number of relevant
		// docs for the query from QREL File
		// If R=15, compute precision at rank=15 which will be rPrecision
		Double rPrecision = precisionAtK(queryId, relevantCountFromQrel, docs);
		return rPrecision;

	}

	public static double averagePrecision(long queryId, List<String> docs) {
		// get total relevant document count for queryId
		int relevantCountFromQrel = getRelevantCountFromQrel(queryId);
		Map<String, Double> precisionsAtK = new HashMap<>();

		// compute precision at k=1,2...max for documents
		for (int k = 0; k < docs.size(); k++) {
			//double precisionAtK = precisionAtK(queryId, (k + 1), docs);
			double precisionAtK = precisionAtKMap.get(queryId).get(k+1);
			precisionsAtK.put(docs.get(k), precisionAtK);
		}

		// compute sum of precisionAtK values for all relevant documents
		double relevanPrecisionSum = 0;
		for (String doc : docs) {
			if (null != qRelMap.get(queryId).get(doc) && relevanceVector.get(queryId).get(doc) == 1)
				relevanPrecisionSum += precisionsAtK.get(doc);
		}
		double averagePrecision = relevanPrecisionSum / Double.valueOf(relevantCountFromQrel);
		return averagePrecision;
	}

	public static double precisionAtK(long queryId, int k, List<String> docs) {

		List<String> kDocs = docs.subList(0, k);
		double relevantCount = getRelevantDocCount(queryId, kDocs);
		//precision@K = (noOfRelevantDocsRetrieved)/(noOfDocsRetrieved) at rank k
		double precision = relevantCount / (Double.valueOf(k));
		return precision;
	}

	public static double recallAtK(long queryId, int k, List<String> docs, int totalRelevant) {

		List<String> kDocs = docs.subList(0, k);
		double relevantCount = getRelevantDocCount(queryId, kDocs);
		//precision@K = (noOfRelevantDocsRetrieved)/(totalNoOfRelevantDocsFromQREL) at rank k
		double recall = relevantCount / (Double.valueOf(totalRelevant));
		return recall;
	}

	public static double f1AtK(long queryId, int k, List<String> docs, int totalRelevant) {

		// get precision@k
		double precisionAtK = precisionAtK(queryId, k, docs);
		// get recall@k
		double recallAtK = recallAtK(queryId, k, docs, totalRelevant);

		double f1AtK = 0.0;
		// f1@k is harmonic mean of precision@k and recall@k
		if (!(precisionAtK == 0.0 && recallAtK == 0.0))
			f1AtK = (Double.valueOf((2 * precisionAtK * recallAtK)) / (precisionAtK + recallAtK));

		return f1AtK;
	}


	private static Double nDCGAtK(Long queryId, List<String> docs, int k){
		
		List<String> kDocs = docs.subList(0, k);
		List<Integer> grades = new ArrayList<>();
		for(int i=0;i<k;i++){
			if(null!= gradeVector.get(queryId).get(kDocs.get(i)))
				grades.add(gradeVector.get(queryId).get(kDocs.get(i)));
			else
				grades.add(0);
		}
		
		
			
		
		//sum of grade/lg(rank)
		double dcg= grades.get(0);
		for(int i=1; i< grades.size();i++){
			dcg+=(Double.valueOf(grades.get(i))/((Math.log(i+1))/(Math.log(2))));
		}
		
		if(dcg==0.0)
			return 0.0;
		
		//sort grades
		List<Integer> sortedGrades = new ArrayList<>();
		sortedGrades.addAll(grades);
		Collections.sort(sortedGrades, new Comparator<Integer>() {

			@Override
			public int compare(Integer grade1, Integer grade2) {
				if(grade1>=grade2)
					return -1;
				else 
					return 1;
				
			}
			
		});
		
		double sortedDCG= sortedGrades.get(0);
		for(int i=1; i< sortedGrades.size();i++){
			sortedDCG+=(Double.valueOf(sortedGrades.get(i))/(Math.log(i+1)/Math.log(2)));
		}
		
		//nDCG = dcg/sortedDCG
		double nDCG = dcg/sortedDCG;
		
		return nDCG;
	}
	
	/* MODELS END */

	/* HELPER METHODS START */


	private static Double[] nDCGUtil(Long queryId, List<String> docs) {
		Double[] nDCGScores = new Double[ranksToCheck.length];
		for(int k=0;k<ranksToCheck.length;k++){
			nDCGScores[k] = nDCGAtK(queryId, docs, ranksToCheck[k]);
		}
		
		return nDCGScores;
	}
	
	private static int getRelevantCountFromQrel(Long queryId) {

		int relevantCount = 0;
		for (Entry<String, List<Integer>> e : qRelMap.get(queryId).entrySet()) {
				relevantCount += relevanceVector.get(queryId).get(e.getKey());
		}
		return relevantCount;
	}

	private static int getRelevantDocCount(long queryId, List<String> docs) {

		int relevantCount = 0;
		for (String doc : docs) {
			if (null != qRelMap.get(queryId).get(doc)) {
				relevantCount += relevanceVector.get(queryId).get(doc);
			}

		}
		return relevantCount;
	}

	private static Double[] f1AtKUtil(Long queryId, List<String> docs) {

		Double[] f1AtKScores = new Double[ranksToCheck.length];
		// get total relevant document count for queryId
		int relevantCountFromQrel = getRelevantCountFromQrel(queryId);
		int index = 0;
		for (int k : ranksToCheck) {
			Double f1AtK = f1AtK(queryId, k, docs, relevantCountFromQrel);
			f1AtKScores[index++] = f1AtK;
		}
		return f1AtKScores;
	}

	private static Double[] recallAtKUtil(Long queryId, List<String> docs) {
		Double[] recallAtKScores = new Double[ranksToCheck.length];
		// get total relevant document count for queryId
		int index = 0;
		for (int k : ranksToCheck) {
			//Double recAtK = recallAtK(queryId, k, docs, relevantCountFromQrel);
			// fetch precision from precision map, that has been populated for all ranks
			  Double recAtK = recallAtKMap.get(queryId).get(k);
			recallAtKScores[index++] = recAtK;
		}
		return recallAtKScores;
	}

	private static Double[] precisionAtKUtil(Long queryId, List<String> docs) {
		Double[] precisionAtKScores = new Double[ranksToCheck.length];
		int index = 0;
		for (int k : ranksToCheck) {
			//Double precAtK = precisionAtK(queryId, k, docs);
			// fetch precision from precision map, that has been populated for all ranks
			Double precAtK = precisionAtKMap.get(queryId).get(k);
			
			precisionAtKScores[index++] = precAtK;
		}
		return precisionAtKScores;
	}

	private static void computeRelevanceVectors() {

		for (Entry<Long, LinkedHashMap<String, List<Integer>>> e : qRelMap.entrySet()) {
			for (Entry<String, List<Integer>> en : e.getValue().entrySet()) {
				int relSum = 0;
				for (int i : en.getValue())
					relSum += i;

				// sum of grades/(2 * noOfGrades) >= 0.5, rel=1, else rel=0
				int relevance = (((Double.valueOf(relSum) / (2 * en.getValue().size()))>=0.50 ? 1 : 0));
				if (relevanceVector.containsKey(e.getKey())) {
					LinkedHashMap<String, Integer> existingDocs = relevanceVector.get(e.getKey());
					existingDocs.put(en.getKey(), relevance);
					relevanceVector.put(e.getKey(), existingDocs);
				} else {
					LinkedHashMap<String, Integer> newDocs = new LinkedHashMap<>();
					newDocs.put(en.getKey(), relevance);
					relevanceVector.put(e.getKey(), newDocs);
				}
			}
		}
	}

	private static void computeGradeVectors() {

		for (Entry<Long, LinkedHashMap<String, List<Integer>>> e : qRelMap.entrySet()) {
			for (Entry<String, List<Integer>> en : e.getValue().entrySet()) {
				double relSum = 0.0;
				for (int i : en.getValue()){
					relSum += i;
				}

				// grade = sum of grades/noOfGrades
				int grade = (int) Math.round(relSum/ en.getValue().size());
				if (gradeVector.containsKey(e.getKey())) {
					LinkedHashMap<String, Integer> existingDocs = gradeVector.get(e.getKey());
					existingDocs.put(en.getKey(), grade);
					gradeVector.put(e.getKey(), existingDocs);
				} else {
					LinkedHashMap<String, Integer> newDocs = new LinkedHashMap<>();
					newDocs.put(en.getKey(), grade);
					gradeVector.put(e.getKey(), newDocs);
				}
			}
		}
	}

	private static void computeRecallAtK(Long queryId, List<String> docs) {
		int relevantCountFromQrel = getRelevantCountFromQrel(queryId);
		
		Map<Integer, Double> scoresAtRanks = new HashMap<>();
		for(int k=0;k<docs.size();k++){
			Double recAtK = recallAtK(queryId, k+1, docs, relevantCountFromQrel);
			scoresAtRanks.put(k+1, recAtK);
		}
		recallAtKMap.put(queryId, (HashMap<Integer, Double>) scoresAtRanks);
		
	}

	private static void computePrecisionAtK(Long queryId, List<String> docs) {
		
		Map<Integer, Double> scoresAtRanks = new HashMap<>();
		for(int k=0;k<docs.size();k++){
			Double precAtK = precisionAtK(queryId, k+1, docs);
			scoresAtRanks.put(k+1, precAtK);
		}
		precisionAtKMap.put(queryId, (HashMap<Integer, Double>) scoresAtRanks);
	}
	
	private static void dumpPrecisionRecallScores() {
		dumpScores(precisionAtKMap,"Precision");
		dumpScores(recallAtKMap, "Recall");
	}

	private static void dumpScores(Map<Long, HashMap<Integer, Double>> scoreMap, String fileType) {
		
		for(Entry<Long, HashMap<Integer, Double>> queryEntry : scoreMap.entrySet()){
			
			Long queryId = queryEntry.getKey();
			Map<Integer, Double> scores = queryEntry.getValue();
			StringBuffer sb = new StringBuffer();
			String delimeter = "";
			for(Entry<Integer, Double> rankEntry : scores.entrySet()){
				if(fileType.equals("Precision")){
					double interpolatedValue = interpolatedPrecisionAtKMap.get(queryEntry.getKey()).get(rankEntry.getKey());
					sb.append(delimeter);
					delimeter=System.lineSeparator();
					sb.append(rankEntry.getValue());
					sb.append("\t");
					sb.append(interpolatedValue);
				}else{
					sb.append(delimeter);
					delimeter=System.lineSeparator();
					sb.append(rankEntry.getValue());
				}
				
			}
			writeToFile(fileType, queryId, sb.toString());
		}
	}

	private static void writeToFile(String fileType, Long queryId, String content) {

		String fileName = fileType+"_"+queryId+".txt";
		File file = new File(fileName);
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

	private static void computeInterpolatedPrecisionAtk(Long queryId) {

		List<Double> precisionValues = new ArrayList<>(precisionAtKMap.get(queryId).values());
		Map<Integer, Double> interpolatedPrecisionValues = new HashMap<>();
		
		double lastValue = precisionValues.get(precisionValues.size()-1);
		for(int i= (precisionValues.size()-1); i>=0;i--){
			double currentValue = precisionValues.get(i);
			if(currentValue>lastValue){
				lastValue = currentValue;
				interpolatedPrecisionValues.put(i+1, lastValue);
			}else
				interpolatedPrecisionValues.put(i+1, lastValue);
		}
		
		interpolatedPrecisionAtKMap.put(queryId,  (HashMap<Integer, Double>) interpolatedPrecisionValues);
	}

	/* HELPER METHODS END */
}