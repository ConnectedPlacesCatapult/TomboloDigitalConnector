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
import uk.org.tombolo.importer.ConfigurationException;
import uk.org.tombolo.importer.DataSourceID;
import uk.org.tombolo.importer.GeneralImporter;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.*;

/**
 * Open street map importer
 */
public class OSMImporter extends GeneralImporter {

    protected static final String URL = "http://overpass-api.de/";
    private File localFile;

    protected DataSourceID dataSourceID;

    public OSMImporter(Config config) {
        super(config);
    }

    protected String compileURL(String area, Map<String, List<String>> categories) {
        StringBuilder builder = new StringBuilder("http://overpass-api.de/api/interpreter?data=");
        builder.append("area[name=\"" + area +"\"];");
        if (categories.isEmpty()) {
            builder.append("(way(area);._; >;);out;");
        }
        for (String category : categories.keySet()) {
            builder.append("(way[~\"" + category + ".*$\"~\"^(");
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
        } else {
            throw new ConfigurationException("Unknown datasourceId: " + datasourceId);
        }
    }

    @Override
    protected List<SubjectType> getSubjectTypes(DataSourceID dataSourceID) {
        return Arrays.asList(new SubjectType(getProvider(), "OSMEntity", "Open Street Map Entity"));
    }

    @Override
    protected List<Attribute> getFixedValuesAttributes(DataSourceID dataSourceID) {
        Set<String> labels = new HashSet<>();
        List<Attribute> attributes = new ArrayList<>();

        OsmIterator osmIterator;
        InMemoryMapDataSet data;

        try {
            osmIterator = new OsmXmlIterator(new FileInputStream(localFile), false);
            data = MapDataSetLoader.read(osmIterator, false, true,
                    false);
        } catch (Exception e) {
            return Collections.emptyList();
        }
        // Iterate contained entities
        Iterator wayIterator = data.getWays().valueCollection().iterator();
        while (wayIterator.hasNext()) {
            labels.addAll(OsmModelUtil.getTagsAsMap((OsmWay) wayIterator.next()).keySet());
        }

        labels.stream().forEach(e -> attributes.add(
                new Attribute(getProvider(), e, e, "", Attribute.DataType.string)));

        return attributes;
    }

    @Override
    protected void setupUtils(Datasource datasource) throws Exception {
        localFile = downloadUtils.fetchFile(new URL(datasource.getRemoteDatafile()), getProvider().getLabel(), ".osm");
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {
        List<FixedValue> fixedValues = new ArrayList<>();
        List<Subject> subjects = new ArrayList<>();


        // Create a reader for XML data and cache it
        OsmIterator osmIterator = new OsmXmlIterator(new FileInputStream(localFile), false);
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

            for (Attribute attribute: datasource.getFixedValueAttributes()) {
                String value = tags.get(attribute.getLabel());
                if (value != null) {
                    FixedValue fixedValue = new FixedValue(subject, attribute, value);
                    fixedValues.add(fixedValue);
                }
            }
        }
        saveAndClearSubjectBuffer(subjects);
        saveAndClearFixedValueBuffer(fixedValues);
    }
}
