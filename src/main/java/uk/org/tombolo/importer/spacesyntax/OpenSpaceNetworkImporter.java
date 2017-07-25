package uk.org.tombolo.importer.spacesyntax;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.importer.AbstractGeotoolsDataStoreImporter;
import uk.org.tombolo.importer.Config;
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
    private static final SubjectType subjectType = new SubjectType(PROVIDER, "SSxNode", "Street segment (node) from an SSx graph");
    protected static final String PROP_USERNAME = "openSpaceNetworkUsername";
    protected static final String PROP_PASSWORD = "openSpaceNetworkPassword";
    static final List<String> NON_ATTRIBUTE_COLUMNS = Arrays.asList("geom", "id", "time_modified");

    public OpenSpaceNetworkImporter(Config config) {
        super(config);
    }

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
    public Datasource getDatasource(String datasourceId) throws Exception {
        // We'll use this ^ for both ID and name as we have nothing else to go by, and an empty description
        Datasource datasource = new Datasource(getClass(), datasourceId, getProvider(), datasourceId, "");

        // Add the attributes
        Iterator<AttributeType> typeIterator = getAttributesForDatasource(datasource).iterator();
        List<Attribute> timedValueAttributes = new ArrayList<>();
        List<Attribute> fixedValueAttributes = new ArrayList<>();


        while (typeIterator.hasNext()) {
            AttributeType type = typeIterator.next();
            String columnName = type.getName().toString();
            if (NON_ATTRIBUTE_COLUMNS.contains(columnName)) { continue; }
            Attribute attribute = new Attribute();
            attribute.setProvider(getProvider());
            attribute.setLabel(columnName);
            attribute.setName(columnName.replace("_", " "));
            if (null != type.getDescription()) {
                attribute.setDescription(type.getDescription().toString());
            } else {
                // If no description, we'll just duplicate the name
                attribute.setDescription(attribute.getName());
            }
            fixedValueAttributes.add(attribute);
        }

        datasource.addAllTimedValueAttributes(timedValueAttributes);
        datasource.addAllFixedValueAttributes(fixedValueAttributes);
        datasource.addSubjectType(subjectType);

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
        subject.setSubjectType(subjectType);
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
