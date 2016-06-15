package uk.org.tombolo.execution.spec;

public class DataExportSpecification {

	DatasetSpecification datasetSpecification;
	String exporterClass;

	public DatasetSpecification getDatasetSpecification() {
		return datasetSpecification;
	}

	public void setDatasetSpecification(DatasetSpecification datasetSpecification) {
		this.datasetSpecification = datasetSpecification;
	}

	public String getExporterClass() {
		return exporterClass;
	}

	public void setExporterClass(String exporterClass) {
		this.exporterClass = exporterClass;
	}
}
