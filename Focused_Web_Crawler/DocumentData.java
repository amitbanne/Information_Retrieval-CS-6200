package assignment_3;

import java.util.Set;

public class DocumentData {

	private String url;
	private String canonicalizedURL;
	private String title;
	private String bodyText;
	private Set<String> outLinks;
	private int depth;

	public DocumentData(String url, String canonicalizedURL, String title, String bodyText, Set<String> outLinks,
			int depth) {
		super();
		this.url = url;
		this.canonicalizedURL = canonicalizedURL;
		this.title = title;
		this.bodyText = bodyText;
		this.outLinks = outLinks;
		this.depth = depth;
	}


	

	public int getDepth() {
		return depth;
	}




	public void setDepth(int depth) {
		this.depth = depth;
	}




	public Set<String> getOutLinks() {
		return outLinks;
	}



	public void setOutLinks(Set<String> outLinks) {
		this.outLinks = outLinks;
	}



	public String getCanonicalizedURL() {
		return canonicalizedURL;
	}

	public void setCanonicalizedURL(String canonicalizedURL) {
		this.canonicalizedURL = canonicalizedURL;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBodyText() {
		return bodyText;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}

}
