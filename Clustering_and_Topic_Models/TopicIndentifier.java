package assignment_8;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TopicIndentifier {
	private static final String DIRECTORY = "F:/Northeastern University/COURSES/Summer 2016/Assignments/Assignment 8/data";
	private static Integer[] queries = { 85, 59, 56, 71, 64, 62, 93, 99, 58, 77, 54, 87, 94, 100, 89, 61, 95, 68, 57,
			97, 98, 60, 80, 63, 91 };
	private static Integer query = 0;
	private static String MODEL_FILE = DIRECTORY + "/model/" + query + "-model.txt";
	private static String VOCAB_FILE = DIRECTORY + "/vocab/" + query + "-vocab.txt";
	private static String TOP_TERMS_FILE = DIRECTORY + "/TOP_TERMS/" + query + "-TOP_TERMS.txt";
	private static Map<Integer, String> idTermMap = new LinkedHashMap<>();
	private static List<TopicTerm> topicTermList = new ArrayList<>();
	private static Map<Integer, List<TermScore>> topTermsForTopic = new LinkedHashMap<>();
	private static final Integer NO_OF_TOPICS = 20;
	private static final Integer NO_OF_TOP_WORDS = 30;
	
	
	public static void main(String[] args) {
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

		for (int i : queries) {
			query = i;
			MODEL_FILE = DIRECTORY + "/model/" + query + "-model.txt";
			VOCAB_FILE = DIRECTORY + "/vocab/" + query + "-vocab.txt";
			TOP_TERMS_FILE = DIRECTORY + "/TOP_TERMS/" + query + "-TOP_TERMS.txt";
			
			parseVocabFile();
			parseModelFile();
			
			for(int k=1;k<=NO_OF_TOPICS;k++)
				identifyTopTermsForTopic(k);
			
			dumpTopTerms();
			System.out.println("*** COMPLETED " + query + " ***");
		}
		System.out.println("*** All Complete ***");
	}

	private static void dumpTopTerms() {
		StringBuffer sb = new StringBuffer();
		sb.append("QUERY ID: " + query);
		sb.append(System.lineSeparator());
		String del = "";
		
		for(int i=0;i<NO_OF_TOPICS;i++)
			sb.append("TOPIC-"+(i+1)+"\t");
		
		sb.append(System.lineSeparator());
		
		/*for (Entry<Integer, List<TermScore>> topicEntry : topTermsForTopic.entrySet()) {
			sb.append(del);
			sb.append(del);
			del = System.lineSeparator();
			sb.append("TOPIC: " + topicEntry.getKey());
			sb.append(del);
			sb.append(getTermStringForTopic(topicEntry.getValue()));
		}*/
		
		for(int i=0;i<NO_OF_TOP_WORDS;i++){
			del="";
			for(Integer t: topTermsForTopic.keySet()){
				sb.append(del);
				del=" ";
				sb.append(idTermMap.get(Integer.parseInt(topTermsForTopic.get(t).get(i).getTerm().trim())));
				sb.append(":");
				sb.append(topTermsForTopic.get(t).get(i).getScore());
			}
			sb.append(System.lineSeparator());
		}

		Util.writeToFile(TOP_TERMS_FILE, sb.toString());
	}

	/*private static String getTermStringForTopic(List<TermScore> list) {
		StringBuffer sb = new StringBuffer();
		String del = "";
		for (String term : list) {
			sb.append(del);
			del = ", ";
			sb.append(term);
		}

		return sb.toString();
	}
*/
	private static void identifyTopTermsForTopic(final int topic) {

		List<TopicTerm> termData = new ArrayList<>();
		termData.addAll(topicTermList);
		Collections.sort(termData, new Comparator<TopicTerm>() {

			@Override
			public int compare(TopicTerm o1, TopicTerm o2) {

				switch (topic) {
				case 1:
					if ((o1.getTopic1() - o2.getTopic1()) >= 0)
						return -1;
					else
						return 1;
				case 2:
					if ((o1.getTopic2() - o2.getTopic2()) >= 0)
						return -1;
					else
						return 1;
				case 3:
					if ((o1.getTopic3() - o2.getTopic3()) >= 0)
						return -1;
					else
						return 1;
				case 4:
					if ((o1.getTopic4() - o2.getTopic4()) >= 0)
						return -1;
					else
						return 1;
				case 5:
					if ((o1.getTopic5() - o2.getTopic5()) >= 0)
						return -1;
					else
						return 1;
				case 6:
					if ((o1.getTopic6() - o2.getTopic6()) >= 0)
						return -1;
					else
						return 1;
				case 7:
					if ((o1.getTopic7() - o2.getTopic7()) >= 0)
						return -1;
					else
						return 1;
				case 8:
					if ((o1.getTopic8() - o2.getTopic8()) >= 0)
						return -1;
					else
						return 1;
				case 9:
					if ((o1.getTopic9() - o2.getTopic9()) >= 0)
						return -1;
					else
						return 1;
				case 10:
					if ((o1.getTopic10() - o2.getTopic10()) >= 0)
						return -1;
					else
						return 1;
				case 11:
					if ((o1.getTopic11() - o2.getTopic11()) >= 0)
						return -1;
					else
						return 1;
				case 12:
					if ((o1.getTopic12() - o2.getTopic12()) >= 0)
						return -1;
					else
						return 1;
				case 13:
					if ((o1.getTopic13() - o2.getTopic13()) >= 0)
						return -1;
					else
						return 1;
				case 14:
					if ((o1.getTopic14() - o2.getTopic14()) >= 0)
						return -1;
					else
						return 1;
				case 15:
					if ((o1.getTopic15() - o2.getTopic15()) >= 0)
						return -1;
					else
						return 1;
				case 16:
					if ((o1.getTopic16() - o2.getTopic16()) >= 0)
						return -1;
					else
						return 1;
				case 17:
					if ((o1.getTopic17() - o2.getTopic17()) >= 0)
						return -1;
					else
						return 1;
				case 18:
					if ((o1.getTopic18() - o2.getTopic18()) >= 0)
						return -1;
					else
						return 1;
				case 19:
					if ((o1.getTopic19() - o2.getTopic19()) >= 0)
						return -1;
					else
						return 1;
				case 20:
					if ((o1.getTopic20() - o2.getTopic20()) >= 0)
						return -1;
					else
						return 1;
				default:
					return 0;
				}
			}
		});

		List<TermScore> topWords = new ArrayList<>();
		for (int i = 0; i < NO_OF_TOP_WORDS; i++){
			switch(topic){
			case 1:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic1().toString()));
				break;
			case 2:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic2().toString()));
				break;
			case 3:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic3().toString()));
				break;
			case 4:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic4().toString()));
				break;
			case 5:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic5().toString()));
				break;
			case 6:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic6().toString()));
				break;
			case 7:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic7().toString()));
				break;
			case 8:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic8().toString()));
				break;
			case 9:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic9().toString()));
				break;
			case 10:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic10().toString()));
				break;
			case 11:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic11().toString()));
				break;
			case 12:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic12().toString()));
				break;
			case 13:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic13().toString()));
				break;
			case 14:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic14().toString()));
				break;
			case 15:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic15().toString()));
				break;
			case 16:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic16().toString()));
				break;
			case 17:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic17().toString()));
				break;
			case 18:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic18().toString()));
				break;
			case 19:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic19().toString()));
				break;
			case 20:
				topWords.add(new TermScore(termData.get(i).getTermId().toString(), termData.get(i).getTopic20().toString()));
				break;
			}
		}
			

		topTermsForTopic.put(topic, topWords);

	}

	private static void parseModelFile() {
		BufferedReader reader = null;
		String line = "";
		int lineCount = 0;
		try {
			reader = new BufferedReader(new FileReader(MODEL_FILE));
			while ((line = reader.readLine()) != null) {
				lineCount++;
				if (line.trim().length() == 0)
					continue;

				if (lineCount > idTermMap.size()) {
					reader.close();
					return;
				}

				if (lineCount > 11) {
					String[] tokens = line.trim().split(" ");
					Integer termId = Integer.parseInt(tokens[0]);
					Double topic1 = Double.parseDouble(tokens[1]);
					Double topic2 = Double.parseDouble(tokens[2]);
					Double topic3 = Double.parseDouble(tokens[3]);
					Double topic4 = Double.parseDouble(tokens[4]);
					Double topic5 = Double.parseDouble(tokens[5]);
					Double topic6 = Double.parseDouble(tokens[6]);
					Double topic7 = Double.parseDouble(tokens[7]);
					Double topic8 = Double.parseDouble(tokens[8]);
					Double topic9 = Double.parseDouble(tokens[9]);
					Double topic10 = Double.parseDouble(tokens[10]);
					Double topic11 = Double.parseDouble(tokens[11]);
					Double topic12 = Double.parseDouble(tokens[12]);
					Double topic13 = Double.parseDouble(tokens[13]);
					Double topic14 = Double.parseDouble(tokens[14]);
					Double topic15 = Double.parseDouble(tokens[15]);
					Double topic16 = Double.parseDouble(tokens[16]);
					Double topic17 = Double.parseDouble(tokens[17]);
					Double topic18 = Double.parseDouble(tokens[18]);
					Double topic19 = Double.parseDouble(tokens[19]);
					Double topic20 = Double.parseDouble(tokens[20]);
					
					TopicTerm topicTermData = new TopicTerm(termId, topic1, topic2, topic3, topic4, topic5, topic6, 
							topic7, topic8, topic9, topic10, topic11, topic12, topic13,
							topic14, topic15, topic16, topic17, topic18, topic19, topic20);
					topicTermList.add(topicTermData);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void parseVocabFile() {

		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(VOCAB_FILE));
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() == 0)
					continue;

				String[] tokens = line.split("\t");
				Integer tId = Integer.parseInt(tokens[0]);
				idTermMap.put(tId, tokens[1]);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
