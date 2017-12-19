package uk.org.tombolo.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.*;
import uk.org.tombolo.importer.utils.JournalEntryUtils;
import uk.org.tombolo.recipe.SubjectRecipe;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public abstract class AbstractImporter implements Importer {
	// Flushing threshold for TimedValue/FixedValue/Subject save buffers
	private static final Integer BUFFER_THRESHOLD = 10000;

	protected List<String> datasourceIds;
	protected List<String> geographyLabels;
	protected List<String> temporalLabels;
	protected Config config;
	protected List<SubjectRecipe> subjectRecipes;
	private String suffix;
	private URL dataURL;

	protected final static String DEFAULT_GEOGRAPHY = "all";
	protected final static String DEFAULT_TEMPORAL = "all";

	private int subjectCount = 0;		// Count of subjects imported during the lifetime of this class instance
	private int fixedValueCount = 0;	// Count of fixed values imported during the lifetime of this class instance
	private int timedValueCount = 0;	// Count of timed values imported during the lifetime of this class instance

	private static final Logger log = LoggerFactory.getLogger(AbstractImporter.class);
	protected Properties properties = new Properties();
	protected DownloadUtils downloadUtils;

	public AbstractImporter(Config config) {
		datasourceIds = Collections.emptyList();
		geographyLabels = Collections.singletonList(DEFAULT_GEOGRAPHY);
		temporalLabels = Collections.singletonList(DEFAULT_TEMPORAL);
		this.config = config;
	}

	@Override
	public List<String> getDatasourceIds() {
		return datasourceIds;
	}

	@Override
	public boolean datasourceExists(String datasourceId) {
		return getDatasourceIds().contains(datasourceId);
	}

	@Override
	public List<String> getGeographyLabels() {
		return geographyLabels;
	}

	@Override
	public List<String> getTemporalLabels() {
		return temporalLabels;
	}

	protected void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	protected void setDataURL(URL dataURL) {
		this.dataURL = dataURL;
	}

	protected String getSuffix() {
		return suffix;
	}

	protected URL getDataURL() {
		return dataURL;
	}

	public void setDownloadUtils(DownloadUtils downloadUtils){
		this.downloadUtils = downloadUtils;
	}

	public void setConfig(Config config) { this.config = config; }

	/**
	 * Loads the data-source identified by datasourceId into the underlying data store
	 *
	 * @param datasourceId
	 * @param geographyScope
	 * @param temporalScope
	 * @throws Exception
	 */
	@Override
	public void importDatasource(@Nonnull String datasourceId, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
		importDatasource(datasourceId, geographyScope, temporalScope, datasourceLocation, Collections.emptyList(), false);
	}

	/**
	 * Loads the data-source identified by datasourceId into the underlying data store 
	 * 
	 * @param datasourceId
	 * @param geographyScope
	 * @param temporalScope
	 * @param force forces the importer to run even if it has already run
	 * @throws Exception
	 */
	@Override
	public void importDatasource(@Nonnull String datasourceId, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation, @Nonnull List<SubjectRecipe> subjectRecipes, Boolean force) throws Exception {
		if (!datasourceExists(datasourceId))
			throw new ConfigurationException("Unknown DatasourceId:" + datasourceId);

		if (!force && DatabaseJournal.journalHasEntry(JournalEntryUtils.getJournalEntryForDatasourceId(
						getClass().getCanonicalName(), datasourceId, geographyScope, temporalScope, datasourceLocation))) {
			log.info("Skipped importing {}:{} as this import has been completed previously",
					this.getClass().getCanonicalName(), datasourceId);
		} else {
			log.info("Importing {}:{}",
					this.getClass().getCanonicalName(), datasourceId);
			// Setting Subject Recipe object
			this.subjectRecipes = subjectRecipes;
			// Get the details for the data source
			Datasource datasource = getDatasource(datasourceId);
			saveDatasourceMetadata(datasource);
			importDatasource(datasource, geographyScope, temporalScope, datasourceLocation);
			DatabaseJournal.addJournalEntry(JournalEntryUtils.getJournalEntryForDatasourceId(
					getClass().getCanonicalName(), datasourceId, geographyScope, temporalScope, datasourceLocation));
			log.info("Imported {} subjects, {} fixed values and {} timedValues",
					subjectCount, fixedValueCount, timedValueCount);
		}
	}

	@Override
	public List<SubjectType> getSubjectTypes(String datasourceId) {
		return Collections.emptyList();
	}

	@Override
	public List<Attribute> getTimedValueAttributes(String datasourceId) throws Exception {
		return Collections.emptyList();
	}

	@Override
	public List<Attribute> getFixedValueAttributes(String datasourceId) throws Exception {
		return Collections.emptyList();
	}

	@Override
	public Datasource getDatasource(String datasourceId) throws Exception {
		Datasource datasource = new Datasource(getDatasourceSpec(datasourceId));
		datasource.addSubjectTypes(getSubjectTypes(datasourceId));
		datasource.addFixedValueAttributes(getFixedValueAttributes(datasourceId));
		datasource.addTimedValueAttributes(getTimedValueAttributes(datasourceId));
		return datasource;
	}

	/**
	 * The import function to be implemented in all none abstract sub-classes.
	 *
	 * @param datasource
	 * @param geographyScope
	 * @param temporalScope
	 * @param datasourceLocation
	 * @throws Exception
	 */
	protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
		log.info("Local datasource is corrupted, deleting the file");
		downloadUtils.deleteLocalDatasource(getDataURL(), getProvider().getLabel(), getSuffix());
		importDatasource(datasource, geographyScope, temporalScope, datasourceLocation);
	}

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

	protected void saveDatasourceMetadata(Datasource datasource) throws Exception {
		// Save provider
		ProviderUtils.save(getProvider());

		// Save SubjectType
		SubjectTypeUtils.save(datasource.getSubjectTypes());

		// Save attributes
		AttributeUtils.save(datasource.getTimedValueAttributes());
		AttributeUtils.save(datasource.getFixedValueAttributes());
	}

	protected <T extends Enum<T>> List<String> stringsFromEnumeration(Class<T> enumeration) {
		List<String> strings = new ArrayList<>();
		for(T item : (enumeration.getEnumConstants())){
			strings.add(item.name());
		}
		return strings;
	}

	public void saveAndClearSubjectBuffer(List<Subject> subjectBuffer){
		log.info("Preparing to write a batch of {} subjects ... ", subjectBuffer.size());
		SubjectUtils.save(subjectBuffer);
		subjectCount += subjectBuffer.size();
		subjectBuffer.clear();
		log.info("Total subjects written: {}", subjectCount);
	}

	public void saveAndClearTimedValueBuffer(List<TimedValue> timedValueBuffer){
		log.info("Preparing to write a batch of {} timed values ...", timedValueBuffer.size());
		timedValueCount += timedValueBuffer.size();
		TimedValueUtils.save(timedValueBuffer);
		timedValueBuffer.clear();
		log.info("Total timed values written: {}", timedValueCount);
	}

	public void saveAndClearFixedValueBuffer(List<FixedValue> fixedValueBuffer){
		log.info("Preparing to write a batch of {} fixed values ...", fixedValueBuffer.size());
		fixedValueCount += fixedValueBuffer.size();
		FixedValueUtils.save(fixedValueBuffer);
		fixedValueBuffer.clear();
		log.info("Total fixed values written: {}", fixedValueCount);
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

	@Override
	public int getCombinedBufferSize() {
		return BUFFER_THRESHOLD;
	}

	@Override
	public int getSubjectBufferSize() {
		return BUFFER_THRESHOLD;
	}

	@Override
	public int getFixedValueBufferSize() {
		return BUFFER_THRESHOLD;
	}

	@Override
	public int getTimedValueBufferSize() {
		return BUFFER_THRESHOLD;
	}
}
