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
public class SpaceSyntaxShapefileImporter extends AbstractImporter implements Importer, ShapefileImporter {
    private static Logger log = LoggerFactory.getLogger(SpaceSyntaxShapefileImporter.class);

    public static final Provider PROVIDER = new Provider("com.spacesyntax","Space Syntax");

    protected int timedValueBufferSize = 10000;

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
        // This expects a path to a local file.
        File shapefile = new File(datasourceId);
        validateShapefile(shapefile);

        String id = shapefile.getName().replaceFirst("\\.shp$", "");
        // We'll use this ^ for both ID and name as we have nothing else to go by, and an empty description
        Datasource datasource = new Datasource(id, getProvider(), id, "");
        datasource.setLocalDatafile(datasourceId);

        // Attributes
        // FIXME: This will break if not all features have the same number of attributes (which they do in this example)
        List<Attribute> attributes = new ArrayList<>();
        ShapefileDataStore store = new ShapefileDataStore(Paths.get(datasource.getLocalDatafile()).toUri().toURL());
        FeatureReader featureReader = ShapefileUtils.getFeatureReader(store,0);
        if (featureReader.hasNext()){
            SimpleFeature feature = (SimpleFeature) featureReader.next();
            Collection<Property> properties = feature.getProperties();

            for(Property property : properties){
                String propertyName = property.getName().toString();
                if ("the_geom".equals(propertyName))
                    // The geometry is not a proper attribute
                    continue;
                if ("Depthmap_R".equals(propertyName))
                    // This is an id and we store that as part of the subject
                    continue;
                attributes.add(new Attribute(getProvider(), propertyName, propertyName, "", Attribute.DataType.numeric));
            }
        }
        datasource.addAllAttributes(attributes);

        return datasource;
    }

    private void validateShapefile(File shapefile) throws ConfigurationException {
        try {
            ShapefileDataStore store = new ShapefileDataStore(shapefile.toURI().toURL());
            store.getNames();
            store.dispose();
        } catch (Exception e) {
            throw new ConfigurationException(String.format("Shapefile invalid or not found at '%s'.", shapefile.getPath()), e);
        }
    }


    public int importDatasource(Datasource datasource) throws Exception {
        SubjectType subjectType = SubjectTypeUtils.getOrCreate("SSxNode", "Street segment (node) from an SSx graph");

        ShapefileDataStore store = new ShapefileDataStore(Paths.get(datasource.getLocalDatafile()).toUri().toURL());
        FeatureReader featureReader = ShapefileUtils.getFeatureReader(store,0);

        List<Subject> subjects = ShapefileUtils.convertFeaturesToSubjects(featureReader, subjectType, this);
        SubjectUtils.save(subjects);

        featureReader.close();

        // Load attributes
        // Save provider
        ProviderUtils.save(datasource.getProvider());

        // Save attributes
        AttributeUtils.save(datasource.getAttributes());

        // Load timed values
        int valueCounter = loadTimedValues(store, datasource, subjectType);

        store.dispose();

        return valueCounter;
    }

    private int loadTimedValues(ShapefileDataStore store, Datasource datasource, SubjectType subjectType) throws IOException {
        LocalDateTime importTime = LocalDateTime.now();

        FeatureReader featureReader = ShapefileUtils.getFeatureReader(store,0);
        int valueCounter = 0;
        List<TimedValue> timedValueBuffer = new ArrayList<>();
        while (featureReader.hasNext()){
            SimpleFeature feature = (SimpleFeature) featureReader.next();
            for (Attribute attribute : datasource.getAttributes()){
                Subject subject = SubjectUtils.getSubjectByLabel(getFeatureSubjectLabel(feature, subjectType));
                Double value = (Double) feature.getAttribute(attribute.getLabel());
                timedValueBuffer.add(new TimedValue(subject, attribute, importTime, value));
                valueCounter++;
                // Flushing buffer
                if (valueCounter % timedValueBufferSize == 0){
                    // Buffer is full ... we write values to db
                    log.info("Preparing to write a batch of {} values ...", timedValueBuffer.size());
                    TimedValueUtils.save(timedValueBuffer);
                    timedValueBuffer = new ArrayList<TimedValue>();
                    log.info("Total values written: {}", valueCounter);
                }
            }
        }
        log.info("Preparing to write a batch of {} values ...", timedValueBuffer.size());
        TimedValueUtils.save(timedValueBuffer);
        log.info("Total values written: {}", valueCounter);
        featureReader.close();

        return valueCounter;
    }

    @Override
    public String getFeatureSubjectLabel(SimpleFeature feature, SubjectType subjectType) {
        return feature.getName()+":"+feature.getAttribute("Depthmap_R");
    }

    @Override
    public String getFeatureSubjectName(SimpleFeature feature, SubjectType subjectType) {
        return feature.getName()+":"+feature.getAttribute("Depthmap_R");
    }

    @Override
    public String getEncoding() {
        return "EPSG:27700";
    }
}
