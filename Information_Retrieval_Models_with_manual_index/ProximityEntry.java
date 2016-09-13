package assignment_2;

import java.util.List;
import java.util.Set;

public class ProximityEntry {

	private String query;
	private List<Long> positions;
	
	public ProximityEntry(String query, List<Long> positions) {
		super();
		this.query = query;
		this.positions = positions;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public List<Long> getPositions() {
		return positions;
	}

	public void setPositions(List<Long> positions) {
		this.positions = positions;
	}
	
	
}
