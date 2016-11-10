package uk.org.tombolo.importer.spacesyntax;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.importer.AbstractGeotoolsDataStoreImporter;
import uk.org.tombolo.importer.ConfigurationException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Importer for Space Syntax PostGIS database tables
 *
 * Takes a datasource id of the form "schema_name.table_name"
 */
public class OpenSpaceNetworkImporter extends AbstractGeotoolsDataStoreImporter {
    private static Logger log = LoggerFactory.getLogger(OpenSpaceNetworkImporter.class);

    private static final Provider PROVIDER = new Provider("com.spacesyntax","Space Syntax");
    protected static final String PROP_USERNAME = "openSpaceNetworkUsername";
    protected static final String PROP_PASSWORD = "openSpaceNetworkPassword";

    @Override
    public Provider getProvider() {
        return PROVIDER;
    }

    @Override
    public void verifyConfiguration() throws ConfigurationException {
        if (properties.getProperty(PROP_USERNAME) == null)
            throw new ConfigurationException("Property "+PROP_USERNAME+" not defined");
        if (properties.getProperty(PROP_PASSWORD) == null)
            throw new ConfigurationException("Property "+PROP_PASSWORD+" not defined");
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

        Iterator<AttributeType> typeIterator = getAttributesForDatasource(datasource).iterator();

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

    protected Map<String, Object> getParamsForDatasource(Datasource datasource) {
        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", "postgis");
        params.put("host", "spacesyntax.gistemp.com");
        params.put("port", 5432);
        params.put("schema", getSchemaNameForDatasource(datasource));
        params.put("database", "tombolo");
        params.put("user", properties.getProperty(PROP_USERNAME));
        params.put("passwd", properties.getProperty(PROP_PASSWORD));

        return params;
    }

    @Override
    protected Subject applyFeatureAttributesToSubject(Subject subject, SimpleFeature feature) {
        subject.setSubjectType(SubjectTypeUtils.getOrCreate("SSxNode", "Street segment (node) from an SSx graph"));
        subject.setLabel(feature.getName()+":"+feature.getID());
        subject.setName(feature.getName()+":"+feature.getID());
        return subject;
    }

    private String getSchemaNameForDatasource(Datasource datasource) {
        // E.g. for milton_keynes.osm_polyline_processed this returns milton_keynes
        return datasource.getId().split("\\.")[0];
    }

    protected String getTypeNameForDatasource(Datasource datasource) {
        // E.g. for milton_keynes.osm_polyline_processed this returns osm_polyline_processed
        // Analogous to the table name in the PostGIS database
        return datasource.getId().split("\\.")[1];
    }

    @Override
    public LocalDateTime getTimestampForFeature(SimpleFeature feature) {
        return ((Timestamp) feature.getAttribute("time_modified")).toLocalDateTime();
    }

    @Override
    public String getSourceEncoding() {
        return "EPSG:27700";
    }
}
