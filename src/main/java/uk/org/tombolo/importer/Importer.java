package uk.org.tombolo.importer;

import java.util.List;

import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;

public interface Importer {

	public Provider getProvider();
	
	public List<Datasource> getAllDatasources() throws Exception;
	
	public Datasource getDatasource(String datasourceId) throws Exception;
	
	public int importDatasource(String datasourceId) throws Exception;
}
