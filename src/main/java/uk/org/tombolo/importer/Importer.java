package uk.org.tombolo.importer;

import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.TimedValue;

import java.util.List;
import java.util.Properties;

public interface Importer {

	public Provider getProvider();
	
	public List<Datasource> getAllDatasources() throws Exception;
	
	public Datasource getDatasource(String datasourceId) throws Exception;
	
	public Integer importDatasource(String datasourceId) throws Exception;
	public Integer importDatasource(String datasourceId, Boolean force) throws Exception;

	/**
	 * Function that takes in a buffer of timed values and saves it to the database.
	 * It logs information about the values saved during that call
	 * and the total number of values saved in the current session.
	 *
	 * @param timedValueBuffer is the buffer of timed values to save
	 * @param valueCount is the total number values calcualted in the current session
	 */
	public void saveBuffer(List<TimedValue> timedValueBuffer, int valueCount);

	public void setDownloadUtils(DownloadUtils downloadUtils);

	public void configure(Properties properties) throws ConfigurationException;
	public void verifyConfiguration() throws ConfigurationException;
	public Properties getConfiguration();
}
