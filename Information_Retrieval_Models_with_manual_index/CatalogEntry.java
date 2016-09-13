package assignment_2;

public class CatalogEntry {

	private String term;
	private long startIndex;
	private long endIndex;
	
	public CatalogEntry(String term, long startIndex, long endIndex) {
		super();
		this.term = term;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public long getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(long startIndex) {
		this.startIndex = startIndex;
	}

	public long getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(long endIndex) {
		this.endIndex = endIndex;
	}
	
	
	

	
}
