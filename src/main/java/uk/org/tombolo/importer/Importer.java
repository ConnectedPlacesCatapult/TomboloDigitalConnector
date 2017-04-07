package uk.org.tombolo.importer;

import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.TimedValue;

import java.util.List;
import java.util.Properties;

public interface Importer {

	public Provider getProvider();

	@Deprecated
	public List<Datasource> getAllDatasources() throws Exception;

	/**
	 * Returns all the identifiers of datasouces importable by this importer
	 *
	 * @return
	 * @throws Exception
	 */
	public List<String> getDatasourceIds();

	/**
	 * Returns true iff a datasource with said id exists
	 *
	 * @param datasourceId
	 * @return
	 */
	boolean datasourceExists(String datasourceId);

	/**
	 * Returns all labels that can be used to restrict the geographical scope of the import.
	 *
	 * @return
	 */
	public List<String> getGeographyLabels();

	/**
	 * Returna all labels that can be used to restrict the temporal scope of the import.
	 * @return
	 */
	public List<String> getTemporalLabels();

	public Datasource getDatasource(String datasourceId) throws Exception;

	/**
	 * Function for importing a datasource for a given geography and temporal scope.
	 * If the scope values are null then we assume a default scope.
	 *
	 * @param datasourceId The identifier of the datasource to be imported.
	 * @param geographyScope A list of geography scopes to be imported.
	 * @param temporalScope A list of temporal scopes to be imported.
	 * @throws Exception
	 */
	public void importDatasource(String datasourceId, List<String> geographyScope, List<String> temporalScope) throws Exception;
	public void importDatasource(String datasourceId, List<String> geographyScope, List<String> temporalScope, Boolean force) throws Exception;

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

	int getSubjectCount();
	int getFixedValueCount();
	int getTimedValueCount();
}
