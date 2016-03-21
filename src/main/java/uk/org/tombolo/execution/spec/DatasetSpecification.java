package uk.org.tombolo.execution.spec;

import java.util.List;

public class DatasetSpecification {

	List<GeographySpecification> geographySpecification;
	
	List<AttributeSpecification> attributeSpecification;
	
	public List<GeographySpecification> getGeographySpecification() {
		return geographySpecification;
	}
	
	public void setGeographySpecification(List<GeographySpecification> geographySpecification) {
		this.geographySpecification = geographySpecification;
	}

	public List<AttributeSpecification> getAttributeSpecification() {
		return attributeSpecification;
	}

	public void setAttributeSpecification(List<AttributeSpecification> attributeSpecification) {
		this.attributeSpecification = attributeSpecification;
	}

}
