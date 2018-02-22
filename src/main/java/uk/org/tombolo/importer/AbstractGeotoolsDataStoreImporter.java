package uk.org.tombolo.importer;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureReader;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.importer.utils.GeotoolsDataStoreUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * AbstractGeotoolsDataStoreImporter.java
 * This is an abstract importer that can be extended to import any sort of
 * Geotools DataStore. This includes GeoJSON and PostGIS now but you can add
 * any DataStore dependency you like and it should work seamlessly (but more
 * likely than not you might discover it needs tweaking).
 *
 * Your responsibility as an implementer is to implement all the abstract
 * methods, and then populate the Datasource objects returned by getDatasource
 * with the relevant Attributes. The methods on this class will do all of the
 * importing for you based on this information. This is possible because all
 * Geotools DataStores have roughly the same form and can be treated similarly.
 */
public abstract class AbstractGeotoolsDataStoreImporter extends AbstractImporter {
    private static Logger log = LoggerFactory.getLogger(AbstractGeotoolsDataStoreImporter.class);

    private List<TimedValue> timedValueBuffer = new ArrayList<>();
    private List<FixedValue> fixedValueBuffer = new ArrayList<>();

    private List<Subject> subjectBuffer = new ArrayList<>();

    /**
     * getParamsForDatasource
     * Returns a params object that will be passed to the Geotools DataStoreFinder.
     * These are often not well documented, but you should be able to scour some
     * examples from the docs for your given DataStore.
     * @param datasource The datasource being imported
     * @return A map of params for DataStoreFinder
     */
    protected abstract Map<String, Object> getParamsForDatasource(Datasource datasource);

    /**
     * applyFeatureAttributesToSubject
     * Passed a Subject and a Feature, builds out the attributes on the subject
     * based presumably on the attributes of the feature.
     * @param subject An empty subject
     * @param feature A geographic feature
     * @return The given subject
     */
    protected abstract Subject applyFeatureAttributesToSubject(Subject subject, SimpleFeature feature);

    /**
     * getSourceEncoding
     * Returns a string representing the coordinate system the source dataset
     * is in. This will be used to normalize it to a common coordinate system.
     * @return A string representing a coordinate system (e.g. "EPSG:27700")
     */
    protected abstract String getSourceEncoding();

    /**
     * getTimestampForFeature
     * Given a geographic feature, return the timestamp for use in its
     * TimedValues
     * @param feature A geographic feature
     * @return A timestamp for the feature's TimedValues
     */
    protected abstract LocalDateTime getTimestampForFeature(SimpleFeature feature) throws ParsingException;

    /**
     * getTypeNameForDatasource
     * In Geotools world each DataStore has a list of types, which are analogous
     * to tables in a database. This method returns the name of the type that
     * should be imported for a given datasource. More often than not there will
     * only be one type, and it might be the name of a table, or the name of
     * the file.
     * @param datasource The datasource being imported
     * @return The name of the type to import
     */
    protected abstract String getTypeNameForDatasource(Datasource datasource);

    /**
     * getAttributesForDatasource
     * Returns a list of attribute types for a datasource. If your DataStore was
     * a database, this would return the columns in the table to be imported.
     * You will probably use this when setting up the attributes on the Datasource.
     * Note that these are Geotools AttributeTypes and have nothing to do with
     * Tombolo's Attribute objects.
     * @param datasource The datasource being imported
     * @return A list of attributes for the datasource
     * @throws IOException
     */
    protected List<AttributeType> getAttributesForDatasource(Datasource datasource) throws IOException {
        DataStore dataStore = null;
        try {
            dataStore = getDataStoreForDatasource(datasource);
            SimpleFeatureType schema = dataStore.getSchema(getTypeNameForDatasource(datasource));
            return schema.getTypes();
        } finally {
            if (null != dataStore) dataStore.dispose();
        }
    }

    @Override
    final public void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        DataStore dataStore = getDataStoreForDatasource(datasource);
        FeatureReader<SimpleFeatureType, SimpleFeature> featureReader = GeotoolsDataStoreUtils.getFeatureReader(dataStore, getTypeNameForDatasource(datasource));

        // Load attribute values
        withSubjects(featureReader, dataStore, (feature, subject) -> {
            timedValueBuffer.addAll(buildTimedValuesFromFeature(datasource, feature, subject));
            fixedValueBuffer.addAll(buildFixedValuesFromFeature(datasource, feature, subject));
        });

        featureReader.close();
        dataStore.dispose();
    }

    private void withSubjects(FeatureReader<SimpleFeatureType, SimpleFeature> featureReader, DataStore dataStore, BiConsumer<SimpleFeature, Subject> fn) throws IOException, FactoryException, TransformException {
        MathTransform crsTransform = GeotoolsDataStoreUtils.makeCrsTransform(getSourceEncoding());

        while(featureReader.hasNext()) {
            SimpleFeature feature = featureReader.next();
            buildSubjectFromFeature(feature, crsTransform).ifPresent(subject -> {
                fn.accept(feature, subject);
                subjectBuffer.add(subject);
            });
            flushBufferIfRequired();
        }

        flushBuffer();
    }

    private Optional<Geometry> extractNormalizedGeometry(SimpleFeature feature, MathTransform crsTransform) throws TransformException {
        try {
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            Geometry transformedGeom = JTS.transform(geom, crsTransform);
            transformedGeom.setSRID(Subject.SRID);
            return Optional.of(transformedGeom);
        } catch (ProjectionException e) {
            log.warn("Rejecting feature {}. You will see this if you have assertions enabled (e.g. " +
                    "you run with `-ea`) as GeoTools runs asserts. See source of GeotoolsDataStoreUtils for details on this. " +
                    "To fix this, replace `-ea` with `-ea -da:org.geotools...` in your test VM options (probably in" +
                    "your IDE) to disable assertions in GeoTools.", feature.getID());
            return Optional.empty();
        }
    }

    private Optional<Subject> buildSubjectFromFeature(SimpleFeature feature, MathTransform crsTransform) throws TransformException {
        Optional<Geometry> optionalGeometry = extractNormalizedGeometry(feature, crsTransform);
        return optionalGeometry.flatMap(geometry -> {
            Subject subject = applyFeatureAttributesToSubject(new Subject(), feature);
            subject.setShape(geometry);
            return Optional.of(subject);
        });
    }

    private List<TimedValue> buildTimedValuesFromFeature(Datasource datasource, SimpleFeature feature, Subject subject) {
        List<TimedValue> timedValues = new ArrayList<>();
        LocalDateTime modified;
        try {
             modified = getTimestampForFeature(feature);
        } catch (ParsingException pe) {
            log.warn("Unable to get timestamp for feature: {}", feature);
            return timedValues;
        }
        for (Attribute attribute : datasource.getTimedValueAttributes()){
            if (feature.getAttribute(attribute.getLabel()) == null)
                continue;
            Double value = Double.parseDouble(feature.getAttribute(attribute.getLabel()).toString());
            timedValues.add(new TimedValue(subject, attribute, modified, value));
        }
        return timedValues;
    }

    private List<FixedValue> buildFixedValuesFromFeature(Datasource datasource, SimpleFeature feature, Subject subject) {
        List<FixedValue> fixedValues = new ArrayList<>();

        for (Attribute attribute : datasource.getFixedValueAttributes()){
            if (feature.getAttribute(attribute.getLabel()) == null)
                continue;
            String value = feature.getAttribute(attribute.getLabel()).toString();
            fixedValues.add(new FixedValue(subject, attribute, value));
        }
        return fixedValues;
    }

    protected DataStore getDataStoreForDatasource(Datasource datasource) throws IOException {
        return DataStoreFinder.getDataStore(getParamsForDatasource(datasource));
    }

    private void flushBufferIfRequired(){
        int bufferSize = timedValueBuffer.size() + fixedValueBuffer.size() + subjectBuffer.size();
        if (bufferSize > getCombinedBufferSize()) {
            flushBuffer();
        }
    }

    private void flushBuffer() {
        // We must save the subjects first
        saveAndClearSubjectBuffer(subjectBuffer);
        saveAndClearTimedValueBuffer(timedValueBuffer);
        saveAndClearFixedValueBuffer(fixedValueBuffer);
    }
}
