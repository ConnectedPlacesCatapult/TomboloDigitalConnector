package uk.org.tombolo.importer.spacesyntax;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.GeotoolsDataStoreImporter;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.utils.GeotoolsDataStoreUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Importer for Space Syntax shapefiles.
 * This importer is different from the other importers in the sense that there is no online dataset that it imports.
 * The importer tries to import a local data file.
 *
 * Some questions and assumptions:
 *
 * - In the shapefile, all features are road segments (nodes).
 *   Therefore we do not import any edges.
 *   That is anyway not needed at this stage since in this sort of graphs all the graph algorithmic stuff has been done.
 *
 * - In the shapefile there are no information about road name, road type, etc.
 *   We should ask Space Syntax to add this sort of data if possible.
 *
 * - The feature name called Demptmap_R is assumed to be a unique id for the node in the graph.
 *   Hence the subject id that is created is a combination of shapefile name and the Depthmap_R field.
 *
 */
public class OpenSpaceNetworkImporter extends AbstractImporter implements Importer, GeotoolsDataStoreImporter {
    private static Logger log = LoggerFactory.getLogger(OpenSpaceNetworkImporter.class);

    private static final Provider PROVIDER = new Provider("com.spacesyntax","Space Syntax");

    private int fixedValueBufferSize = 10000;
    private int timedValueBufferSize = 10000;

    @Override
    public Provider getProvider() {
        return PROVIDER;
    }

    @Override
    public List<Datasource> getAllDatasources() throws Exception {
        // This function returns an empty list of datasources since it is a local file import
        return new ArrayList<Datasource>();
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {

        // We'll use this ^ for both ID and name as we have nothing else to go by, and an empty description
        Datasource datasource = new Datasource(datasourceId, getProvider(), datasourceId, "");

        DataStore dataStore = getDataStoreForDatasource(datasource);
        SimpleFeatureType schema = dataStore.getSchema(getTableNameForDatasource(datasource));
        Iterator<AttributeType> typeIterator = schema.getTypes().iterator();

        // Attributes
        List<Attribute> timedValueAttributes = new ArrayList<>();
        List<Attribute> fixedValueAttributes = new ArrayList<>();
        while (typeIterator.hasNext()) {
            AttributeType type = typeIterator.next();
            String columnName = type.getName().toString();
            switch (columnName) {
                case "geom":
                    // The geometry is not a proper attribute
                    break;
                case "id":
                    // This is an id and we store that as part of the subject
                    break;
                case "time_modified":
                    break;
                case "os_road_ids":
                    fixedValueAttributes.add(new Attribute(getProvider(), columnName, "OS Road IDs", "", Attribute.DataType.string));
                    break;
                case "os_meridian_ids":
                    fixedValueAttributes.add(new Attribute(getProvider(), columnName, "OS Meridian IDs", "", Attribute.DataType.string));
                    break;
                case "road_classes":
                    fixedValueAttributes.add(new Attribute(getProvider(), columnName, "Road classes", "", Attribute.DataType.string));
                    break;
                case "road_numbers":
                    fixedValueAttributes.add(new Attribute(getProvider(), columnName, "Road numbers", "", Attribute.DataType.string));
                    break;
                case "road_names":
                    fixedValueAttributes.add(new Attribute(getProvider(), columnName, "Road names", "", Attribute.DataType.string));
                    break;
                case "abwc_n":
                    timedValueAttributes.add(new Attribute(getProvider(), columnName, "Angular Cost", "", Attribute.DataType.numeric));
                    break;
                default:
                    // Any attribute that we do not know we assume is a timed value attribute
                    timedValueAttributes.add(new Attribute(getProvider(), columnName, columnName, "", Attribute.DataType.numeric));
            }
        }

        datasource.addAllTimedValueAttributes(timedValueAttributes);
        datasource.addAllFixedValueAttributes(fixedValueAttributes);

        return datasource;
    }


    public int importDatasource(Datasource datasource) throws Exception {
        SubjectType subjectType = SubjectTypeUtils.getOrCreate("SSxNode", "Street segment (node) from an SSx graph");

        // Save provider and attributes
        saveProviderAndAttributes(datasource);

        DataStore dataStore = getDataStoreForDatasource(datasource);

        // Load attribute values
        loadSubjects(datasource, dataStore, subjectType);
        return loadValues(dataStore, datasource, subjectType);
    }

    private int loadSubjects(Datasource datasource, DataStore dataStore, SubjectType subjectType) throws SQLException, IOException, FactoryException, TransformException {
        FeatureReader<SimpleFeatureType, SimpleFeature> featureReader = GeotoolsDataStoreUtils.getFeatureReader(dataStore, getTableNameForDatasource(datasource));
        List<Subject> subjects = GeotoolsDataStoreUtils.convertFeaturesToSubjects(featureReader, subjectType, this);
        SubjectUtils.save(subjects);
        featureReader.close();

        return subjects.size();
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
            LocalDateTime modified = ((Timestamp) feature.getAttribute("time_modified")).toLocalDateTime();

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

    private String getSchemaNameForDatasource(Datasource datasource) {
        return datasource.getId().split("\\.")[0];
    }

    private String getTableNameForDatasource(Datasource datasource) {
        return datasource.getId().split("\\.")[1];
    }

    private DataStore getDataStoreForDatasource(Datasource datasource) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", "postgis");
        params.put("host", "spacesyntax.gistemp.com");
        params.put("port", 5432);
        params.put("schema", getSchemaNameForDatasource(datasource));
        params.put("database", "tombolo");
        params.put("user", "tombolo");
        params.put("passwd", "Catapult16");

        return DataStoreFinder.getDataStore(params);
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

    @Override
    public String getFeatureSubjectLabel(SimpleFeature feature, SubjectType subjectType) {
        return feature.getName()+":"+feature.getID();
    }

    @Override
    public String getFeatureSubjectName(SimpleFeature feature, SubjectType subjectType) {
        return feature.getName()+":"+feature.getID();
    }

    @Override
    public String getEncoding() {
        return "EPSG:27700";
    }
}
