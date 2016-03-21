package uk.org.tombolo.execution.spec;

public class GeographySpecification {

	String labelPattern;
	String geographyType;
	
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
	
}
