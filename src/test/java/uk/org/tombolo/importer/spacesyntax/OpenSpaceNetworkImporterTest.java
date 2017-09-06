package uk.org.tombolo.importer.spacesyntax;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Using the following test data files:
 *
 * Local: t.b.a.
 */
public class OpenSpaceNetworkImporterTest extends AbstractTest {
    OpenSpaceNetworkImporter importer;

    @Before
    public void setUp() throws Exception {
        importer = new OpenSpaceNetworkImporter(TestFactory.DEFAULT_CONFIG);
        importer.setDownloadUtils(makeTestDownloadUtils());
        Properties props = new Properties();
        props.put("openSpaceNetworkUsername", "");
        props.put("openSpaceNetworkPassword", ""); // Fill these in if you want to run the @Ignored tests
        importer.configure(props);
    }

    @Test
    public void getProvider() throws Exception {
        Provider provider = importer.getProvider();

        assertEquals("com.spacesyntax", provider.getLabel());
        assertEquals("Space Syntax", provider.getName());
    }

    @Test
    public void getDatasourceIds() throws Exception {
        List<String> datasources = importer.getDatasourceIds();
        // FIXME: We should change this when we re-implement the ssx importers
        assertEquals(0,datasources.size());
    }


    // These next two are @Ignored because they contact a live PostGIS server.
    // Don't be surprised if when you connect them up again the data has changed
    // — ideally you'll get the tests working before your change, then check
    // they work again after the change.
    @Test @Ignore
    public void getDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("milton_keynes.osm_polyline_processed");

        assertEquals("milton_keynes.osm_polyline_processed",datasource.getDatasourceSpec().getId());
        assertEquals("milton_keynes.osm_polyline_processed",datasource.getDatasourceSpec().getName());
        assertEquals("",datasource.getDatasourceSpec().getDescription());

        assertEquals(0, datasource.getTimedValueAttributes().size());
        assertEquals(6, datasource.getFixedValueAttributes().size());

        assertEquals(datasource.getFixedValueAttributes().get(0).getLabel(), "os_road_ids");
    }

    @Test @Ignore
    public void importDatasource() throws Exception {
        importer.importDatasource("milton_keynes.osm_polyline_processed", null, null, null);
        assertEquals(69489, importer.getTimedValueCount());

        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(importer.getProvider().getLabel(), "SSxNode");
        Subject streetSegment = SubjectUtils.getSubjectByTypeAndLabel(subjectType, "osm_polyline_processed:osm_polyline_processed.12"); // or osm_polyline_processed:osm_polyline_processed.12

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

        Attribute angularCost = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "abwc_n");
        FixedValue angularCosts = FixedValueUtils.getBySubjectAndAttribute(streetSegment, angularCost);
        assertEquals("4.12235391281414", angularCosts.getValue());
    }

}