package uk.org.tombolo.importer.osm;

import com.vividsolutions.jts.geom.*;
import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import de.topobyte.osm4j.geometry.MissingEntitiesStrategy;
import gnu.trove.map.TLongObjectMap;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Class extending the functionality of OSMHandler to persist only the entities we are interested in and save the ways
 * as subjects while reading.
 *
 */
public class OSMEntityHandler implements OsmHandler {
    public static final int SIZE_BUFFER = 100000;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), Subject.SRID);

    private final InMemoryMapDataSet dataSet = new InMemoryMapDataSet();
    private final TLongObjectMap<OsmNode> nodes = dataSet.getNodes();

    private List<FixedValue> fixedValues = new ArrayList<>();
    private List<Subject> subjects = new ArrayList<>();

    private GeometryBuilder builder;
    private OSMImporter importer;

    public OSMEntityHandler(OSMImporter importer) {
        this.importer = importer;

        builder = new GeometryBuilder(GEOMETRY_FACTORY);
        // Empty geography if nodes are missing
        builder.setMissingEntitiesStrategy(MissingEntitiesStrategy.BUILD_EMPTY);
    }

    @Override
    public void handle(OsmBounds bounds) throws IOException
    {
        dataSet.setBounds(bounds);
    }

    @Override
    public void handle(OsmNode node) throws IOException
    {
        // We are not interested in the node tags, but just the coordinates to create a geometry for the ways
        node = new Node(node.getId(), node.getLongitude(), node.getLatitude());
        nodes.put(node.getId(), node);
    }

    @Override
    public void handle(OsmWay way) throws IOException
    {
        // Convert the way's tags to a map
        Map<String, String> tags = OsmModelUtil.getTagsAsMap(way);
        // Check if the subject has one of the predefined tags
        categoriesloop:
        for (Collection<String> category : importer.categories.values()) {
            for (String value : category) {
                if (tags.get(value) != null) {
                    // we keep the way as it is with all the tags
                    persistWay(way, tags);
                    break categoriesloop;
                }
            }
        }
        if (subjects.size() == SIZE_BUFFER) {
            importer.saveAndClearSubjectBuffer(subjects);
            importer.saveAndClearFixedValueBuffer(fixedValues);
        }
    }

    @Override
    public void handle(OsmRelation relation) throws IOException
    {
        // We are not interested in keeping the relations
    }

    @Override
    public void complete() throws IOException
    {
        // save the remaining subjects and values
        importer.saveAndClearSubjectBuffer(subjects);
        importer.saveAndClearFixedValueBuffer(fixedValues);
    }

    private void persistWay(OsmWay way, Map<String, String> tags) {
        Geometry geometry = null;
        try {
            Geometry osmGeometry = builder.build(way, dataSet);
            if (osmGeometry instanceof LinearRing) {
                geometry = new Polygon((LinearRing) osmGeometry, null, GEOMETRY_FACTORY);
            } else {
                geometry = osmGeometry;
            }
        } catch (EntityNotFoundException e) {
            // Nothing to do, continue...
        }

            // Save subject
            Subject subject = new Subject(
                    importer.getSubjectType(),
                    "osm" + way.getId(),
                    tags.get("name"),
                    geometry
            );
            subjects.add(subject);

            // Save fixed attributes
            for (String tag : tags.keySet()){
                Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), tag);
                if(attribute == null) {
                    attribute = importer.attributeFromTag(tag);
                    AttributeUtils.save(attribute);
                }
                FixedValue fixedValue = new FixedValue(subject, attribute, tags.get(tag));
                fixedValues.add(fixedValue);
            }
    }
}
