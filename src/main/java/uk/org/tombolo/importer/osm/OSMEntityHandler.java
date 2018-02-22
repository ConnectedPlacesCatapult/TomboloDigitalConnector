package uk.org.tombolo.importer.osm;

import com.vividsolutions.jts.geom.*;
import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.model.iface.*;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import de.topobyte.osm4j.geometry.MissingEntitiesStrategy;
import gnu.trove.map.TLongObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class extending the functionality of OSMHandler to persist only the entities we are interested in and save the ways
 * as subjects while reading.
 *
 */
public class OSMEntityHandler implements OsmHandler {
    public static Logger log = LoggerFactory.getLogger(OSMEntityHandler.class);
    public static final int SIZE_BUFFER = 100000;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), Subject.SRID);

    private final InMemoryMapDataSet dataSet = new InMemoryMapDataSet();
    private final TLongObjectMap<OsmNode> nodes = dataSet.getNodes();
    private final TLongObjectMap<OsmWay> ways = dataSet.getWays();


    private List<FixedValue> fixedValues = new ArrayList<>();
    private List<Subject> subjects = new ArrayList<>();

    private GeometryBuilder builder;
    private OSMImporter importer;

    private String datasourceId;

    public OSMEntityHandler(OSMImporter importer, String datasourceId) {
        this.importer = importer;
        this.datasourceId = datasourceId;

        builder = new GeometryBuilder(GEOMETRY_FACTORY);
        // Throw exception if entities are missing
        builder.setMissingEntitiesStrategy(MissingEntitiesStrategy.THROW_EXCEPTION);
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
        nodes.put(node.getId(), node);
        handleEntity(node, builder.build(node));
    }

    @Override
    public void handle(OsmWay way) throws IOException
    {
        Geometry osmGeometry = null;
        try {
            osmGeometry = builder.build(way, dataSet);
            if (osmGeometry instanceof LinearRing) {
                osmGeometry = new Polygon((LinearRing) osmGeometry, null, GEOMETRY_FACTORY);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Could not build way (illegal argument): {}", e.getMessage());
        } catch (EntityNotFoundException e) {
            // Nothing to do, continue...
            log.warn("Could not build way (entity not found): {}", e.getMessage());
        }
        ways.put(way.getId(), way);
        handleEntity(way, osmGeometry);
    }

    @Override
    public void handle(OsmRelation relation) throws IOException
    {
        Geometry osmGeometry = null;
        try {
            osmGeometry = builder.build(relation, dataSet);
        } catch (EntityNotFoundException e) {
            // Nothing to do, continue...
            log.warn("Could not build way (entity not found): {}", e.getMessage());
        }
        handleEntity(relation, osmGeometry);
    }

    @Override
    public void complete() throws IOException
    {
        // save the remaining subjects and values
        importer.saveAndClearSubjectBuffer(subjects);
        importer.saveAndClearFixedValueBuffer(fixedValues);
    }

    private void handleEntity(OsmEntity entity, Geometry osmGeometry) {
        // Convert the entity's tags to a map
        Map<String, String> tags = OsmModelUtil.getTagsAsMap(entity);
        // Categories
        Map<String, List<String>> categories = OSMBuiltInImporters.valueOf(datasourceId).getCategories();
        // Check if the subject has one of the predefined tags
        categoriesloop:
        for (String categoryKey : categories.keySet()) {
            if (tags.containsKey(categoryKey)) {
                // The entity has at least one tag that matches one of the importers category
                if (categories.get(categoryKey).contains("*")) {
                    // The entity has a tag that matches the category key and the value is a wildcard
                    // Hence we persist the entity
                    persistEntity(entity, osmGeometry, tags);
                    break categoriesloop;
                }

                for (String categoryValue : categories.get(categoryKey)) {
                    if (tags.get(categoryKey).equals(categoryValue)) {
                        // The entity has a tag that matches a category key and value
                        // Hence we persist the entity
                        persistEntity(entity, osmGeometry, tags);
                        break categoriesloop;
                    }
                }
            }
        }
        if (subjects.size() == SIZE_BUFFER) {
            importer.saveAndClearSubjectBuffer(subjects);
            importer.saveAndClearFixedValueBuffer(fixedValues);
        }

    }

    /**
     * When dealing with GeometryCollection-s we want to be sure that we persist on the database a valid geometry.
     * From a valid GeometryCollection we get if possible the next element that is not empty.
     *
     * @param geometryCollection Geometry for the subject
     *
     * @return Returns the chosen geometry from the collection
     */
    private Geometry dumpGeometryCollection(GeometryCollection geometryCollection) {
        Geometry element = new GeometryFactory().createGeometryCollection(null);
        int elementIndex = 0;
        while (elementIndex < geometryCollection.getDimension()) {
            element = geometryCollection.getGeometryN(elementIndex);
            if (!element.isEmpty()) {
                return element;
            }
            elementIndex++;
        }

        return element;
    }

    private void persistEntity(OsmEntity entity, Geometry geometry, Map<String, String> tags) {
        // If the geometry is null, not valid or empty, it will be skipped
        if (geometry == null || !geometry.isValid() || geometry.isEmpty()) {
            log.warn("Could not build {}: {} (geometry not valid or empty): {}", entity.getClass(), entity.getId(),
                    geometry);
            return;
        }

        // Check if it's a GeometryCollection and dump it to a chosen geometry eventually
        if (geometry instanceof GeometryCollection && !(geometry instanceof MultiPolygon)) {
            Geometry chosenGeo = dumpGeometryCollection((GeometryCollection) geometry);
            if (chosenGeo.isEmpty()) {
                log.warn("Could not build {}: {} (geometry collection contains only empty geometries): {}",
                        entity.getClass(), entity.getId(), geometry);
                return;
            }
            geometry = chosenGeo;
        }

        geometry.setSRID(Subject.SRID);
        // Save subject
        Subject subject = new Subject(
                importer.getSubjectType(),
                "osm" + entity.getId(),
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
