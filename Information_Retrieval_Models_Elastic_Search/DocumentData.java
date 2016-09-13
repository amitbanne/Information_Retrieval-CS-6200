package assignment_1;

public class DocumentData {

	private String docNo;
	private String term;
	private long termFreq;
	private long docFreq;
	
	public DocumentData(String docNo, String term, long termFreq, long docFreq) {
		super();
		this.docNo = docNo;
		this.term = term;
		this.termFreq = termFreq;
		this.docFreq = docFreq;
	}

	public String getDocNo() {
		return docNo;
	}

	public void setDocNo(String docNo) {
		this.docNo = docNo;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public long getTermFreq() {
		return termFreq;
	}

	public void setTermFreq(long termFreq) {
		this.termFreq = termFreq;
	}

	public long getDocFreq() {
		return docFreq;
	}

	public void setDocFreq(long docFreq) {
		this.docFreq = docFreq;
	}
	
	
	
	
}
