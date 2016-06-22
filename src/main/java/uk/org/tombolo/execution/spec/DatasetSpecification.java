package uk.org.tombolo.execution.spec;

import java.util.List;

public class DatasetSpecification {

	List<SubjectSpecification> subjectSpecification;
	
	List<DatasourceSpecification> datasourceSpecification;

	List<FieldSpecification> fieldSpecification;
	
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

	public List<FieldSpecification> getFieldSpecification() { return fieldSpecification; }

	public void setFieldSpecification(List<FieldSpecification> fieldSpecification) {
		this.fieldSpecification = fieldSpecification;
	}
}
