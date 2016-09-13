package assignment_7;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;

public class SPAM_INFO {

	private static Client client;
	private final static String INDEX_NAME = "spam_classifier";
	private final static String DOC_TYPE = "document";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		client = Util.transportClientBuilder();
	}
	
	
	
}
