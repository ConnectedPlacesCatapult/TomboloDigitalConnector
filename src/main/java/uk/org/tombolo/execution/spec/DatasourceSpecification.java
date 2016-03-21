package uk.org.tombolo.execution.spec;

public class DatasourceSpecification {

	String importerClass;
	String datasourceId;
	
	public DatasourceSpecification(String importerClass, String datasourceId){
		this.importerClass = importerClass;
		this.datasourceId = datasourceId;
	}

	public String getImporterClass() {
		return importerClass;
	}

	public String getDatasourceId() {
		return datasourceId;
	}

}
