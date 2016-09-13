package assignment_3;

public class ExtraDataUnit {

	private String docNo;
	private String rawURL;
	private String httpHeaders;
	private String html_Source;
	
	public ExtraDataUnit(String docNo, String rawURL, String httpHeaders, String html_Source) {
		super();
		this.docNo = docNo;
		this.rawURL = rawURL;
		this.httpHeaders = httpHeaders;
		this.html_Source = html_Source;
	}

	public String getDocNo() {
		return docNo;
	}

	public void setDocNo(String docNo) {
		this.docNo = docNo;
	}

	public String getRawURL() {
		return rawURL;
	}

	public void setRawURL(String rawURL) {
		this.rawURL = rawURL;
	}

	public String getHttpHeaders() {
		return httpHeaders;
	}

	public void setHttpHeaders(String httpHeaders) {
		this.httpHeaders = httpHeaders;
	}

	public String getHtml_Source() {
		return html_Source;
	}

	public void setHtml_Source(String html_Source) {
		this.html_Source = html_Source;
	}
	
	
	
	
}
