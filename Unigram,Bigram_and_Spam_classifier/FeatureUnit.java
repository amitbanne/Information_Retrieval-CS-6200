package assignment_7;

import java.util.List;

public class FeatureUnit {

	private String email;
	private List<TermUnit> termFeatures;
	private Integer lable;
	
	public FeatureUnit(String email, List<TermUnit> termFeatures, Integer lable) {
		super();
		this.email = email;
		this.termFeatures = termFeatures;
		this.lable = lable;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<TermUnit> getTermFeatures() {
		return termFeatures;
	}

	public void setTermFeatures(List<TermUnit> termFeatures) {
		this.termFeatures = termFeatures;
	}

	public Integer getLable() {
		return lable;
	}

	public void setLable(Integer lable) {
		this.lable = lable;
	}
	
		
}
