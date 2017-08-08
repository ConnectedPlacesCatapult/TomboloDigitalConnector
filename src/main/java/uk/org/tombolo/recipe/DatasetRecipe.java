package uk.org.tombolo.recipe;

import java.util.List;

public class DatasetRecipe {

	List<SubjectRecipe> subjects;
	
	List<DatasourceRecipe> datasources;

	List<FieldRecipe> fields;
	
	public List<SubjectRecipe> getSubjects() {
		return subjects;
	}
	
	public void setSubjects(List<SubjectRecipe> subjects) {
		this.subjects = subjects;
	}

	public List<DatasourceRecipe> getDatasources() {
		return datasources;
	}

	public void setDatasources(List<DatasourceRecipe> datasources) {
		this.datasources = datasources;
	}

	public List<FieldRecipe> getFields() { return fields; }

	public void setFields(List<FieldRecipe> fields) {
		this.fields = fields;
	}
}
