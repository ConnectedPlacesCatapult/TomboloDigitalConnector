package uk.org.tombolo.execution.spec;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class DatasetSpecification {

	List<SubjectSpecification> subjectSpecification;
	
	List<DatasourceSpecification> datasourceSpecification;
	
	List<AttributeSpecification> attributeSpecification;

	List<FieldSpecification> fieldSpecification;

	List<TransformSpecification> transformSpecification;
	
	public List<SubjectSpecification> getSubjectSpecification() {
		return subjectSpecification;
	}
	
	public void setSubjectSpecification(List<SubjectSpecification> subjectSpecification) {
		this.subjectSpecification = subjectSpecification;
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

	public List<FieldSpecification> getFieldSpecification() { return fieldSpecification; }

	public void setFieldSpecification(List<FieldSpecification> fieldSpecification) {
		this.fieldSpecification = fieldSpecification;
	}

	public static DatasetSpecification fromJsonFile(File jsonFile) throws JsonSyntaxException, JsonIOException, FileNotFoundException{
		Gson gson = new Gson();
		
		return gson.fromJson(new FileReader(jsonFile), DatasetSpecification.class);
	}
}
