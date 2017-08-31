package uk.org.tombolo.importer;

import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.*;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class AbstractGeotoolsDataStoreImporterTest extends AbstractTest {

    TestGeotoolsDataStoreImporter importer;
    private SubjectType testSubjectType;

    // A controlled implementation of the abstract class so we can test it
    class TestGeotoolsDataStoreImporter extends AbstractGeotoolsDataStoreImporter {

        public TestGeotoolsDataStoreImporter(Config config) {
            super(config);
            datasourceIds = Arrays.asList("osm_polyline_processed");
        }

        @Override
        public String getTypeNameForDatasource(Datasource datasource) {
            return datasource.getDatasourceSpec().getId();
        }

        @Override
        public LocalDateTime getTimestampForFeature(SimpleFeature feature) {
            return TimedValueUtils.parseTimestampString((String) feature.getAttribute("time_modified"));
        }

        @Override
        public String getSourceEncoding() {
            return "EPSG:27700";
        }


        public Map<String, Object> getParamsForDatasource(Datasource datasource) {
            Map<String, Object> params = new HashMap<>();
            URL storeUrl = ClassLoader.getSystemResource("datacache/TomboloData/com.spacesyntax/osn/" + datasource.getDatasourceSpec().getId() + ".json");
            params.put("url", storeUrl);
            return params;
        }

        @Override
        protected Subject applyFeatureAttributesToSubject(Subject subject, SimpleFeature feature) {
            subject.setSubjectType(testSubjectType);
            subject.setLabel("example-feature:" + feature.getID());
            subject.setName("Example feature: " + feature.getID());
            return subject;
        }

        @Override
        public Provider getProvider() {
            return TestFactory.DEFAULT_PROVIDER;
        }

        @Override
        public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
            DatasourceSpec datasourceSpec = new DatasourceSpec(TestGeotoolsDataStoreImporter.class, datasourceId, datasourceId, datasourceId, "");
            return datasourceSpec;
        }

        @Override
        public List<Attribute> getDatasourceTimedValueAttributes(String datasourceId) throws Exception {
            return Collections.singletonList(
                        new Attribute(importer.getProvider(), "abwc_n", "Angular Cost", "", Attribute.DataType.numeric));
        }

        @Override
        public List<Attribute> getDatasourceFixedValueAttributes(String datasourceId) throws Exception {
            return Collections.singletonList(
                    new Attribute(importer.getProvider(), "abwc_n", "Angular Cost", "", Attribute.DataType.numeric));
        }
    }

    @Before
    public void setUp() throws Exception {
        importer = new TestGeotoolsDataStoreImporter(TestFactory.DEFAULT_CONFIG);
        importer.setDownloadUtils(makeTestDownloadUtils());
        testSubjectType = SubjectTypeUtils.getOrCreate(TestFactory.DEFAULT_PROVIDER, "example", "Test Example");
    }

    @Test
    public void testImportDatasourceImportsSubjects() throws Exception {

        importer.importDatasource("osm_polyline_processed", null, null, null);
        assertEquals(25, importer.getSubjectCount());

        Subject subject = SubjectUtils.getSubjectByTypeAndLabel(testSubjectType, "example-feature:feature-0");

        assertEquals("Example feature: feature-0", subject.getName());
        assertEquals("example", subject.getSubjectType().getLabel());
        assertEquals(-0.691220, subject.getShape().getCentroid().getX(), 1.0E-6);
        assertEquals(52.053400, subject.getShape().getCentroid().getY(), 1.0E-6);
    }

    @Test
    public void testImportDatasourceImportsTimedAttributes() throws Exception {

        importer.importDatasource("osm_polyline_processed", null, null, null);
        assertEquals(25, importer.getTimedValueCount());

        Subject streetSegment = SubjectUtils.getSubjectByTypeAndLabel(testSubjectType, "example-feature:feature-0");

        // Test fixed values
        Attribute angularCostAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "abwc_n");
        List<TimedValue> angularCostValues = TimedValueUtils.getBySubjectAndAttribute(streetSegment, angularCostAttribute);
        assertEquals(angularCostValues.size(), 1);
        assertEquals(4.880167, angularCostValues.get(0).getValue(), 1.0E-6);
    }

    @Test
    public void testImportDatasourceImportsFixedAttributes() throws Exception {
        importer.importDatasource("osm_polyline_processed", null, null, null);
        assertEquals(25, importer.getFixedValueCount());

        Subject streetSegment = SubjectUtils.getSubjectByTypeAndLabel(testSubjectType, "example-feature:feature-0");

        // Test fixed values
        Attribute angularCostAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "abwc_n");
        FixedValue angularCostValue = FixedValueUtils.getBySubjectAndAttribute(streetSegment, angularCostAttribute);
        assertEquals("4.88016738443536", angularCostValue.getValue());
    }
}