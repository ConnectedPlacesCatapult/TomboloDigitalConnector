package uk.org.tombolo.execution.spec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GeographySpecification {

	public static class GeographyMatcher {
		public final String attribute;
		public final String pattern;

		public GeographyMatcher(String attribute, String pattern) {
			this.attribute = attribute;
			this.pattern = pattern;
		}
	}

	List<GeographyMatcher> matchers;
	String geographyType;
	
	Map<String,String> attributes = new TreeMap<String,String>();
	
	public GeographySpecification(List<GeographyMatcher> matchers, String geographyType){
		this.matchers = matchers;
		this.geographyType = geographyType;
	}

	public List<GeographyMatcher> getMatchers() {
		if (null == this.matchers)
			return new ArrayList<>();
		return this.matchers;
	}

	public String getGeographyType() {
		return geographyType;
	}
	
	public Map<String,String> getAttributes(){
		return attributes;
	}
	
}
