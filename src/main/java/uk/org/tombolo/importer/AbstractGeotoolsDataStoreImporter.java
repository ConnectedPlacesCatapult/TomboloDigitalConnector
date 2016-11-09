package uk.org.tombolo.importer;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureReader;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.utils.GeotoolsDataStoreUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class AbstractGeotoolsDataStoreImporter extends AbstractImporter {
    private static Logger log = LoggerFactory.getLogger(AbstractGeotoolsDataStoreImporter.class);

    private int bufferThreshold = 10000;

    private List<TimedValue> timedValueBuffer = new ArrayList<>();
    private List<FixedValue> fixedValueBuffer = new ArrayList<>();
    List<Subject> subjectBuffer = new ArrayList<>();

    protected abstract Map<String, Object> getParamsForDatasource(Datasource datasource);
    protected abstract Subject applyFeatureAttributesToSubject(Subject subject, SimpleFeature feature);
    protected abstract String getSourceEncoding();
    protected abstract LocalDateTime getTimestampForFeature(SimpleFeature feature);
    protected abstract String getTypeNameForDatasource(Datasource datasource);

    final public int importDatasource(Datasource datasource) throws Exception {
        // Save provider and attributes
        saveProviderAndAttributes(datasource);

        DataStore dataStore = getDataStoreForDatasource(datasource);
        FeatureReader<SimpleFeatureType, SimpleFeature> featureReader = GeotoolsDataStoreUtils.getFeatureReader(dataStore, getTypeNameForDatasource(datasource));

        // Load attribute values
        int counter = withSubjects(featureReader, dataStore, (feature, subject) -> {
            timedValueBuffer.addAll(buildTimedValuesFromFeature(datasource, feature, subject));
            fixedValueBuffer.addAll(buildFixedValuesFromFeature(datasource, feature, subject));
        });

        featureReader.close();
        dataStore.dispose();

        return counter;
    }

    private int withSubjects(FeatureReader<SimpleFeatureType, SimpleFeature> featureReader, DataStore dataStore, BiConsumer<SimpleFeature, Subject> fn) throws IOException, FactoryException, TransformException {
        MathTransform crsTransform = GeotoolsDataStoreUtils.makeCrsTransform(getSourceEncoding());
        int valueCounter = 0;

        while(featureReader.hasNext()) {
            SimpleFeature feature = featureReader.next();
            buildSubjectFromFeature(feature, crsTransform).ifPresent(subject -> {
                fn.accept(feature, subject);
                subjectBuffer.add(subject);
            });
            valueCounter += flushBufferIfRequired();
        }

        valueCounter += flushBuffer();

        log.info("Total values written: {}", valueCounter);
        return valueCounter;
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
        LocalDateTime modified = getTimestampForFeature(feature);
        List<TimedValue> timedValues = new ArrayList<>();

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

    private int flushBufferIfRequired(){
        int bufferSize = timedValueBuffer.size() + fixedValueBuffer.size() + subjectBuffer.size();
        if (bufferSize > bufferThreshold) {
            return flushBuffer();
        } else {
            return 0;
        }
    }

    private int flushBuffer() {
        int bufferSize = timedValueBuffer.size() + fixedValueBuffer.size();  // This isn't a bug — we don't count the subjects we've saved

        // We must save the subjects first
        log.info("Preparing to write a batch of {} subjects ...", subjectBuffer.size());
        SubjectUtils.save(subjectBuffer);
        subjectBuffer.clear();

        log.info("Preparing to write a batch of {} timed values ...", timedValueBuffer.size());
        TimedValueUtils.save(timedValueBuffer);
        timedValueBuffer.clear();

        log.info("Preparing to write a batch of {} fixed values ...", fixedValueBuffer.size());
        FixedValueUtils.save(fixedValueBuffer);
        fixedValueBuffer.clear();

        return bufferSize;
    }
}
