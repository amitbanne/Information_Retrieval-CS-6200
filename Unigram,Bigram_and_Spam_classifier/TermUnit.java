package assignment_7;

public class TermUnit {

	private String term;
	private Integer tf;
	
	public TermUnit(String term, Integer tf) {
		super();
		this.term = term;
		this.tf = tf;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public Integer getTf() {
		return tf;
	}

	public void setTf(Integer tf) {
		this.tf = tf;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((term == null) ? 0 : term.hashCode());
		result = prime * result + ((tf == null) ? 0 : tf.hashCode());
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
		TermUnit other = (TermUnit) obj;
		if (term == null) {
			if (other.term != null)
				return false;
		} else if (!term.equals(other.term))
			return false;
		if (tf == null) {
			if (other.tf != null)
				return false;
		} else if (!tf.equals(other.tf))
			return false;
		return true;
	}
	
	
	
}

