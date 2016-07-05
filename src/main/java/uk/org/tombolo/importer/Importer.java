package uk.org.tombolo.importer;

import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.util.List;
import java.util.Properties;

public interface Importer {

	public Provider getProvider();
	
	public List<Datasource> getAllDatasources() throws Exception;
	
	public Datasource getDatasource(String datasourceId) throws Exception;
	
	public int importDatasource(String datasourceId) throws Exception;
	public int importDatasource(Datasource datasource) throws Exception;

	public void setDownloadUtils(DownloadUtils downloadUtils);
	public void setTimedValueUtils(TimedValueUtils timedValueUtils);

	public void configure(Properties properties) throws ConfigurationException;
	public void verifyConfiguration() throws ConfigurationException;
	public Properties getConfiguration();
}
