package assignment_2;

import java.util.Set;

public class DocIndex{

	private String docNo;
	private long termFreq;
	private Set<Long> positions;
	
	public DocIndex(String docNo, Set<Long> positions) {
		super();
		this.docNo = docNo;
		this.positions = positions;
		this.termFreq = this.positions.size();
	}

	public String getDocNo() {
		return docNo;
	}

	public void setDocNo(String docNo) {
		this.docNo = docNo;
	}

	public long getTermFreq() {
		return termFreq;
	}

	public void setTermFreq(long termFreq) {
		this.termFreq = termFreq;
	}

	public Set<Long> getPositions() {
		return positions;
	}

	public void setPositions(Set<Long> positions) {
		this.positions = positions;
	}

	@Override
	public String toString() {
		return "DocIndex [docNo=" + docNo + ", termFreq=" + termFreq + ", positions=" + positions + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((docNo == null) ? 0 : docNo.hashCode());
		result = prime * result + ((positions == null) ? 0 : positions.hashCode());
		result = prime * result + (int) (termFreq ^ (termFreq >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DocIndex other = (DocIndex) obj;
		if (docNo == null) {
			if (other.docNo != null)
				return false;
		} else if (!docNo.equals(other.docNo))
			return false;
		if (positions == null) {
			if (other.positions != null)
				return false;
		} else if (!positions.equals(other.positions))
			return false;
		if (termFreq != other.termFreq)
			return false;
		return true;
	}
	
	
	
	
	
}
