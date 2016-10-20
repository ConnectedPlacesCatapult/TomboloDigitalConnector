package uk.org.tombolo.importer.spacesyntax;

import org.geotools.data.FeatureReader;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.*;
import uk.org.tombolo.importer.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
public class OpenSpaceNetworkImporter extends AbstractImporter implements Importer, ShapefileImporter {
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
        // FIXME: Currently the OpenSpaceNetwork has not been published.
        // For the time being we will have to manually make sure that the file is in the right place.
        datasource.setLocalDatafile("osn/"+datasourceId+".zip");
        validateShapefile(datasource);

        // FIXME: Add this when the OpenSpaceNetwork has been released in the wild
        //datasource.setRemoteDatafile("http://what-ever-the-url-will-be.com");

        // Attributes
        // FIXME: This will break if not all features have the same number of attributes (which they do in this example)
        List<Attribute> timedValueAttributes = new ArrayList<>();
        List<Attribute> fixedValueAttributes = new ArrayList<>();
        ShapefileDataStore store = getShapefileDataStoreForDatasource(datasource);
        FeatureReader featureReader = ShapefileUtils.getFeatureReader(store,0);
        if (featureReader.hasNext()){
            SimpleFeature feature = (SimpleFeature) featureReader.next();
            Collection<Property> properties = feature.getProperties();

            for(Property property : properties){
                String propertyName = property.getName().toString();
                switch (propertyName) {
                    case "the_geom":
                        // The geometry is not a proper attribute
                        continue;
                    case "id":
                        // This is an id and we store that as part of the subject
                        continue;
                    case "modified":
                        continue;
                    case "custom_cos":
                        timedValueAttributes.add(new Attribute(getProvider(), propertyName, "Custom cost", "", Attribute.DataType.numeric));
                        continue;
                    case "metric_cos":
                        timedValueAttributes.add(new Attribute(getProvider(), propertyName, "Metric cost", "", Attribute.DataType.numeric));
                        continue;
                    case "angular_co":
                        timedValueAttributes.add(new Attribute(getProvider(), propertyName, "Angular cost", "", Attribute.DataType.numeric));
                        continue;
                    case "id_network":
                        fixedValueAttributes.add(new Attribute(getProvider(), propertyName, "Network Id", "", Attribute.DataType.string));
                        continue;
                    case "class":
                        fixedValueAttributes.add(new Attribute(getProvider(), propertyName, "Node class", "", Attribute.DataType.string));
                        continue;
                    case "street_nam":
                        fixedValueAttributes.add(new Attribute(getProvider(), propertyName, "Street name", "", Attribute.DataType.string));
                        continue;
                    default:
                        // Any attribute that we do not know we assume is a timed value attribute
                        timedValueAttributes.add(new Attribute(getProvider(), propertyName, propertyName, "", Attribute.DataType.numeric));
                }
            }
        }
        datasource.addAllTimedValueAttributes(timedValueAttributes);
        datasource.addAllFixedValueAttributes(fixedValueAttributes);

        return datasource;
    }

    private void validateShapefile(Datasource datasource) throws ConfigurationException {
        try {
            ShapefileDataStore store = getShapefileDataStoreForDatasource(datasource);
            store.getNames();
            store.dispose();
        } catch (Exception e) {
            throw new ConfigurationException("Shapefile invalid.", e);
        }
    }

    private ShapefileDataStore getShapefileDataStoreForDatasource(Datasource datasource) throws IOException {
        File localFile = downloadUtils.getDatasourceFile(datasource);
        Path tempDirectory = ZipUtils.unzipToTemporaryDirectory(localFile);
        return new ShapefileDataStore(Paths.get(tempDirectory.toString(), "/"  + datasource.getId() +".shp").toUri().toURL());
    }


    public int importDatasource(Datasource datasource) throws Exception {
        SubjectType subjectType = SubjectTypeUtils.getOrCreate("SSxNode", "Street segment (node) from an SSx graph");

        ShapefileDataStore store = getShapefileDataStoreForDatasource(datasource);
        FeatureReader featureReader = ShapefileUtils.getFeatureReader(store,0);

        List<Subject> subjects = ShapefileUtils.convertFeaturesToSubjects(featureReader, subjectType, this);
        SubjectUtils.save(subjects);

        featureReader.close();

        // Save provider and attributes
        saveProviderAndAttributes(datasource);

        // Load attribute values
        int valueCounter = loadValues(store, datasource, subjectType);

        store.dispose();

        return valueCounter;
    }

    private int loadValues(ShapefileDataStore store, Datasource datasource, SubjectType subjectType) throws IOException {
        FeatureReader featureReader = ShapefileUtils.getFeatureReader(store,0);
        int fixedValueCounter = 0;
        List<FixedValue> fixedValueBuffer = new ArrayList<>();
        int timedValueCounter = 0;
        List<TimedValue> timedValueBuffer = new ArrayList<>();
        while (featureReader.hasNext()){
            SimpleFeature feature = (SimpleFeature) featureReader.next();
            Subject subject = SubjectUtils.getSubjectByLabel(getFeatureSubjectLabel(feature, subjectType));
            LocalDateTime modified = LocalDateTime.parse(
                    ((String)feature.getAttribute("modified"))
                            .replaceAll(" ", "T")
                            .substring(0,22)
            );

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
                // Flushing buffer inf full
                if (timedValueCounter % timedValueBufferSize == 0)
                    saveTimedValues(timedValueCounter, timedValueBuffer);
            }
        }
        saveTimedValues(timedValueCounter, timedValueBuffer);
        featureReader.close();

        return timedValueCounter + fixedValueCounter;
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
        return feature.getName()+":"+feature.getAttribute("id");
    }

    @Override
    public String getFeatureSubjectName(SimpleFeature feature, SubjectType subjectType) {
        return feature.getName()+":"+feature.getAttribute("id");
    }

    @Override
    public String getEncoding() {
        return "EPSG:27700";
    }
}
