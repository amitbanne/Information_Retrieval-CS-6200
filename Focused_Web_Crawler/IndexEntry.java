package assignment_3;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class IndexEntry {

	private String rawURL;
	private String title;
	private String text;
	private Set<String> outlinks;
	private Set<String> inlinks;
	private long depth;
	private Set<String> authors;
	private String htmlSource;
	private String httpHeaders;
	

	
	
	public IndexEntry(String rawURL, String title, String text, Set<String> outlinks, Set<String> inlinks, long depth) {
		super();
		this.rawURL = rawURL;
		this.title = title;
		this.text = text;
		this.outlinks = outlinks;
		this.inlinks = inlinks;
		this.depth = depth;
		authors =  new HashSet<>();
	}


	public IndexEntry(String rawURL, String title, String text, Set<String> outlinks, Set<String> inlinks, long depth,
			Set<String> authors, String htmlSource, String httpHeaders) {
		super();
		this.rawURL = rawURL;
		this.title = title;
		this.text = text;
		this.outlinks = outlinks;
		this.inlinks = inlinks;
		this.depth = depth;
		this.authors = authors;
		this.htmlSource = htmlSource;
		this.httpHeaders = httpHeaders;
	}


	public String getRawURL() {
		return rawURL;
	}


	public void setRawURL(String rawURL) {
		this.rawURL = rawURL;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}


	public Set<String> getOutlinks() {
		return outlinks;
	}


	public void setOutlinks(Set<String> outlinks) {
		if(this.outlinks==null)
			this.outlinks = new HashSet<>();
		
		this.outlinks = outlinks;
	}


	public Set<String> getInlinks() {
		return inlinks;
	}


	public void setInlinks(Set<String> inlinks) {
		if(this.inlinks==null)
			this.inlinks = new HashSet<>();
		
		this.inlinks = inlinks;
	}


	public long getDepth() {
		return depth;
	}


	public void setDepth(long depth) {
		this.depth = depth;
	}


	public Set<String> getAuthors() {
		return authors;
		
	}


	public void setAuthors(Set<String> authors) {
		if(this.authors==null)
			this.authors = new HashSet<>();
		
		this.authors = authors;
	}


	public String getHtmlSource() {
		return htmlSource;
	}


	public void setHtmlSource(String htmlSource) {
		this.htmlSource = htmlSource;
	}


	public String getHttpHeaders() {
		return httpHeaders;
	}


	public void setHttpHeaders(String httpHeaders) {
		this.httpHeaders = httpHeaders;
	}


	public void addAuthor(String author) {
		if(this.authors==null)
			this.authors = new HashSet<>();
		
		this.authors.add(author);
	}
		
}

