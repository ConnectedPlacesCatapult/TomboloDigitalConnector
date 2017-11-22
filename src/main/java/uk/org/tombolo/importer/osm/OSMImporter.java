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
public class OSMImporter extends AbstractImporter {
    protected static final String URL = "http://download.geofabrik.de";
    // Default area is the whole Great Britain, if the geography scope is empty or null the default area will be considered.
    // The following are the geographic regions for the UK as in geofabrik download server.
    private static final List<String> DEFAULT_AREA = Arrays.asList(
            "europe/great-britain/england/berkshire",
            "europe/great-britain/england/buckinghamshire",
            "europe/great-britain/england/cambridgeshire",
            "europe/great-britain/england/cheshire",
            "europe/great-britain/england/cornwall",
            "europe/great-britain/england/cumbria",
            "europe/great-britain/england/derbyshire",
            "europe/great-britain/england/devon",
            "europe/great-britain/england/dorset",
            "europe/great-britain/england/east-sussex",
            "europe/great-britain/england/east-yorkshire-with-hull",
            "europe/great-britain/england/essex",
            "europe/great-britain/england/gloucestershire",
            "europe/great-britain/england/greater-london",
            "europe/great-britain/england/greater-manchester",
            "europe/great-britain/england/hampshire",
            "europe/great-britain/england/herefordshire",
            "europe/great-britain/england/hertfordshire",
            "europe/great-britain/england/isle-of-wight",
            "europe/great-britain/england/kent",
            "europe/great-britain/england/lancashire",
            "europe/great-britain/england/leicestershire",
            "europe/great-britain/england/norfolk",
            "europe/great-britain/england/north-yorkshire",
            "europe/great-britain/england/northumberland",
            "europe/great-britain/england/nottinghamshire",
            "europe/great-britain/england/oxfordshire",
            "europe/great-britain/england/shropshire",
            "europe/great-britain/england/somerset",
            "europe/great-britain/england/south-yorkshire",
            "europe/great-britain/england/staffordshire",
            "europe/great-britain/england/suffolk",
            "europe/great-britain/england/surrey",
            "europe/great-britain/england/west-midlands",
            "europe/great-britain/england/west-sussex",
            "europe/great-britain/england/west-yorkshire",
            "europe/great-britain/england/wiltshire",
            "europe/great-britain/england/worcestershire",
            "europe/great-britain/wales"
    );


    public OSMImporter(Config config) {
        super(config);
        datasourceIds = new ArrayList<>();
        Arrays.stream(OSMBuiltInImporters.values()).map(builtin -> builtin.name()).forEach(datasourceIds::add);
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
        OSMBuiltInImporters builtIn = OSMBuiltInImporters.valueOf(datasourceIdString);
        return new DatasourceSpec(getClass(), builtIn.name(), "", builtIn.getDescription(), URL);
    }

    @Override
    public List<SubjectType> getSubjectTypes(String datasourceId) {
        subjectType = new SubjectType(getProvider(), "OSMEntity", "Open Street Map Entity");

        return Collections.singletonList(subjectType);
    }

    @Override
    public List<Attribute> getFixedValueAttributes(String datasourceId) {
        List<Attribute> attributes = new ArrayList<>();
        OSMBuiltInImporters.valueOf(datasourceId).getCategories().keySet().stream().map(
                category -> attributeFromTag(category)).forEach(attributes::add);
        return attributes;
    }

    Attribute attributeFromTag(String tag){
        return new Attribute(getProvider(), tag, "OSM entity having category " + tag);
    }

    private File getDatafile(String area) throws Exception {
        return downloadUtils.fetchFile(new URL(compileURL(area)), getProvider().getLabel(), ".osm.pbf");
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        if (geographyScope == null || geographyScope.isEmpty())
            geographyScope = DEFAULT_AREA;

        for (String area : geographyScope) {
            File localFile = getDatafile(area);

            // Since we cannot know the attributes until import time, we store them now
            List<Attribute> attributes = datasource.getFixedValueAttributes();
            AttributeUtils.save(attributes);

            // Create a reader for PBF data and cache it
            PbfReader reader = new PbfReader(localFile, true);
            OSMEntityHandler handler = new OSMEntityHandler(this, datasource.getDatasourceSpec().getId());
            reader.setHandler(handler);
            reader.read();
        }
    }
}
