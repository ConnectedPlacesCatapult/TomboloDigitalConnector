package uk.org.tombolo.execution.spec;

import java.util.Map;
import java.util.TreeMap;

public class GeographySpecification {

	String labelPattern;
	String geographyType;
	
	Map<String,String> attributes = new TreeMap<String,String>();
	
	public GeographySpecification(String labelPattern, String geographyType){
		this.labelPattern = labelPattern;
		this.geographyType = geographyType;
	}

	public String getLabelPattern() {
		return labelPattern;
	}

	public String getGeographyType() {
		return geographyType;
	}
	
	public Map<String,String> getAttributes(){
		return attributes;
	}
	
}
