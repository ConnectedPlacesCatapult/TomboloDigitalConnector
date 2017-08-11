package uk.org.tombolo.importer.osm;

import de.topobyte.osm4j.pbf.seq.PbfReader;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.importer.*;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Open street map importer
 */
public abstract class OSMImporter extends AbstractImporter implements Importer{

    protected static final String URL = "http://download.geofabrik.de";
    private static final String DEFAULT_AREA = "europe/great-britain";


    protected DataSourceID dataSourceID;
    protected Map<String, List<String>> categories = Collections.emptyMap();

    public OSMImporter(Config config) {
        super(config);
    }

    private SubjectType subjectType;

    SubjectType getSubjectType() {
        return this.subjectType;
    }

    private String compileURL(String area) {
       return URL + "/" + area + "-latest.osm.pbf";
    }

    @Override
    public Provider getProvider() {
        return new Provider("org.openstreetmap", "Open Street Map");
    }

    @Override
    public Datasource getDatasource(String datasourceIdString) throws Exception {
        if (datasourceExists(datasourceIdString)) {
            Datasource datasource = datasourceFromDatasourceId(dataSourceID);
            datasource.setUrl(dataSourceID.getUrl());
            datasource.addSubjectType(new SubjectType(getProvider(), "OSMEntity", "Open Street Map Entity"));
            return datasource;
        } else {
            throw new ConfigurationException("Unknown datasourceId: " + datasourceIdString);
        }
    }

    protected List<Attribute> getFixedValuesAttributes() throws Exception {
        List<Attribute> attributes = new ArrayList<>();
        for (String category : categories.keySet()) {
            attributes.add(attributeFromTag(category));
        }
        return attributes;
    }

    Attribute attributeFromTag(String tag){
        return new Attribute(getProvider(), tag, tag, "OSM entity having category "+tag, Attribute.DataType.string);
    }

    private File getDatafile(String area) throws Exception {
        return downloadUtils.fetchFile(new URL(compileURL(area)), getProvider().getLabel(), ".osm.pbf");
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {

        subjectType = SubjectTypeUtils.getOrCreate(
                datasource.getUniqueSubjectType().getProvider(),
                datasource.getUniqueSubjectType().getLabel(),
                datasource.getUniqueSubjectType().getName()
        );

        if (geographyScope == null || geographyScope.isEmpty())
            geographyScope = Arrays.asList(DEFAULT_AREA);

        for (String area : geographyScope) {
            File localFile = getDatafile(area);

            // Since we cannot know the attributes until import time, we store them now
            List<Attribute> attributes = getFixedValuesAttributes();
            AttributeUtils.save(attributes);

            // Create a reader for PBF data and cache it
            PbfReader reader = new PbfReader(localFile, true);
            OSMEntityHandler handler = new OSMEntityHandler(this);
            reader.setHandler(handler);
            reader.read();
        }
    }
}
