package uk.org.tombolo.importer.osm;

import com.vividsolutions.jts.geom.*;
import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for green space importer from open street map
 *
 * File: 87e9e913-8849-3dc9-8f67-9dac9fdeccb4.osm.pbf
 */
public class OSMImporterTest extends AbstractTest {
    private static String TEST_AREA = "europe/great-britain/england/herefordshire";
    private static OSMImporter importer;

    @Before
    public void before(){
        importer = new OSMImporter(TestFactory.DEFAULT_CONFIG);
        mockDownloadUtils(importer);
    }

    @Test
    public void getDatasource() throws Exception {
        List<String> datasources = importer.getDatasourceIds();

        assertEquals(true, datasources.contains("OSMGreenspace"));
    }

    @Test
    public void getFixedValueAttributes() throws Exception {
        List<Attribute> attributes = importer.getFixedValueAttributes("OSMLanduse");
        assertEquals(1, attributes.size());
    }

    @Test
    public void importDatasource() throws Exception {
        importer.importDatasource("OSMHighways", Arrays.asList(TEST_AREA), Collections.emptyList(), null);

        // Test attribute import
        Attribute landuse = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "landuse");
        assertEquals("org.openstreetmap", landuse.getProvider().getLabel());
        assertEquals("landuse", landuse.getLabel());

        Attribute natural = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "natural");
        assertEquals("org.openstreetmap", natural.getProvider().getLabel());
        assertEquals("natural", natural.getLabel());

        // Test subjects import
        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel("org.openstreetmap","OSMEntity");
        Subject osm1 = SubjectUtils.getSubjectByTypeAndLabel(subjectType,"osm35833175");
        assertNull(osm1.getName());
        testFixedValue(osm1, "natural", "wood");

        Subject osm2 = SubjectUtils.getSubjectByTypeAndLabel(subjectType, "osm363465081");
        assertNull(osm2.getName());
        testFixedValue(osm2, "landuse", "forest");
        testFixedValue(osm2, "description", "Plantation");

        Subject osm3 = SubjectUtils.getSubjectByTypeAndLabel(subjectType, "osm126115156");
        assertEquals("Putson Coppice", osm3.getName());
        testFixedValue(osm3, "natural", "wood");
        testFixedValue(osm3, "source", "Bing/OSOpenData");

        // Test importing relations
        Subject osm4 = SubjectUtils.getSubjectByTypeAndLabel(subjectType, "osm1101245");
        assertNull(osm4.getName());
        assertEquals(MultiPolygon.class, osm4.getShape().getClass());
        testFixedValue(osm4, "natural", "wood");
        testFixedValue(osm4, "type", "multipolygon");

        TEST_AREA = "europe/great-britain/england/dorset";
        importer.importDatasource("OSMLanduse", Arrays.asList(TEST_AREA), Collections.emptyList(), Collections.emptyList());

        // Test subjects import
        subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel("org.openstreetmap","OSMEntity");
        osm1 = SubjectUtils.getSubjectByTypeAndLabel(subjectType,"osm355831382");
        assertNull(osm1);

        osm2 = SubjectUtils.getSubjectByTypeAndLabel(subjectType, "osm233595507");
        assertNull(osm2.getName());
        testFixedValue(osm2, "landuse", "forest");
        testFixedValue(osm2, "source", "OS_OpenData_VectorMapDistrict");
    }

    private void testFixedValue(Subject subject, String attributeLabel, String value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, attribute);
        assertEquals("Value for key (" + subject.getLabel() + "," + attributeLabel + ")", value, fixedValue.getValue());
    }

    @Test
    public void testDumpGeometryCollection() throws Exception {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), Subject.SRID);
        MultiPolygon multiPolygon = new MultiPolygon(new Polygon[0], factory);
        Geometry point = TestFactory.makePointGeometry(3.5, 3.5);
        Geometry[] geometries = {multiPolygon, point};

        OSMEntityHandler handler = new OSMEntityHandler(importer, "OSMLanduse");

        Method method = OSMEntityHandler.class.getDeclaredMethod("dumpGeometryCollection", GeometryCollection.class);
        method.setAccessible(true);
        Geometry geometry = (Geometry) method.invoke(handler, new GeometryCollection(geometries, factory));
        assertEquals("Point", geometry.getGeometryType());
    }
}
