package assignment_1;

public class DuplicateCheck {

	private String term, docNo;
	
	
	
	
	public DuplicateCheck(String term, String docNo) {
		super();
		this.term = term;
		this.docNo = docNo;
	}




	public String getTerm() {
		return term;
	}




	public void setTerm(String term) {
		this.term = term;
	}




	public String getDocNo() {
		return docNo;
	}




	public void setDocNo(String docNo) {
		this.docNo = docNo;
	}




	public DuplicateCheck() {
		// TODO Auto-generated constructor stub
	}



	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((docNo == null) ? 0 : docNo.hashCode());
		result = prime * result + ((term == null) ? 0 : term.hashCode());
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
		DuplicateCheck other = (DuplicateCheck) obj;
		if (docNo == null) {
			if (other.docNo != null)
				return false;
		} else if (!docNo.equals(other.docNo))
			return false;
		if (term == null) {
			if (other.term != null)
				return false;
		} else if (!term.equals(other.term))
			return false;
		return true;
	}




	@Override
	public String toString() {
		return "DuplicateCheck [term=" + term + ", docNo=" + docNo + "]";
	}

	
	
}
