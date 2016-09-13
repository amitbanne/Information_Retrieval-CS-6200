package assignment_8;

public class TopicEntry {

	private Integer term;
	private Double score;
	public TopicEntry(Integer term, Double score) {
		super();
		this.term = term;
		this.score = score;
	}
	public Integer getTerm() {
		return term;
	}
	public void setTerm(Integer term) {
		this.term = term;
	}
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	
		
}
