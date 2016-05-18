package uk.org.tombolo.execution.spec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class DatasetSpecification {

	List<GeographySpecification> geographySpecification;
	
	List<DatasourceSpecification> datasourceSpecification;
	
	List<AttributeSpecification> attributeSpecification;

	List<TransformSpecification> transformSpecification;
	
	public List<GeographySpecification> getGeographySpecification() {
		return geographySpecification;
	}
	
	public void setGeographySpecification(List<GeographySpecification> geographySpecification) {
		this.geographySpecification = geographySpecification;
	}

	public List<DatasourceSpecification> getDatasourceSpecification() {
		return datasourceSpecification;
	}

	public void setDatasourceSpecification(List<DatasourceSpecification> datasourceSpecification) {
		this.datasourceSpecification = datasourceSpecification;
	}

	public List<AttributeSpecification> getAttributeSpecification() {
		return attributeSpecification;
	}

	public void setAttributeSpecification(List<AttributeSpecification> attributeSpecification) {
		this.attributeSpecification = attributeSpecification;
	}

	public List<TransformSpecification> getTransformSpecification() {
		return transformSpecification;
	}

	public void setTransformSpecification(List<TransformSpecification> transformSpecification) {
		this.transformSpecification = transformSpecification;
	}

	public static DatasetSpecification fromJsonFile(File jsonFile) throws JsonSyntaxException, JsonIOException, FileNotFoundException{
		Gson gson = new Gson();
		
		return gson.fromJson(new FileReader(jsonFile), DatasetSpecification.class);
	}
	
}
