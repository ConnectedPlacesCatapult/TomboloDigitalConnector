package uk.org.tombolo.importer;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.DatabaseJournalEntry;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.DatabaseJournal;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public abstract class AbstractImporter implements Importer {
	private static final Logger log = LoggerFactory.getLogger(AbstractImporter.class);
	protected Properties properties = new Properties();
	protected DownloadUtils downloadUtils;

	public AbstractImporter() { }

	public void setDownloadUtils(DownloadUtils downloadUtils){
		this.downloadUtils = downloadUtils;
	}

	/**
	 * Loads the data-source identified by datasourceId into the underlying data store
	 *
	 * @param datasourceId
	 * @return the number of data values loaded
	 * @throws IOException
	 * @throws ParseException
	 */
	public Integer importDatasource(String datasourceId) throws Exception {
		return importDatasource(datasourceId, false);
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
	public Integer importDatasource(String datasourceId, Boolean force) throws Exception {
		if (!force && DatabaseJournal.journalHasEntry(getJournalEntryForDatasourceId(datasourceId))) {
			log.info("Skipped importing {}:{} as this import has been completed previously", this.getClass().getCanonicalName(), datasourceId);
			return null;
		} else {
			log.info("Importing {}:{}", this.getClass().getCanonicalName(), datasourceId);
			// Get the details for the data source
			Datasource datasource = getDatasource(datasourceId);
			Integer count = importDatasource(datasource);
			DatabaseJournal.addJournalEntry(getJournalEntryForDatasourceId(datasourceId));
			log.info("Imported {} values", count);
			return count;
		}
	}

	private DatabaseJournalEntry getJournalEntryForDatasourceId(String datasourceId) {
		return new DatabaseJournalEntry(getClass().getCanonicalName(), datasourceId);
	};

	protected abstract int importDatasource(Datasource datasource) throws Exception;

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

	protected static void saveProviderAndAttributes(Datasource datasource){
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

	public void saveBuffer(List<TimedValue> timedValueBuffer, int valueCount){
		log.info("Preparing to write a batch of {} values ...", timedValueBuffer.size());
		TimedValueUtils.save(timedValueBuffer);
		timedValueBuffer.clear();
		log.info("Total values written: {}", valueCount);
	}
}
