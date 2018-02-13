package uk.org.tombolo.recipe;

import java.util.List;

public class DatasourceRecipe {

	private String importerClass;
	private String datasourceId;
	private List<String> geographyScope;
	private List<String> temporalScope;
	private List<String> localData;
	private String configFile;
	
	public DatasourceRecipe(String importerClass, String datasourceId, List<String> geographyScope, List<String>
			temporalScope, List<String> localData, String configFile) {
		this.importerClass = importerClass;
		this.datasourceId = datasourceId;
		this.geographyScope = geographyScope;
		this.temporalScope = temporalScope;
		this.localData = localData;
		this.configFile = configFile;
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

	public List<String> getLocalData() { return localData; }

	public String getConfigFile() { return configFile; }
}
