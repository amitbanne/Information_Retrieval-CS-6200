package assignment_3;

public class CatalogEntry {

	private String url;
	private String fileName;
	private long startOffSet;
	private long endOffSet;
	
	
	public CatalogEntry(String url, String fileName, long startOffSet,
			long endOffSet) {
		super();
		this.url = url;
		this.fileName = fileName;
		this.startOffSet = startOffSet;
		this.endOffSet = endOffSet;
	}
	
	
	public CatalogEntry(String url, long startOffSet, long endOffSet) {
		super();
		this.url = url;
		this.startOffSet = startOffSet;
		this.endOffSet = endOffSet;
	}




	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getStartOffSet() {
		return startOffSet;
	}
	public void setStartOffSet(long startOffSet) {
		this.startOffSet = startOffSet;
	}
	public long getEndOffSet() {
		return endOffSet;
	}
	public void setEndOffSet(long endOffSet) {
		this.endOffSet = endOffSet;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}
