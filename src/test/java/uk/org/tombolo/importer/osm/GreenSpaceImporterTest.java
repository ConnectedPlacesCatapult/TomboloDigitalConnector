package uk.org.tombolo.importer.osm;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for green space importer from open street map
 *
 * File: aHR0cDovL2Rvd25sb2FkLmdlb2ZhYnJpay5kZS9ldXJvcGUvZ3JlYXQtYnJpdGFpbi9lbmdsYW5kL2RvcnNldC1sYXRlc3Qub3NtLnBiZg==.osm.pbf
 */
public class GreenSpaceImporterTest extends AbstractTest {
    private static final String TEST_AREA = "europe/great-britain/england/dorset";
    private static GreenSpaceImporter importer;

    @Before
    public void before(){
        importer = new GreenSpaceImporter(TestFactory.DEFAULT_CONFIG);
        mockDownloadUtils(importer);
    }

    @Test
    public void getDatasource() throws Exception {
        List<String> datasources = importer.getDatasourceIds();

        assertEquals(1, datasources.size());
        assertEquals("OSMGreenSpace", datasources.get(0));
    }

    @Test
    public void getFixedValueAttributes() throws Exception {
        List<Attribute> attributes = importer.getFixedValuesAttributes();
        assertEquals(2, attributes.size());
    }

    @Test
    public void importDatasource() throws Exception {
        importer.importDatasource("OSMGreenSpace", Arrays.asList(TEST_AREA), Collections.emptyList());

        // Test attribute import
        Attribute landuse = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "landuse");
        assertEquals("org.openstreetmap", landuse.getProvider().getLabel());
        assertEquals("landuse", landuse.getLabel());
        assertEquals("landuse", landuse.getName());
        Attribute natural = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "natural");
        assertEquals("org.openstreetmap", natural.getProvider().getLabel());
        assertEquals("natural", natural.getLabel());
        assertEquals("natural", natural.getName());

        // Test subjects import
        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel("org.openstreetmap","OSMEntity");
        Subject osm1 = SubjectUtils.getSubjectByTypeAndLabel(subjectType,"osm355831382");
        assertNull(osm1.getName());
        testFixedValue(osm1, "natural", "wood");

        Subject osm2 = SubjectUtils.getSubjectByTypeAndLabel(subjectType, "osm233595507");
        assertNull(osm2.getName());
        testFixedValue(osm2, "landuse", "forest");
        testFixedValue(osm2, "source", "OS_OpenData_VectorMapDistrict");

        Subject osm3 = SubjectUtils.getSubjectByTypeAndLabel(subjectType, "osm254331534");
        assertEquals("The Warren Wood", osm3.getName());
        testFixedValue(osm3, "natural", "wood");
        testFixedValue(osm3, "name", "The Warren Wood");
    }

    private void testFixedValue(Subject subject, String attributeLabel, String value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, attribute);
        assertEquals("Value for key (" + subject.getLabel() + "," + attributeLabel + ")", value, fixedValue.getValue());
    }
}
