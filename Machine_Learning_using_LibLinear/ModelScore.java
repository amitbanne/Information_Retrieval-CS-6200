package assignment_6;

public class ModelScore {

	private double okapiTF;
	private double tfIDF;
	private double bm25;
	private double laplace;
	private double jelinek;
	private double proximity;
	private Integer label;
	
	public ModelScore() {
		super();
	}

	public double getOkapiTF() {
		return okapiTF;
	}

	public void setOkapiTF(double okapiTF) {
		this.okapiTF = okapiTF;
	}

	public double getTfIDF() {
		return tfIDF;
	}

	public void setTfIDF(double tfIDF) {
		this.tfIDF = tfIDF;
	}

	public double getBm25() {
		return bm25;
	}

	public void setBm25(double bm25) {
		this.bm25 = bm25;
	}

	public double getLaplace() {
		return laplace;
	}

	public void setLaplace(double laplace) {
		this.laplace = laplace;
	}

	public double getJelinek() {
		return jelinek;
	}

	public void setJelinek(double jelinek) {
		this.jelinek = jelinek;
	}

	public double getProximity() {
		return proximity;
	}

	public void setProximity(double proximity) {
		this.proximity = proximity;
	}

	public Integer getLabel() {
		return label;
	}

	public void setLabel(Integer label) {
		this.label = label;
	}
	
	
	
}
