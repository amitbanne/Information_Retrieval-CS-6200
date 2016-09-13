package assignment_6;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class LibLinear {

	private static List<Integer> testQueryIds;
	private static Map<Integer, LinkedHashMap<String, FeatureUnit>> featureData;
	private static Map<Integer, LinkedHashMap<String, FeatureUnit>> trainingData;
	private static Map<Integer, LinkedHashMap<String, FeatureUnit>> testingData;
	private static Map<Integer, LinkedHashMap<String, Double>> testScores;
	private static Map<Integer, LinkedHashMap<String, Double>> trainScores;
	private static Integer noOfTrainingExamples = 0;
	private static Integer noOfTestingExamples = 0;
	private static final Integer noOfFeatures = 6;
	private static double[] targetValues;
	private static Feature[][] featureNodeValues;
	private static final String DATA_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 6/data/output.txt";
	private static Model model;
	private static File modelFile;
	private static String TEST_SCORE_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 6/data/";
	private static String TRAIN_SCORE_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 6/data/";
	static int count = 0;
	public static void main(String[] args) {

		
		parseFeaturesFromFile();
		initalizeTestingTrainingQueries();
		composeTrainingAndTestingData();
		prepareTargetValues();
		prepareFeatureNodes();
		trainModel();
		System.out.println("*** Training Model Complete ***");
		runTest();
		runTrain();
		System.out.println("*** Testing Complete ***");
		dumpTestResults();
		count=0;
		dumpTrainResults();
		System.out.println("*** Dumping Results Complete ***");
	}

	private static void dumpTrainResults() {

		StringBuilder sb = new StringBuilder();
		for(Integer i:testQueryIds)
			sb.append(i+"_");
		
		TRAIN_SCORE_FILE+=sb.toString()+"Train_Scores.txt";
		
		for (Entry<Integer, LinkedHashMap<String, Double>> tEntry : trainScores.entrySet()) {

			List<Entry<String, Double>> docs = new ArrayList<>(tEntry.getValue().entrySet());
			Collections.sort(docs, new Comparator<Entry<String, Double>>() {

				@Override
				public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {

					if ((o1.getValue() - o2.getValue()) >= 0)
						return -1;
					else
						return 1;
				}
			});

			prepareFileContent(TRAIN_SCORE_FILE, tEntry.getKey(), docs);

		}

	}

	private static void dumpTestResults() {

		StringBuilder sb = new StringBuilder();
		for(Integer i:testQueryIds)
			sb.append(i+"_");
		
		TEST_SCORE_FILE+=sb.toString()+"Test_Scores.txt";
		for (Entry<Integer, LinkedHashMap<String, Double>> tEntry : testScores.entrySet()) {

			List<Entry<String, Double>> docs = new ArrayList<>(tEntry.getValue().entrySet());
			Collections.sort(docs, new Comparator<Entry<String, Double>>() {

				@Override
				public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {

					if ((o1.getValue() - o2.getValue()) >= 0)
						return -1;
					else
						return 1;
				}
			});

			prepareFileContent(TEST_SCORE_FILE, tEntry.getKey(), docs);

		}

	}

	private static void prepareFileContent(String fileName, Integer qId, List<Entry<String, Double>> docs) {

		StringBuffer sb = new StringBuffer();
		int rank = 0;

		for (Entry<String, Double> doc : docs) {
			if(count!=0)
				sb.append(System.lineSeparator());
			count++;
			sb.append(qId);
			sb.append(" ");
			sb.append("Q0");
			sb.append(" ");
			sb.append(doc.getKey());
			sb.append(" ");
			sb.append(++rank);
			sb.append(" ");
			sb.append(doc.getValue());
			sb.append(" ");
			sb.append("EXP");
		}
		Util.writeToFile(fileName, sb.toString());
	}

	private static void prepareFeatureNodes() {

		featureNodeValues = new Feature[noOfTrainingExamples][];
		int index = 0;
		for (Entry<Integer, LinkedHashMap<String, FeatureUnit>> tEntry : trainingData.entrySet()) {
			for (Entry<String, FeatureUnit> dEntry : tEntry.getValue().entrySet()) {
				Double[] values = dEntry.getValue().getFeatures();
				int nonZeroCount = 0;
				for (int i = 0; i < 6; i++) {
					if (!(values[i] == 0.0))
						nonZeroCount++;
				}
				featureNodeValues[index] = new Feature[nonZeroCount];
				for (int i = 0, insertIndex = 0; i < 6; i++) {
					if (!(values[i] == 0.0)) {
						featureNodeValues[index][insertIndex] = new FeatureNode(insertIndex + 1, values[i]);
						insertIndex++;
					}
				}
				index++;
			}
		}
	}

	private static void prepareTargetValues() {

		targetValues = new double[noOfTrainingExamples];
		int index = 0;
		for (Entry<Integer, LinkedHashMap<String, FeatureUnit>> tEntry : trainingData.entrySet()) {
			for (Entry<String, FeatureUnit> dEntry : tEntry.getValue().entrySet()) {
				targetValues[index++] = dEntry.getValue().getFeatures()[6];
			}
		}

	}

	private static void composeTrainingAndTestingData() {
		trainingData = new LinkedHashMap<>();
		testingData = new LinkedHashMap<>();
		for (Integer queryId : featureData.keySet()) {
			if (testQueryIds.contains(queryId)) {
				testingData.put(queryId, featureData.get(queryId));
				noOfTestingExamples += featureData.get(queryId).size();
			} else {
				trainingData.put(queryId, featureData.get(queryId));
				noOfTrainingExamples += featureData.get(queryId).size();
			}
		}
	}

	private static void parseFeaturesFromFile() {
		featureData = new HashMap<>();
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(DATA_FILE));
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() > 0) {
					String[] tokens = line.split(" ");

					Integer queryId = Integer.parseInt(tokens[0]);
					String documentId = tokens[1];

					Double[] features = new Double[7];
					for (int i = 2; i < tokens.length; i++) {
						features[i - 2] = Double.parseDouble(tokens[i]);
					}

					if (featureData.containsKey(queryId)) {
						Map<String, FeatureUnit> existingFeatures = featureData.get(queryId);
						existingFeatures.put(documentId, new FeatureUnit(features));
						featureData.put(queryId, (LinkedHashMap<String, FeatureUnit>) existingFeatures);
					} else {
						Map<String, FeatureUnit> newFeatures = new LinkedHashMap<>();
						newFeatures.put(documentId, new FeatureUnit(features));
						featureData.put(queryId, (LinkedHashMap<String, FeatureUnit>) newFeatures);
					}
				}

			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void initalizeTestingTrainingQueries() {

		/*List<Integer> qIds = new ArrayList<>(featureData.keySet());
		
		Collections.shuffle(qIds);
		
		testQueryIds = new ArrayList<>();
		testQueryIds.addAll(qIds.subList(0, 5));
		*/
		// test queries
		testQueryIds = new ArrayList<>();
		testQueryIds.add(94);
		testQueryIds.add(95);
		testQueryIds.add(97);
		testQueryIds.add(99);
		testQueryIds.add(100);

	}

	private static void trainModel() {

		Problem problem = new Problem();
		problem.l = noOfTrainingExamples; // number of training examples
		problem.n = noOfFeatures; // number of features
		problem.x = featureNodeValues; // feature nodes
		problem.y = targetValues; // target values

		// SolverType solver = SolverType.L2R_LR; // -s 0
		SolverType solver = SolverType.L2R_L1LOSS_SVR_DUAL; // -s 0
		double C = 1.0; // cost of constraints violation
		double eps = 0.01; // stopping criteria

		Parameter parameter = new Parameter(solver, C, eps);
		
		model = Linear.train(problem, parameter);
		modelFile = new File("model");
		try {
			model.save(modelFile);
			// model = Model.load(modelFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Exception");
			e.printStackTrace();
		}

	}

	private static Double test(Feature[] instance) {
		try {
			// load model or use it directly
			model = Model.load(modelFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Exception");
			e.printStackTrace();
		}
		double prediction = Linear.predict(model, instance);
		return prediction;
	}


	private static void runTest() {
		testScores = new LinkedHashMap<>();
		for (Entry<Integer, LinkedHashMap<String, FeatureUnit>> tEntry : testingData.entrySet()) {
			Map<String, Double> documentScoresForQuery = new LinkedHashMap<>();
			for (Entry<String, FeatureUnit> dEntry : tEntry.getValue().entrySet()) {
				Double[] values = dEntry.getValue().getFeatures();
				int nonZeroCount = 0;
				for (int i = 0; i < 6; i++) {
					if (!(values[i] == 0.0))
						nonZeroCount++;
				}
				Feature[] instance = new Feature[nonZeroCount];
				for (int i = 0, insertIndex = 0; i < 6; i++) {
					if (!(values[i] == 0.0)) {
						instance[insertIndex] = new FeatureNode(insertIndex + 1, values[i]);
						insertIndex++;
					}
				}
				Double prediction = test(instance);
				documentScoresForQuery.put(dEntry.getKey(), prediction);
			}
			testScores.put(tEntry.getKey(), (LinkedHashMap<String, Double>) documentScoresForQuery);
		}

	}

	private static void runTrain() {
		trainScores = new LinkedHashMap<>();
		for (Entry<Integer, LinkedHashMap<String, FeatureUnit>> tEntry : trainingData.entrySet()) {
			Map<String, Double> documentScoresForQuery = new LinkedHashMap<>();
			for (Entry<String, FeatureUnit> dEntry : tEntry.getValue().entrySet()) {
				Double[] values = dEntry.getValue().getFeatures();
				int nonZeroCount = 0;
				for (int i = 0; i < 6; i++) {
					if (!(values[i] == 0.0))
						nonZeroCount++;
				}
				Feature[] instance = new Feature[nonZeroCount];
				for (int i = 0, insertIndex = 0; i < 6; i++) {
					if (!(values[i] == 0.0)) {
						instance[insertIndex] = new FeatureNode(insertIndex + 1, values[i]);
						insertIndex++;
					}
				}
				Double prediction = test(instance);
				documentScoresForQuery.put(dEntry.getKey(), prediction);
			}
			trainScores.put(tEntry.getKey(), (LinkedHashMap<String, Double>) documentScoresForQuery);
		}

	}

}
