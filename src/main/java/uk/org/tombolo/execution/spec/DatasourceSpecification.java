package uk.org.tombolo.execution.spec;

import java.util.List;

public class DatasourceSpecification {

	private String importerClass;
	private String datasourceId;
	private List<String> geographyScope;
	private List<String> temporalScope;
	private String datasourceLocation;
	private String configFile = "";
	
	public DatasourceSpecification(String importerClass, String datasourceId, List<String> geographyScope, List<String> temporalScope, String datasourceLocation){
		this.importerClass = importerClass;
		this.datasourceId = datasourceId;
		this.geographyScope = geographyScope;
		this.temporalScope = temporalScope;
		this.datasourceLocation = datasourceLocation;
	}

	public DatasourceSpecification addConfigFile(String configFile) {
		this.configFile = configFile;

		return this;
	}

	public String getImporterClass() {
		return importerClass;
	}

	public String getDatasourceId() {
		return datasourceId;
	}

	public List<String> getGeographyScope() {
		return geographyScope;
	}

	public List<String> getTemporalScope() {
		return temporalScope;
	}

	public String getDatasourceLocation() { return datasourceLocation; }

	public String getConfigFile() { return configFile; }
}
