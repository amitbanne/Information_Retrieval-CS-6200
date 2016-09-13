package assignment_7;

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

	private static List<FeatureUnit> trainingData = new ArrayList<>();
	private static List<FeatureUnit> testingData = new ArrayList<>();
	private static Map<String, Double> testScores;
	private static Map<String, Double> trainScores;
	private static Integer noOfTrainingExamples = 0;
	private static Integer noOfTestingExamples = 0;
//	private static final Integer noOfFeatures = 68;
//	private static final Integer noOfFeatures = 2329544;
	private static final Integer noOfFeatures = 32;
	
	
	private static double[] targetValues;
	private static Feature[][] featureNodeValues;
//	private static final String TRAIN_DATA_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/Feature_Matrix_Train.txt";
//	private static final String TEST_DATA_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/Feature_Matrix_Test.txt";

	
	private static final String TRAIN_DATA_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/CUSTOM_Feature_Matrix_Train.txt";
	private static final String TEST_DATA_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/CUSTOM_Feature_Matrix_Test.txt";

	
//	private static final String TRAIN_DATA_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/Feature_Matrix_Train_Unigram.txt";	
//	private static final String TEST_DATA_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/clean/Feature_Matrix_Test_Unigram.txt";
	private static Model model;
	private static File modelFile;
	private static final String TEST_SCORE_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/TEST_RESULT_UNIGRAM.txt";
//	private static final String TEST_SCORE_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/TEST_RESULT_SPAM_LIST.txt";
//	private static final String TEST_SCORE_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 7/TEST_RESULT_CUSTOM_LIST.txt";
	
	public static void main(String[] args) {

		parseTrainingFeaturesFromFile();
		System.out.println("*** Parsing Training File Complete ***");
		parseTestingFeaturesFromFile();
		System.out.println("*** Parsing Testing File Complete ***");
		prepareTargetValues();
		System.out.println("*** Preparing Target Values Complete ***");
		prepareFeatureNodes();
		System.out.println("*** Preparing FeatureNodes Complete ***");
		trainModel();
		System.out.println("*** Training Model Complete ***");
		//runTest();
		System.out.println("*** Testing Complete ***");
		//dumpTestResults();
		System.out.println("*** Dumping Results Complete ***");
	}

	private static void dumpTestResults() {

		List<Entry<String, Double>> scores = new ArrayList<>(testScores.entrySet());
		Collections.sort(scores, new Comparator<Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				if((o1.getValue()-o2.getValue()) >=0)
					return -1;
				else
					return 1;
			}
		});
		
		StringBuilder sb = new StringBuilder();
		String delimiter = "";
		for (Entry<String, Double> tScore : scores) {
			sb.append(delimiter);
			delimiter=System.lineSeparator();
			sb.append(tScore.getKey());
			sb.append(" ");
			sb.append(tScore.getValue());
		}
		Util.writeToFile(TEST_SCORE_FILE, sb.toString());
	}

	private static void prepareFeatureNodes() {

		featureNodeValues = new Feature[noOfTrainingExamples][];
		int index = 0;
		for (FeatureUnit fUnit : trainingData) {
			featureNodeValues[index] = new Feature[fUnit.getTermFeatures().size()];
			for (int i = 0; i < fUnit.getTermFeatures().size(); i++) {
				Integer fIndex = Integer.parseInt(fUnit.getTermFeatures().get(i).getTerm());
				Integer tf = fUnit.getTermFeatures().get(i).getTf();
				featureNodeValues[index][i] = new FeatureNode(fIndex, tf);
			}
			index++;
		}
	}

	private static void prepareTargetValues() {
		noOfTrainingExamples = trainingData.size();
		targetValues = new double[noOfTrainingExamples];
		int index = 0;
		for (FeatureUnit fUnit : trainingData) {
			targetValues[index++] = fUnit.getLable();
		}
	}

	private static void parseTrainingFeaturesFromFile() {
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(TRAIN_DATA_FILE));
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() > 0) {
					String[] tokens = line.split(" ");

//					String email = tokens[0];
//					Integer lable = Integer.parseInt(tokens[tokens.length - 1]);
					
					String email = "";
					Integer lable = Integer.parseInt(tokens[0]);
					
					List<TermUnit> termFeatures = new ArrayList<>();
//					for (int i = 1; i < tokens.length - 1; i++) {
					for (int i = 1; i < tokens.length; i++) {
						String term = tokens[i].split(":")[0];
						Integer tf = Integer.parseInt(tokens[i].split(":")[1]);
						termFeatures.add(new TermUnit(term, tf));
					}
					trainingData.add(new FeatureUnit(email, termFeatures, lable));
				}

			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void parseTestingFeaturesFromFile() {
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(TEST_DATA_FILE));
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() > 0) {
					String[] tokens = line.split(" ");

//					String email = tokens[0];
//					Integer lable = Integer.parseInt(tokens[tokens.length - 1]);
					
					String email = "";
					Integer lable = Integer.parseInt(tokens[0]);
					
					List<TermUnit> termFeatures = new ArrayList<>();
					//for (int i = 1; i < tokens.length - 1; i++) {
					for (int i = 1; i < tokens.length; i++) {
						String term = tokens[i].split(":")[0];
						Integer tf = Integer.parseInt(tokens[i].split(":")[1]);
						termFeatures.add(new TermUnit(term, tf));
					}
					testingData.add(new FeatureUnit(email, termFeatures, lable));
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void trainModel() {

		Problem problem = new Problem();
		problem.l = noOfTrainingExamples; // number of training examples
		problem.n = noOfFeatures; // number of features
		problem.x = featureNodeValues; // feature nodes
		problem.y = targetValues; // target values

		SolverType solver = SolverType.L2R_LR; // -s 0
//		SolverType solver = SolverType.L2R_L1LOSS_SVR_DUAL; // -s 0
		double C = 1.0; // cost of constraints violation
		double eps = 0.01; // stopping criteria

		Parameter parameter = new Parameter(solver, C, eps);

		model = Linear.train(problem, parameter);
//		modelFile = new File("UNIGRAM-MODEL");
//		modelFile = new File("SPAM-MODEL");
		modelFile = new File("CUSTOM-SPAM-MODEL");
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
		for (FeatureUnit fUnit : testingData) {
			Feature[] instance = new Feature[fUnit.getTermFeatures().size()];
			for (int i = 0; i < fUnit.getTermFeatures().size(); i++) {
				Integer fIndex = Integer.parseInt(fUnit.getTermFeatures().get(i).getTerm());
				Integer tf = fUnit.getTermFeatures().get(i).getTf();
				instance[i] = new FeatureNode(fIndex, tf);
			}
			Double prediction = test(instance);
			testScores.put(fUnit.getEmail(), prediction);
		}

	}

}
