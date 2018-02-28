package uk.org.tombolo.importer;

import uk.org.tombolo.core.*;
import uk.org.tombolo.recipe.SubjectRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Properties;

public interface Importer {

	Provider getProvider();

	/**
	 * Returns all the identifiers of datasources importable by this importer
	 *
	 * @return
	 * @throws Exception
	 */
	List<String> getDatasourceIds();

	/**
	 * Returns true if a datasource with said id exists
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
	List<String> getGeographyLabels();

	/**
	 * Returna all labels that can be used to restrict the temporal scope of the import.
	 * @return
	 */
	List<String> getTemporalLabels();

	Datasource getDatasource(String datasourceId) throws Exception;

	DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception;


	/**
	 * Function for importing a datasource for a given geography and temporal scope.
	 * If the scope values are null then we assume a default scope.
	 *
	 * @param datasourceId The identifier of the datasource to be imported.
	 * @param geographyScope A list of geography scopes to be imported.
	 * @param temporalScope A list of temporal scopes to be imported.
	 * @param datasourceLocation A list of file locations in case the data comes from a local source
	 * @throws Exception
	 */
	void importDatasource(@Nonnull  String datasourceId, @Nullable List<String> geographyScope, @Nullable List<String> temporalScope, @Nullable List<String> datasourceLocation) throws Exception;
	void importDatasource(@Nonnull String datasourceId, @Nullable List<String> geographyScope, @Nullable List<String> temporalScope, @Nullable List<String> datasourceLocation, Boolean force) throws Exception;

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
	void saveAndClearTimedValueBuffer(List<TimedValue> timedValueBuffer);

	/**
	 * Function that takes in a buffer of fixed values and saves it to the database and clears the buffer.
	 *
	 * @param fixedValues
	 */
	void saveAndClearFixedValueBuffer(List<FixedValue> fixedValues);

	void setDownloadUtils(DownloadUtils downloadUtils);

	/*
	 * Sets the subjects specified in the recipe to be used by the importer to make import decisions.
	 */
	void setSubjectRecipes(List<SubjectRecipe> subjectRecipes);

	void configure(Properties properties) throws ConfigurationException;
	void verifyConfiguration() throws ConfigurationException;
	Properties getConfiguration();

	int getSubjectCount();
	int getFixedValueCount();
	int getTimedValueCount();

	int getCombinedBufferSize();
	int getTimedValueBufferSize();
	int getFixedValueBufferSize();
	int getSubjectBufferSize();

	List<SubjectType> getSubjectTypes(String datasourceId);
	List<Attribute> getTimedValueAttributes(String datasourceId) throws Exception;
	List<Attribute> getFixedValueAttributes(String datasourceId) throws Exception;
}
