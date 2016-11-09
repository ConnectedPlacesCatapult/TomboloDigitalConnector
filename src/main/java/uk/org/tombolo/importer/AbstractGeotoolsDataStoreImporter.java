package uk.org.tombolo.importer;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.utils.GeotoolsDataStoreUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractGeotoolsDataStoreImporter extends AbstractImporter implements GeotoolsDataStoreImporter {
    private static Logger log = LoggerFactory.getLogger(AbstractGeotoolsDataStoreImporter.class);

    private int fixedValueBufferSize = 10000;
    private int timedValueBufferSize = 10000;

    public int importDatasource(Datasource datasource) throws Exception {
        // Save provider and attributes
        saveProviderAndAttributes(datasource);

        DataStore dataStore = getDataStoreForDatasource(datasource);

        // Load attribute values
        loadSubjects(datasource, dataStore, getSubjectType());
        return loadValues(dataStore, datasource, getSubjectType());
    }

    public DataStore getDataStoreForDatasource(Datasource datasource) throws IOException {
        return DataStoreFinder.getDataStore(getParamsForDatasource(datasource));
    }

    public <T> T withFeatureReaderForDatasource(Datasource datasource, Function<FeatureReader<SimpleFeatureType, SimpleFeature>, T> fn) throws IOException {
        FeatureReader<SimpleFeatureType, SimpleFeature> featureReader = GeotoolsDataStoreUtils.getFeatureReader(getDataStoreForDatasource(datasource), getTableNameForDatasource(datasource));
        try {
            return fn.apply(featureReader);
        } finally {
            featureReader.close();
        }
    }

    protected abstract String getTableNameForDatasource(Datasource datasource);

    private int loadSubjects(Datasource datasource, DataStore dataStore, SubjectType subjectType) throws SQLException, IOException, FactoryException, TransformException {
        return withFeatureReaderForDatasource(datasource, featureReader -> {
            try {
                List<Subject> subjects = GeotoolsDataStoreUtils.convertFeaturesToSubjects(featureReader, subjectType, this);
                SubjectUtils.save(subjects);
                return subjects.size();
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        });
    }

    private int loadValues(DataStore dataStore, Datasource datasource, SubjectType subjectType) throws IOException {
        FeatureReader featureReader = GeotoolsDataStoreUtils.getFeatureReader(dataStore, getTableNameForDatasource(datasource));
        int fixedValueCounter = 0;
        List<FixedValue> fixedValueBuffer = new ArrayList<>();
        int timedValueCounter = 0;
        List<TimedValue> timedValueBuffer = new ArrayList<>();

        while (featureReader.hasNext()){
            SimpleFeature feature = (SimpleFeature) featureReader.next();
            Subject subject = SubjectUtils.getSubjectByLabel(getFeatureSubjectLabel(feature, subjectType));
            LocalDateTime modified = getTimestampForFeature(feature);

            for (Attribute attribute : datasource.getFixedValueAttributes()){
                if (feature.getAttribute(attribute.getLabel()) == null)
                    continue;
                String value = feature.getAttribute(attribute.getLabel()).toString();
                fixedValueBuffer.add(new FixedValue(subject, attribute, value));
                fixedValueCounter++;
                // Flushing  buffer if full
                if (fixedValueCounter % fixedValueBufferSize == 0)
                    saveFixedValues(fixedValueCounter, fixedValueBuffer);
            }

            for (Attribute attribute : datasource.getTimedValueAttributes()){
                if (feature.getAttribute(attribute.getLabel()) == null)
                    continue;
                Double value = Double.parseDouble(feature.getAttribute(attribute.getLabel()).toString());
                timedValueBuffer.add(new TimedValue(subject, attribute, modified, value));
                timedValueCounter++;
                // Flushing buffer if full
                if (timedValueCounter % timedValueBufferSize == 0)
                    saveTimedValues(timedValueCounter, timedValueBuffer);
            }
        }

        saveTimedValues(timedValueCounter, timedValueBuffer);
        featureReader.close();

        return timedValueCounter + fixedValueCounter;
    }

    protected abstract LocalDateTime getTimestampForFeature(SimpleFeature feature);

    @Override
    public String getFeatureSubjectLabel(SimpleFeature feature, SubjectType subjectType) {
        return feature.getName()+":"+feature.getID();
    }

    @Override
    public String getFeatureSubjectName(SimpleFeature feature, SubjectType subjectType) {
        return feature.getName()+":"+feature.getID();
    }

    private void saveTimedValues(int valueCounter, List<TimedValue> timedValueBuffer){
        log.info("Preparing to write a batch of {} timed values ...", timedValueBuffer.size());
        TimedValueUtils.save(timedValueBuffer);
        timedValueBuffer.clear();

        log.info("Total values written: {}", valueCounter);
    }

    private void saveFixedValues(int valueCounter, List<FixedValue> fixedValueBuffer){
        log.info("Preparing to write a batch of {} fixed values ...", fixedValueBuffer.size());
        FixedValueUtils.save(fixedValueBuffer);
        fixedValueBuffer.clear();

        log.info("Total values written: {}", valueCounter);
    }
}
