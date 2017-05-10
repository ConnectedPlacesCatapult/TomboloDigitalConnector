package uk.org.tombolo.importer.osm;

import com.vividsolutions.jts.geom.Geometry;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.DataSourceID;
import uk.org.tombolo.importer.GeneralImporter;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Open street map importer for green spaces. Green spaces include:
 * Parks, gardens, god parks, woods, meadows, forests, orchards
 */
public class GreenSpaceImporter extends GeneralImporter {
    static Logger log = LoggerFactory.getLogger(GreenSpaceImporter.class);

    private DataSourceID dataSourceID;

    public GreenSpaceImporter(Config config) {
        super(config);
        dataSourceID = new DataSourceID(
                "OSMGreenSpace",
                "",
                "Open Street Map green space data",
                "http://overpass-api.de/",
                "http://overpass-api.de/api/interpreter?data=area[name=\"Great Britain\"];" +
                        "(way[\"leisure\"~\"^(park|garden|dog_park)$\"](area));out;" +
                        "(way[\"landuse\"~\"^(meadow|wood|forest|orchard)$\"](area));out;"
        );

        datasourceIds = Arrays.asList(dataSourceID.getLabel());
    }

    @Override
    public Provider getProvider() {
        return new Provider("overpass-api.de", "Open street map API");
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {
        if (dataSourceID.getLabel().equals(datasourceId)) {
            return getDatasource(getClass(), dataSourceID);
        }

        return null;
    }

    @Override
    protected List<SubjectType> getSubjectTypes(DataSourceID dataSourceID) {
        return Arrays.asList(new SubjectType(getProvider(), "OSMEntity", "Open Street Map Entity"));
    }

    @Override
    protected List<Attribute> getFixedValuesAttributes(DataSourceID dataSourceID) {
        return Collections.emptyList();
    }

    @Override
    protected void setupUtils(Datasource datasource) throws Exception {
        // Nothing to do
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {
        Set<Attribute> attributes = new HashSet<>();
        List<FixedValue> fixedValues = new ArrayList<>();
        List<Subject> subjects = new ArrayList<>();

        // Open a stream
        InputStream input = downloadUtils.fetchInputStream(new URL(datasource.getRemoteDatafile()), "", "");

        // Create a reader for XML data
        OsmIterator iterator = new OsmXmlIterator(input, false);
        InMemoryMapDataSet data = MapDataSetLoader.read(iterator, false, false,
                true);

        // Iterate contained entities
        for (EntityContainer container : iterator) {

            // We are interested to ways as they'll give the area of the green spaces
            if (container.getType() == EntityType.Way) {

                // Get the way from the container
                OsmWay way = (OsmWay) container.getEntity();

                // Convert the way's tags to a map
                Map<String, String> tags = OsmModelUtil.getTagsAsMap(way);

                Geometry geometry = new GeometryBuilder().build(way, data);

                Subject subject = new Subject(
                        datasource.getUniqueSubjectType(),
                        way.getId() + "",
                        tags.get("name"),
                        geometry
                );
                subjects.add(subject);

                for (String key: tags.keySet()) {
                    Attribute attribute = new Attribute(getProvider(),
                            AttributeUtils.nameToLabel(key),
                            key.replace("\\s+",""),
                            "",
                            Attribute.DataType.string
                    );
                    attributes.add(attribute);
                    FixedValue fixedValue = new FixedValue(subject, attribute,tags.get(key));
                    fixedValues.add(fixedValue);
                }
            }
        }
        AttributeUtils.save(datasource.getFixedValueAttributes());
        saveAndClearSubjectBuffer(subjects);
        saveAndClearFixedValueBuffer(fixedValues);
    }
}
