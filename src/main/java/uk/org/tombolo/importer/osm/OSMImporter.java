package uk.org.tombolo.importer.osm;

import com.vividsolutions.jts.geom.*;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import de.topobyte.osm4j.pbf.seq.PbfIterator;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.importer.*;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.*;

/**
 * Open street map importer
 */
public abstract class OSMImporter extends AbstractImporter implements Importer{

    protected static final String URL = "http://download.geofabrik.de";
    private static final String DEFAULT_AREA = "europe/great-britain";

    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), Subject.SRID);

    protected DataSourceID dataSourceID;
    protected Map<String, List<String>> categories = Collections.emptyMap();

    public OSMImporter(Config config) {
        super(config);
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

    private Attribute attributeFromTag(String tag){
        return new Attribute(getProvider(), tag, tag, "OSM entity having category "+tag, Attribute.DataType.string);
    }

    private File getDatafile(String area) throws Exception {
        return downloadUtils.fetchFile(new URL(compileURL(area)), getProvider().getLabel(), ".osm.pbf");
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {
        List<FixedValue> fixedValues = new ArrayList<>();
        List<Subject> subjects = new ArrayList<>();

        SubjectType subjectType = SubjectTypeUtils.getOrCreate(
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
            OsmIterator osmIterator = new PbfIterator(new FileInputStream(localFile), true);
            InMemoryMapDataSet data = MapDataSetLoader.read(osmIterator, false, true,
                    false);
            // Iterate contained entities
            Iterator wayIterator = data.getWays().valueCollection().iterator();
            data.getNodes();
            while (wayIterator.hasNext()) {
                // Get the way from the container
                OsmWay way = (OsmWay) wayIterator.next();

                // Convert the way's tags to a map
                Map<String, String> tags = OsmModelUtil.getTagsAsMap(way);

                Geometry geometry;
                try {
                    Geometry osmGeometry = new GeometryBuilder(geometryFactory).build(way, data);
                    if (osmGeometry instanceof LinearRing) {
                        geometry = new Polygon((LinearRing) osmGeometry, null, geometryFactory);
                    } else {
                        geometry = osmGeometry;
                    }
                } catch (EntityNotFoundException e) {
                    continue;
                }

                // Check if the subject has one of the predefined tags
                boolean attributeMatch = false;
                for (Attribute attribute : attributes) {
                    String value = tags.get(attribute.getLabel());
                    if (value != null
                            && (categories.get(attribute.getLabel()).contains(value)
                            || categories.get(attribute.getLabel()).isEmpty())) {
                        attributeMatch = true;
                        break;
                    }
                }

                // We only add the subject if if has a match with one of the predefined tags
                if (attributeMatch){
                    // Save subject
                    Subject subject = new Subject(
                            subjectType,
                            "osm" + way.getId(),
                            tags.get("name"),
                            geometry
                    );
                    subjects.add(subject);

                    // Save fixed attributes
                    for (String tag : tags.keySet()){
                        Attribute attribute = AttributeUtils.getByProviderAndLabel(getProvider(), tag);
                        if(attribute == null) {
                            attribute = attributeFromTag(tag);
                            AttributeUtils.save(attribute);
                        }
                        FixedValue fixedValue = new FixedValue(subject, attribute, tags.get(tag));
                        fixedValues.add(fixedValue);
                    }
                }
            }
            saveAndClearSubjectBuffer(subjects);
            saveAndClearFixedValueBuffer(fixedValues);
        }
    }
}
