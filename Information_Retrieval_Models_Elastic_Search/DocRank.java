package assignment_1;

import java.util.Comparator;

public class DocRank implements Comparable<DocRank>{

	private String queryNo, docNo;
	private double score;
	
	public DocRank(String queryNo, String docNo, double score) {
		super();
		this.queryNo = queryNo;
		this.docNo = docNo;
		this.score = score;
	}

	public String getQueryNo() {
		return queryNo;
	}

	public void setQueryNo(String queryNo) {
		this.queryNo = queryNo;
	}

	public String getDocNo() {
		return docNo;
	}

	public void setDocNo(String docNo) {
		this.docNo = docNo;
	}

	
	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public int compareTo(DocRank doc2) {
		if(this.getScore() >=  doc2.getScore())
			return -1;
		else 
			return 1;	
	}
	
	
}
