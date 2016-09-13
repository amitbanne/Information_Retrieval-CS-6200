package assignment_2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class CatalogFileMerger {

	final static String directory = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 2/files/no_stem_stop/intermediate";
	final static String mergeddirectory = "C:/Users/NishantRatnakar/Desktop/Lecture Notes/IR/Amit/Assignment 2/files/no_stem_stop/merged";
	final static String catalogSubDirectory = "/catalog";
	final static String indexSubDirectory = "/index";
	static Map<String, CatalogEntry> catalogMap_1;
	static String catalogFile_1 = directory + "/NSS_CatalogFile_";
	static String indexFile_1 = directory + "/NSS_IndexFile_";

	static Map<String, CatalogEntry> catalogMap_2;
	static String catalogFile_2 = directory + "/NSS_CatalogFile_";
	static String indexFile_2 = directory + "/NSS_IndexFile_";

	static String mergedFile = mergeddirectory +indexSubDirectory+ "/NSS_IndexFile_";
	static Map<String, CatalogEntry> mergedCatalogMap = new LinkedHashMap<String, CatalogEntry>();;
	static String mergedCatalogFile = mergeddirectory +catalogSubDirectory+ "/NSS_CatalogFile_";

	static int indexFileCounter = 1;
	static int catalogFileCounter = 1;
	static int indexProcessedCounter = 1;
	static int catalogProcessedCounter = 1;
	
	static Map<String, String> mergedIndexMap = new LinkedHashMap<>();
	static int totalFileCount = 85;
	
	static Map<String, Long> docToIDMap = new LinkedHashMap<>();
	static Map<String, Long> termToIdMap = new LinkedHashMap<>();
	
	static Map<Long,String> idToDocMap = new LinkedHashMap<>();
	static Map<Long,String> idToTermMap = new LinkedHashMap<>();
	
	public static void main(String[] args) {

		mapTermIdsFromFile();
		mapDocumentIdsFromFile();
		
		mergeFiles();
		System.out.println("completed..");
	}
	
	
	private static void mapDocumentIdsFromFile() {
		
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader("./DocumentIDs.txt"));
			while ((line = reader.readLine()) != null) {
			
				Long docId = Long.parseLong(line.split("=")[0]);
				String docNo = line.split("=")[1];
				docToIDMap.put(docNo, docId);
				idToDocMap.put(docId, docNo);
				
				if(null==docNo)
					System.out.println(docNo+"="+docId);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private static void mapTermIdsFromFile() {

		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader("./TermIDs.txt"));
			while ((line = reader.readLine()) != null && line.contains("=")) {
				
				Long termId = Long.parseLong(line.split("=")[0]);
				String term = line.split("=")[1];
				termToIdMap.put(term, termId);
				idToTermMap.put(termId, term);
				if(null==term)
					System.out.println(term+"="+termId);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void mergeFiles(){
	
		boolean startOffsetAdjust = false;
		
		while(true){
			catalogFile_1 = directory + "/NSS_CatalogFile_";
			indexFile_1 = directory + "/NSS_IndexFile_";
			catalogFile_2 = directory + "/NSS_CatalogFile_";
			indexFile_2 = directory + "/NSS_IndexFile_";
			if(!startOffsetAdjust){
				indexFile_1 +=(indexProcessedCounter++)+".txt";
				indexFile_2 +=(indexProcessedCounter++)+".txt";
				
				catalogFile_1 += (catalogProcessedCounter++)+".txt";
				catalogFile_2 +=(catalogProcessedCounter++)+".txt";				
				startOffsetAdjust = true;
			}else{
				indexFile_1 =mergedFile;
				indexFile_2 +=(indexProcessedCounter++)+".txt";
				
				catalogFile_1 = mergedCatalogFile;
				catalogFile_2 +=(catalogProcessedCounter++)+".txt";				
			}
			
			mergeUtil(catalogFile_1,indexFile_1,catalogFile_2,indexFile_2);
			if(indexProcessedCounter>85)
				break;
		}
	}

	
	public static void mergeUtil(String catalog1, String index1, String catalog2, String index2){
		
		
		mergedFile = mergeddirectory +indexSubDirectory+ "/NSS_IndexFile_";
		mergedCatalogFile = mergeddirectory +catalogSubDirectory+ "/NSS_CatalogFile_";
		
		mergedCatalogMap = new LinkedHashMap<String, CatalogEntry>();
		catalogMap_1 = new LinkedHashMap<>();
		catalogMap_2 = new LinkedHashMap<>();
		catalogMap_1 = parseCatalogFile(catalog1);
		catalogMap_2 = parseCatalogFile(catalog2);

		mergedFile+=indexFileCounter+".txt";
		mergedCatalogFile+=catalogFileCounter+".txt";
		
		indexFileMerger(mergedFile);
		storeCatalog(mergedCatalogFile);
		
		indexFileCounter++;
		catalogFileCounter++;
	}
	
	public static Map<String, CatalogEntry> parseCatalogFile(String fileName) {
		// String fileName = "./CatalogFile.txt";
		Map<String, CatalogEntry> catalogMap = new LinkedHashMap<String, CatalogEntry>();
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader(fileName));
			while ((line = reader.readLine()) != null) {
				CatalogEntry c = parseFileEntry(line.trim());
				if (null != c)
					catalogMap.put(c.getTerm(), c);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return catalogMap;
	}

	private static CatalogEntry parseFileEntry(String catalog) {

		if (catalog.trim().contains("=")) {
			String[] termCatalogEntries = catalog.split("=");
			if (termCatalogEntries.length > 1) {
				String term = termCatalogEntries[0];
				long startIndex = Long.parseLong(termCatalogEntries[1]
						.split(",")[0]);
				long endIndex = Long
						.parseLong(termCatalogEntries[1].split(",")[1]);

				CatalogEntry c = new CatalogEntry(term, startIndex, endIndex);
				return c;

				// catalogMap.put(term, new CatalogEntry(startIndex, endIndex));

			}
		}
		System.out.println("Missing catalog: "+catalog);
		return null;
	}

	private static void indexFileMerger(String mergedFile) {
		long seekOffSet = 0;
		
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(mergedFile, "rw");

			Iterator<Entry<String, CatalogEntry>> itr = catalogMap_1.entrySet()
					.iterator();

			while (itr.hasNext()) {

				Entry<String, CatalogEntry> e = itr.next();
				String term = e.getKey();
				CatalogEntry catalogEntry_1 = e.getValue();
				CatalogEntry catalogEntry_2 = null;

				if (catalogMap_2.containsKey(term)) {
					catalogEntry_2 = catalogMap_2.get(term);
				}
				StringBuilder sb = new StringBuilder();
				List<DocIndex> termDocIndex = new ArrayList<DocIndex>();
				if (null == catalogEntry_2) {
					// term present in first catalog file only

					String fileEntry_1 = extractFromFile(indexFile_1,
							catalogEntry_1);
					if(fileEntry_1.trim().contains("=")){
						
						//mergedIndexMap.put(term, fileEntry_1);
						sb.append(fileEntry_1);
						sb.append(System.lineSeparator());
						long startIndex = seekOffSet;
						raf.seek(seekOffSet);
						raf.writeBytes(sb.toString());
						seekOffSet = raf.getFilePointer();
						long endIndex = seekOffSet;
						mergedCatalogMap.put(term, new CatalogEntry(term, startIndex,
								endIndex));
					}
				} else {
					// term present in both catalog files

					String fileEntry_1 = extractFromFile(indexFile_1,
							catalogEntry_1);
					String fileEntry_2 = extractFromFile(indexFile_2,
							catalogEntry_2);
					String mergedListAsString = "";
					List<DocIndex> invertedList_1 = stringToDocIndexList(fileEntry_1);
					List<DocIndex> invertedList_2 = stringToDocIndexList(fileEntry_2);

					if (null == invertedList_1 && null == invertedList_2)
						continue;
					else if (null == invertedList_1) {
						mergedListAsString = fileEntry_2.trim();
					} else if (null == invertedList_2) {
						mergedListAsString = fileEntry_1.trim();
					} else {
						termDocIndex = listMerger(invertedList_1,
								invertedList_2);
						
						Long termId = termToIdMap.get(term);
						
						//compressing termId
						String compressed="";
						if(termId % 100000 ==0){
							long x = termId/100000;
							compressed = x+"^"+5;
							//sb.append(compressed);
						}else if(termId % 10000 ==0){
							long x = termId/10000;
							compressed = x+"^"+4;
							//sb.append(compressed);
						}else if(termId % 1000 ==0){
							long x = termId/1000;
							compressed = x+"^"+3;
							//sb.append(compressed);
						}else
							compressed = termId.toString();;
						
						mergedListAsString = (compressed+"="+invertedListToString(termDocIndex)).trim();
					}
					
					if(mergedListAsString.trim().length()>=1){
						//mergedIndexMap.put(term, mergedListAsString);
						
						
						sb.append(mergedListAsString);
						sb.append(System.lineSeparator());
						long startIndex = seekOffSet;
						raf.seek(seekOffSet);
						raf.writeBytes(sb.toString());
						seekOffSet = raf.getFilePointer();
						long endIndex = seekOffSet;
						mergedCatalogMap.put(term, new CatalogEntry(term, startIndex,
								endIndex));
					}
					catalogMap_2.remove(term);
				}
			}

			// for terms present only in catalog2
			for (Entry<String, CatalogEntry> e : catalogMap_2.entrySet()) {
				StringBuilder sb = new StringBuilder();

				String term = e.getKey();
				CatalogEntry catalogEntry = e.getValue();
				String fileEntry = extractFromFile(indexFile_2, catalogEntry);
				sb.append(fileEntry.trim());
				sb.append(System.lineSeparator());
				
				if(sb.toString().trim().length()>=1){
					//mergedIndexMap.put(term, sb.toString());
					long startIndex = seekOffSet;
					raf.seek(seekOffSet);
					raf.writeBytes(sb.toString());
					seekOffSet = raf.getFilePointer();
					long endIndex = seekOffSet;
					mergedCatalogMap.put(term, new CatalogEntry(term, startIndex,
							endIndex));
				}
			}
			
			raf.close();
			//writeMergedIndexToFile(mergedFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private static void writeMergedIndexToFile(String mergedFile) throws IOException {

		long seekOffSet = 0;
			RandomAccessFile raf = new RandomAccessFile(mergedFile, "rw");
			for(Entry<String, String> e: mergedIndexMap.entrySet()){
				
				long startIndex = seekOffSet;
				raf.seek(seekOffSet);
				raf.writeBytes(e.getValue());
				seekOffSet = raf.getFilePointer();
				long endIndex = seekOffSet;
				mergedCatalogMap.put(e.getKey(), new CatalogEntry(e.getKey(), startIndex,
						endIndex));
			}
			raf.close();
		
	}


	private static String invertedListToString(List<DocIndex> termDocIndex) {

		String delimeter = "";
		StringBuilder sb = new StringBuilder();
		for (DocIndex dIndex : termDocIndex) {
			sb.append(delimeter);
			delimeter = ";";

			Long docId = docToIDMap.get(dIndex.getDocNo());
			
			//compressing docId
			String compressed="";
			if(docId % 10000 ==0){
				long x = docId/10000;
				compressed = x+"^"+4;
				sb.append(compressed);
			}else if(docId % 1000 ==0){
				long x = docId/1000;
				compressed = x+"^"+3;
				sb.append(compressed);
			}else
				sb.append(docId);

			//sb.append(docId);
			/*sb.append(":");
			sb.append(dIndex.getTermFreq());*/
			sb.append(":");
			StringBuilder posSb = new StringBuilder();
			String del = "";
			for(Long pos : dIndex.getPositions()){
				posSb.append(del);
				del = ",";
				posSb.append(pos);
			}
			sb.append(posSb);
		}
		return sb.toString();
	}

	private static List<DocIndex> listMerger(List<DocIndex> invertedList_1,
			List<DocIndex> invertedList_2) {

		List<DocIndex> mergedList = new ArrayList<DocIndex>();
		int length_1 = invertedList_1.size();
		int length_2 = invertedList_2.size();
		int i = 0, j = 0;
		// merge sort
		while (i < length_1 && j < length_2) {
			DocIndex d1 = invertedList_1.get(i);
			DocIndex d2 = invertedList_2.get(j);

			if (d1.getTermFreq() > d2.getTermFreq()) {
				mergedList.add(d1);
				i++;
			} else {
				mergedList.add(d2);
				j++;
			}

		}

		while (i < length_1) {
			DocIndex d1 = invertedList_1.get(i);
			mergedList.add(d1);
			i++;
		}

		while (j < length_2) {
			DocIndex d2 = invertedList_2.get(j);
			mergedList.add(d2);
			j++;
		}

		return mergedList;
	}

	private static List<DocIndex> stringToDocIndexList(String fileEntry) {
		List<DocIndex> invertedList = new ArrayList<DocIndex>();
		if (fileEntry == null || !fileEntry.contains(":")
				|| !fileEntry.contains("="))
			return null;
		
		String[] docIndexs = fileEntry.trim().split("=")[1].split(";");

		for (String d : docIndexs) {

			String[] parsedInfo = d.split(":");

			long docId = 0;
			String l = parsedInfo[0];
			if(l.contains("^")){
				String split[] = l.split("\\^");
				docId = (long) ((Long.parseLong(split[0]))*(Math.pow(10, Long.parseLong(split[1]))));
			}else{
				docId = Long.parseLong(parsedInfo[0]);
			}
			String docNo = idToDocMap.get(docId);
			String positionsUnparsed = parsedInfo[1];
			String[] positionTokens = positionsUnparsed.split(",");
			Set<Long> positions = new LinkedHashSet<>();
			for(String s: positionTokens){
				positions.add(Long.parseLong(s));
			}
			
			//String docNo =parsedInfo[0];
			
			//long termFreq = Long.parseLong(parsedInfo[1]);
			DocIndex docIdx = new DocIndex(docNo,positions);
			invertedList.add(docIdx);
		}
		return invertedList;
	}

	private static String extractFromFile(String indexFile,
			CatalogEntry catalogEntry) {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(indexFile, "rw");
			raf.seek(catalogEntry.getStartIndex());
			String s = raf.readLine();
			
			raf.close();
			return s;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static void storeCatalog(String catalogFileName) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, CatalogEntry> e : mergedCatalogMap.entrySet()) {

			String s = e.getKey()+"="+e.getValue().getStartIndex()+","+e.getValue().getEndIndex()+System.lineSeparator();
			if(s.trim().length()>=2)
				sb.append(s);
		}
		try {
			fileWriterUtil(catalogFileName,sb.toString());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public static void fileWriterUtil(String catalogFileName, String contentToFile) throws IOException {

		File file = new File(catalogFileName);
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(contentToFile);
		bw.close();
	}
}
