package uk.org.tombolo.importer;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.DatabaseJournalEntry;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.*;

import java.io.IOException;
import java.util.*;

public abstract class AbstractImporter implements Importer {
	// Flushing threshold for TimedValue/FixedValue/Subject save buffers
	protected static final Integer BUFFER_THRESHOLD = 10000;

	protected List<String> datasourceLables = Collections.EMPTY_LIST;
	protected List<String> geographyLabels = Collections.EMPTY_LIST;
	protected List<String> temporalLabels = Collections.EMPTY_LIST;

	protected final static String DEFAULT_GEOGRAPHY = "uk";
	protected final static String DEFAULT_TEMPORAL = "latest";

	protected int subjectCount = 0;		// Count of subjects imported during the lifetime of this class instance
	protected int fixedValueCount = 0;	// Count of fixed values imported during the lifetime of this class instance
	protected int timedValueCount = 0;	// Count of timed values imported during the lifetime of this class instance

	private static final Logger log = LoggerFactory.getLogger(AbstractImporter.class);
	protected Properties properties = new Properties();
	protected DownloadUtils downloadUtils;

	public AbstractImporter() {
		geographyLabels = Arrays.asList(DEFAULT_GEOGRAPHY);
		temporalLabels = Arrays.asList(DEFAULT_TEMPORAL);
	}

	@Override
	public List<Datasource> getAllDatasources() throws Exception {
		List<Datasource> datasources = new ArrayList<>();
		for(String datasourceLabel : getDatasourceLabels()){
			datasources.add(getDatasource(datasourceLabel));
		}
		return datasources;
	}

	@Override
	public List<String> getDatasourceLabels() {
		return datasourceLables;
	}

	@Override
	public List<String> getGeographyLabels() {
		return geographyLabels;
	}

	@Override
	public List<String> getTemporalLabels() {
		return temporalLabels;
	}

	public void setDownloadUtils(DownloadUtils downloadUtils){
		this.downloadUtils = downloadUtils;
	}

	/**
	 * Syntactic sugar for global scope import
	 * @param datasourceLabel
	 * @throws Exception
	 */
	public void importDatasource(String datasourceLabel) throws Exception{
		importDatasource(datasourceLabel, null, null);
	}

	/**
	 * Loads the data-source identified by datasourceId into the underlying data store
	 *
	 * @param datasourceId
	 * @return the number of data values loaded
	 * @throws IOException
	 * @throws ParseException
	 */
	@Override
	public void importDatasource(String datasourceId, List<String> geographyScope, List<String> temporalScope) throws Exception {
		importDatasource(datasourceId, geographyScope, temporalScope, false);
	}
	
	/**
	 * Loads the data-source identified by datasourceId into the underlying data store 
	 * 
	 * @param datasourceId
	 * @param force forces the importer to run even if it has already run
	 * @return the number of data values loaded
	 * @throws IOException
	 * @throws ParseException 
	 */
	@Override
	public void importDatasource(String datasourceId, List<String> geographyScope, List<String> temporalScope, Boolean force) throws Exception {
		if (!force && DatabaseJournal.journalHasEntry(getJournalEntryForDatasourceId(datasourceId))) {
			log.info("Skipped importing {}:{} as this import has been completed previously",
					this.getClass().getCanonicalName(), datasourceId);
		} else {
			log.info("Importing {}:{}",
					this.getClass().getCanonicalName(), datasourceId);
			// Get the details for the data source
			Datasource datasource = getDatasource(datasourceId);
			saveDatasourceMetadata(datasource);
			importDatasource(datasource, geographyScope, temporalScope);
			DatabaseJournal.addJournalEntry(getJournalEntryForDatasourceId(datasourceId));
			log.info("Imported {} subjects, {} fixed values and {} timedValues",
					subjectCount, fixedValueCount, timedValueCount);
		}
	}

	private DatabaseJournalEntry getJournalEntryForDatasourceId(String datasourceId) {
		return new DatabaseJournalEntry(getClass().getCanonicalName(), datasourceId);
	};

	/**
	 * The import function to be implemented in all none abstract sub-classes.
	 *
	 * @param datasource
	 * @param geographyScope
	 * @param temporalScope
	 * @throws Exception
	 */
	protected abstract void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception;

	/**
	 * Loads the given properties resource into the main properties object
	 *
     */
	@Override
	public void configure(Properties properties) throws ConfigurationException {
		this.properties.putAll(properties);
		verifyConfiguration();
	}

	@Override
	public void verifyConfiguration() throws ConfigurationException {
		// Do nothing by default
		// Importers that need configuration will override this
	}

	@Override
	public Properties getConfiguration(){
		return properties;
	}

	protected static void saveDatasourceMetadata(Datasource datasource){
		// Save SubjectType
		SubjectTypeUtils.save(datasource.getSubjectTypes());

		// Save provider
		ProviderUtils.save(datasource.getProvider());

		// Save attributes
		AttributeUtils.save(datasource.getTimedValueAttributes());
		AttributeUtils.save(datasource.getFixedValueAttributes());
	}

	/**
	 * Method for turning an enumeration of datasource identifiers into a List of Datasources.
	 *
	 * @param enumeration The enumeration
	 * @param <T> The type of the enumeration
	 * @return a List of datasources corresponding to the ids in the enumeration
	 * @throws Exception
	 */
	protected <T extends Enum<T>> List<Datasource> datasourcesFromEnumeration(Class<T> enumeration) throws Exception{
		List<Datasource> datasources = new ArrayList<>();
		for(T datasourceId : (enumeration.getEnumConstants())){
			datasources.add(getDatasource(datasourceId.name()));
		}
		return datasources;
	}

	protected <T extends Enum<T>> List<String> stringsFromEnumeration(Class<T> enumeration) {
		List<String> strings = new ArrayList<>();
		for(T item : (enumeration.getEnumConstants())){
			strings.add(item.name());
		}
		return strings;
	}

	public void saveBuffer(List<TimedValue> timedValueBuffer, int valueCount){
		log.info("Preparing to write a batch of {} values ...", timedValueBuffer.size());
		TimedValueUtils.save(timedValueBuffer);
		timedValueBuffer.clear();
		log.info("Total values written: {}", valueCount);
	}

	public int getSubjectCount() {
		return subjectCount;
	}

	public int getFixedValueCount() {
		return fixedValueCount;
	}

	public int getTimedValueCount() {
		return timedValueCount;
	}
}
