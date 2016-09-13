package assignment_3;

public class URLData {

	private String url;
	private long timeOfEntry;
	private int relevantCount;
	private int depth;
	


	public URLData(String url, long timeOfEntry, int relevantCount, int depth) {
		super();
		this.url = url;
		this.timeOfEntry = timeOfEntry;
		this.relevantCount = relevantCount;
		this.depth = depth;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getTimeOfEntry() {
		return timeOfEntry;
	}

	public void setTimeOfEntry(long timeOfEntry) {
		this.timeOfEntry = timeOfEntry;
	}

	
	public int getRelevantCount() {
		return relevantCount;
	}

	public void setRelevantCount(int relevantCount) {
		this.relevantCount = relevantCount;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	
	

}
