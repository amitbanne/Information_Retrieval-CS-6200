package assignment_8;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClusterMatrix {

	private static final String QUERY_COMBINATIONS = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 8/data/cluster/QueryCombinations.txt";
	private static final String CLUSTER_DATA = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 8/data/cluster/CLUSTER_OUTPUT1.arff";
	private static Map<String, Integer> documentClusterMap = new HashMap<>();
	private static Integer[][] cluster = new Integer[2][2];

	public static void main(String[] args) {
		cluster[0][0] = 0;
		cluster[0][1] = 0;
		cluster[1][0] = 0;
		cluster[1][1] = 0;
		parseDocClusterData();
		parseQueryCombinations();
		
		System.out.println(cluster[0][0]+"\t"+cluster[0][1]);
		System.out.println(cluster[1][0]+"\t"+cluster[1][1]);
		
	}

	private static void parseQueryCombinations() {
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(QUERY_COMBINATIONS));
			while ((line = reader.readLine()) != null) {

				String tokens[] = line.split("&&");
				Integer q1 = Integer.parseInt(tokens[0].split(":")[0].trim());
				Integer q2 = Integer.parseInt(tokens[1].split(":")[0].trim());

				String doc1 = tokens[0].split(":")[1].trim();
				String doc2 = tokens[1].split(":")[1].trim();

				if (q1 == q2) {
					if (documentClusterMap.get(doc1) == documentClusterMap.get(doc2)) {
						cluster[0][0]+=1;
					}else{
						cluster[0][1]+=1;
					}
				} else {
					if (documentClusterMap.get(doc1) == documentClusterMap.get(doc2)) {
						cluster[1][0]+=1;
					}else{
						cluster[1][1]+=1;
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

	private static void parseDocClusterData() {

		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(CLUSTER_DATA));
			while ((line = reader.readLine()) != null) {
				if (!line.contains("AP89"))
					continue;

				String tokens[] = line.split(",");
				String doc = tokens[1].trim();
				String clusterString = tokens[tokens.length - 1].trim();
				Integer cluster = Integer.parseInt(clusterString.split("cluster")[1]);
				documentClusterMap.put(doc, cluster);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
