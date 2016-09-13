package assignment_8;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QueryParser {
	private static final String QUERY_FILE = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 8/data/query_desc.51-100.short.txt";

	public static List<String> getQueryList() {

		BufferedReader reader = null;
		String line = "";
		List<String> queryList = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(QUERY_FILE));
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() > 0)
					queryList.add(line.trim());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return queryList;
	}
	
	public static String[] queryTokenizer(String query) {

		String q = query.substring(query.indexOf(".") + 1, query.length() - 1);
		q = q.replaceAll(", ", " ");
		// q=q.substring(0, q.lastIndexOf("."));
		String[] tokens = q.trim().split(" ");
		return tokens;
	}

}
