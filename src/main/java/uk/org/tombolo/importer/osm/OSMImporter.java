package uk.org.tombolo.importer.osm;

import de.topobyte.osm4j.pbf.seq.PbfReader;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Config;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Open street map importer
 */
public abstract class OSMImporter extends AbstractImporter {

    protected static final String URL = "http://download.geofabrik.de";
    private static final String DEFAULT_AREA = "europe/great-britain";


    protected DatasourceSpec datasourceSpec;
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
    public DatasourceSpec getDatasourceSpec(String datasourceIdString) throws Exception {
        return datasourceSpec;
    }

    @Override
    public List<SubjectType> getSubjectTypes(String datasourceId) {
        subjectType = new SubjectType(getProvider(), "OSMEntity", "Open Street Map Entity");

        return Collections.singletonList(subjectType);
    }

    @Override
    public List<Attribute> getFixedValueAttributes(String datasourceId) {
        List<Attribute> attributes = new ArrayList<>();
        categories.keySet().stream().map(category -> attributeFromTag(category)).forEach(attributes::add);
        return attributes;
    }

    Attribute attributeFromTag(String tag){
        return new Attribute(getProvider(), AttributeUtils.nameToLabel(tag), "OSM entity having category "+tag);
    }

    private File getDatafile(String area) throws Exception {
        return downloadUtils.fetchFile(new URL(compileURL(area)), getProvider().getLabel(), ".osm.pbf");
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        if (geographyScope == null || geographyScope.isEmpty())
            geographyScope = Arrays.asList(DEFAULT_AREA);

        for (String area : geographyScope) {
            File localFile = getDatafile(area);

            // Since we cannot know the attributes until import time, we store them now
            List<Attribute> attributes = datasource.getFixedValueAttributes();
            AttributeUtils.save(attributes);

            // Create a reader for PBF data and cache it
            PbfReader reader = new PbfReader(localFile, true);
            OSMEntityHandler handler = new OSMEntityHandler(this);
            reader.setHandler(handler);
            reader.read();
        }
    }
}
