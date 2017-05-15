package uk.org.tombolo.importer.osm;

import com.vividsolutions.jts.geom.Geometry;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import uk.org.tombolo.core.*;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.DataSourceID;
import uk.org.tombolo.importer.GeneralImporter;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Created
 */
public class OSMImporter extends GeneralImporter {

    protected DataSourceID dataSourceID;

    protected String area;
    protected Map<String, List<String>> categories;

    public OSMImporter(Config config) {
        super(config);
    }

    protected String compileURL() {
        StringBuilder builder = new StringBuilder("http://overpass-api.de/api/interpreter?data=");
        builder.append("area[name=\"" + area +"\"];");
        for (String category : categories.keySet()) {
            builder.append("(way[\"" + category + "\"~\"^(");
            String delim = "";
            for (String subcategory : categories.get(category)) {
                builder.append(delim + subcategory);
                delim = "|";
            }
            builder.append(")$\"](area);._; >;);out;");
        }

        return builder.toString()
                .replace("\"", "%22")
                .replace(" ", "%20")
                .replace("^", "%5E")
                .replace("|", "%7C")
                .replace(">", "%3E")
                ;
    }

    @Override
    public Provider getProvider() {
        return new Provider("de.overpass-api", "Open street map API");
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
        List<Attribute> attributes = new ArrayList<>();
        for (String label: Arrays.asList("category", "value")) {
            Attribute attribute = new Attribute(getProvider(),
                    label,
                    "",
                    "",
                    Attribute.DataType.string
            );
            attributes.add(attribute);
        }

        return attributes;
    }

    @Override
    protected void setupUtils(Datasource datasource) throws Exception {
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {
        List<FixedValue> fixedValues = new ArrayList<>();
        List<Subject> subjects = new ArrayList<>();

        InputStream input = downloadUtils.fetchInputStream(new URL(datasource.getRemoteDatafile()), getProvider().getLabel(), ".osm");

        // Create a reader for XML data and cache it
        OsmIterator osmIterator = new OsmXmlIterator(input, false);
        InMemoryMapDataSet data = MapDataSetLoader.read(osmIterator, true, true,
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
                geometry = new GeometryBuilder().build(way, data);
            } catch (EntityNotFoundException e) {
                continue;
            }

            Subject subject = new Subject(
                    datasource.getUniqueSubjectType(),
                    "osm" + way.getId(),
                    tags.get("name"),
                    geometry
            );
            subjects.add(subject);

            String value = "";
            for (Attribute attribute: datasource.getFixedValueAttributes()) {
                switch (attribute.getLabel()) {
                    case "category" :
                        for (String category : categories.keySet()) {
                            if (tags.get(category) == null) continue;
                            value = category;
                        }
                        break;
                    case "value" :
                        value = tags.get(value);
                        break;

                }
                FixedValue fixedValue = new FixedValue(subject, attribute, value);
                fixedValues.add(fixedValue);
            }
        }
        saveAndClearSubjectBuffer(subjects);
        saveAndClearFixedValueBuffer(fixedValues);
    }
}
