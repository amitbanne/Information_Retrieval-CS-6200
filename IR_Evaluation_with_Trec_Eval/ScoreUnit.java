package assignment_5;

import java.util.Arrays;

public class ScoreUnit {

	private int ranks = 9;
	private long queryId;
	private Double rPrecision;
	private Double avgPrecision;
	private Double[] precisionAtK = new Double[ranks];
	private Double[] recallAtK = new Double[ranks];
	private Double[] f1AtK = new Double[ranks];
	private Double[] nDCG = new Double[ranks];
	private Long retrievedDocs;
	private Long relevantDocs;
	private Long relevantDocsRetrieved;
	
	public ScoreUnit(long queryId, Double rPrecision, Double avgPrecision, Double[] precisionAtK, Double[] recallAtK,
			Double[] f1AtK, Double[] nDCG, Long retrievedDocs, Long relevantDocs, Long relevantDocsRetrieved) {
		super();
		this.queryId = queryId;
		this.rPrecision = rPrecision;
		this.avgPrecision = avgPrecision;
		this.precisionAtK = precisionAtK;
		this.recallAtK = recallAtK;
		this.f1AtK = f1AtK;
		this.nDCG = nDCG;
		this.retrievedDocs = retrievedDocs;
		this.relevantDocs = relevantDocs;
		this.relevantDocsRetrieved = relevantDocsRetrieved;
	}

	public int getRanks() {
		return ranks;
	}

	public void setRanks(int ranks) {
		this.ranks = ranks;
	}

	public long getQueryId() {
		return queryId;
	}

	public void setQueryId(long queryId) {
		this.queryId = queryId;
	}

	public Double getrPrecision() {
		return rPrecision;
	}

	public void setrPrecision(Double rPrecision) {
		this.rPrecision = rPrecision;
	}

	public Double getAvgPrecision() {
		return avgPrecision;
	}

	public void setAvgPrecision(Double avgPrecision) {
		this.avgPrecision = avgPrecision;
	}

	public Double[] getPrecisionAtK() {
		return precisionAtK;
	}

	public void setPrecisionAtK(Double[] precisionAtK) {
		this.precisionAtK = precisionAtK;
	}

	public Double[] getRecallAtK() {
		return recallAtK;
	}

	public void setRecallAtK(Double[] recallAtK) {
		this.recallAtK = recallAtK;
	}

	public Double[] getF1AtK() {
		return f1AtK;
	}

	public void setF1AtK(Double[] f1AtK) {
		this.f1AtK = f1AtK;
	}

	public Double[] getnDCG() {
		return nDCG;
	}

	public void setnDCG(Double[] nDCG) {
		this.nDCG = nDCG;
	}

	public Long getRetrievedDocs() {
		return retrievedDocs;
	}

	public void setRetrievedDocs(Long retrievedDocs) {
		this.retrievedDocs = retrievedDocs;
	}

	public Long getRelevantDocs() {
		return relevantDocs;
	}

	public void setRelevantDocs(Long relevantDocs) {
		this.relevantDocs = relevantDocs;
	}

	public Long getRelevantDocsRetrieved() {
		return relevantDocsRetrieved;
	}

	public void setRelevantDocsRetrieved(Long relevantDocsRetrieved) {
		this.relevantDocsRetrieved = relevantDocsRetrieved;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((avgPrecision == null) ? 0 : avgPrecision.hashCode());
		result = prime * result + Arrays.hashCode(f1AtK);
		result = prime * result + Arrays.hashCode(nDCG);
		result = prime * result + Arrays.hashCode(precisionAtK);
		result = prime * result + (int) (queryId ^ (queryId >>> 32));
		result = prime * result + ((rPrecision == null) ? 0 : rPrecision.hashCode());
		result = prime * result + ranks;
		result = prime * result + Arrays.hashCode(recallAtK);
		result = prime * result + ((relevantDocs == null) ? 0 : relevantDocs.hashCode());
		result = prime * result + ((relevantDocsRetrieved == null) ? 0 : relevantDocsRetrieved.hashCode());
		result = prime * result + ((retrievedDocs == null) ? 0 : retrievedDocs.hashCode());
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
		ScoreUnit other = (ScoreUnit) obj;
		if (avgPrecision == null) {
			if (other.avgPrecision != null)
				return false;
		} else if (!avgPrecision.equals(other.avgPrecision))
			return false;
		if (!Arrays.equals(f1AtK, other.f1AtK))
			return false;
		if (!Arrays.equals(nDCG, other.nDCG))
			return false;
		if (!Arrays.equals(precisionAtK, other.precisionAtK))
			return false;
		if (queryId != other.queryId)
			return false;
		if (rPrecision == null) {
			if (other.rPrecision != null)
				return false;
		} else if (!rPrecision.equals(other.rPrecision))
			return false;
		if (ranks != other.ranks)
			return false;
		if (!Arrays.equals(recallAtK, other.recallAtK))
			return false;
		if (relevantDocs == null) {
			if (other.relevantDocs != null)
				return false;
		} else if (!relevantDocs.equals(other.relevantDocs))
			return false;
		if (relevantDocsRetrieved == null) {
			if (other.relevantDocsRetrieved != null)
				return false;
		} else if (!relevantDocsRetrieved.equals(other.relevantDocsRetrieved))
			return false;
		if (retrievedDocs == null) {
			if (other.retrievedDocs != null)
				return false;
		} else if (!retrievedDocs.equals(other.retrievedDocs))
			return false;
		return true;
	}
	
	
}
