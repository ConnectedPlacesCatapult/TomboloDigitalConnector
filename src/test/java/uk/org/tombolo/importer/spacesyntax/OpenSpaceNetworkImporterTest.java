package uk.org.tombolo.importer.spacesyntax;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OpenSpaceNetworkImporterTest extends AbstractTest {
    OpenSpaceNetworkImporter importer;

    @Before
    public void setUp() throws Exception {
        importer = new OpenSpaceNetworkImporter();
        importer.setDownloadUtils(makeTestDownloadUtils());
        Properties props = new Properties();
        props.put("openSpaceNetworkUsername", "tombolo");
        props.put("openSpaceNetworkPassword", "Catapult16");
        importer.configure(props);
    }

    @Test
    public void getProvider() throws Exception {
        Provider provider = importer.getProvider();

        assertEquals("com.spacesyntax", provider.getLabel());
        assertEquals("Space Syntax", provider.getName());
    }

    @Test
    public void getAllDatasources() throws Exception {
        List<Datasource> datasources = importer.getAllDatasources();
        assertEquals(0,datasources.size());
    }

    @Test
    public void getDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("milton_keynes.osm_polyline_processed");

        assertEquals("milton_keynes.osm_polyline_processed",datasource.getId());
        assertEquals("milton_keynes.osm_polyline_processed",datasource.getName());
        assertEquals("",datasource.getDescription());
        assertNull(datasource.getLocalDatafile());
        assertNull(datasource.getRemoteDatafile());

        assertEquals(1, datasource.getTimedValueAttributes().size());
        assertEquals(5, datasource.getFixedValueAttributes().size());
    }

    @Test
    public void importDatasource() throws Exception {
        int importedCount = importer.importDatasource("milton_keynes.osm_polyline_processed");
        assertEquals(69489, importedCount);

        Subject streetSegment = SubjectUtils.getSubjectByLabel("osm_polyline_processed:osm_polyline_processed.12"); // or osm_polyline_processed:osm_polyline_processed.12

        assertEquals("osm_polyline_processed:osm_polyline_processed.12", streetSegment.getName());
        assertEquals("SSxNode", streetSegment.getSubjectType().getLabel());
        assertEquals(-0.734972, streetSegment.getShape().getCentroid().getX(), 1.0E-6);
        assertEquals(52.043647, streetSegment.getShape().getCentroid().getY(), 1.0E-6);

        // Test fixed values
        Attribute roadClassesAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "road_classes");
        FixedValue roadClassesValue = FixedValueUtils.getBySubjectAndAttribute(streetSegment, roadClassesAttribute);
        assertEquals("{Unclassified}", roadClassesValue.getValue());

        Attribute roadNamesAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "road_names");
        FixedValue roadNamesValue = FixedValueUtils.getBySubjectAndAttribute(streetSegment, roadNamesAttribute);
        assertEquals("{\"Walbrook Avenue\"}", roadNamesValue.getValue());

        Attribute osRoadIdsAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "os_road_ids");
        FixedValue osRoadIdsValue = FixedValueUtils.getBySubjectAndAttribute(streetSegment, osRoadIdsAttribute);
        assertEquals("{114eecf0-4d7a-4c61-9ce0-63cfdeaca735}", osRoadIdsValue.getValue());

        // Test timed values
        Attribute angularCost = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "abwc_n");
        List<TimedValue> angularCosts = TimedValueUtils.getBySubjectAndAttribute(streetSegment, angularCost);
        assertEquals(1, angularCosts.size());
        assertEquals(4.12235391281414, angularCosts.get(0).getValue(), 1.0E-6);
    }

}