package assignment_6;

public class FeatureUnit {

	private int numOfFeatures=7;
	private Double[] features;
	
	public FeatureUnit() {
		super();
		this.features = new Double[this.numOfFeatures];
	}

	public FeatureUnit(Double[] features) {
		super();
		this.features = features;
	}

	public int getNumOfFeatures() {
		return numOfFeatures;
	}

	public void setNumOfFeatures(int numOfFeatures) {
		this.numOfFeatures = numOfFeatures;
	}

	public Double[] getFeatures() {
		return features;
	}

	public void setFeatures(Double[] features) {
		this.features = features;
	}
	
}
