package uk.org.tombolo.importer;

import uk.org.tombolo.core.*;

import java.util.List;
import java.util.Properties;

public interface Importer {

	public Provider getProvider();

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
	 * Function that takes in a buffer of subjects and saves it to the database and clears the buffer.
	 *
	 * @param subjects
	 */
	void saveAndClearSubjectBuffer(List<Subject> subjects);

	/**
	 * Function that takes in a buffer of timed values and saves it to the database.
	 * It logs information about the values saved during that call
	 * and the total number of values saved in the current session.
	 * At the end of the call it clears the buffer.
	 *
	 * @param timedValueBuffer is the buffer of timed values to save
	 */
	public void saveAndClearTimedValueBuffer(List<TimedValue> timedValueBuffer);

	/**
	 * Function that takes in a buffer of fixed values and saves it to the database and clears the buffer.
	 *
	 * @param fixedValues
	 */
	public void saveAndClearFixedValueBuffer(List<FixedValue> fixedValues);

	public void setDownloadUtils(DownloadUtils downloadUtils);

	public void configure(Properties properties) throws ConfigurationException;
	public void verifyConfiguration() throws ConfigurationException;
	public Properties getConfiguration();

	int getSubjectCount();
	int getFixedValueCount();
	int getTimedValueCount();
}
