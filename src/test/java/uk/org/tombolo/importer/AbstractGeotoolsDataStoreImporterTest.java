package uk.org.tombolo.importer;

import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.*;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class AbstractGeotoolsDataStoreImporterTest extends AbstractTest {

    TestGeotoolsDataStoreImporter importer;
    Consumer<Datasource> datasourceSetup = (o) -> {};

    // A controlled implementation of the abstract class so we can test it
    class TestGeotoolsDataStoreImporter extends AbstractGeotoolsDataStoreImporter {

        public TestGeotoolsDataStoreImporter() {
            datasourceIds = Arrays.asList("osm_polyline_processed");
        }

        @Override
        public String getTypeNameForDatasource(Datasource datasource) {
            return datasource.getId();
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
            URL storeUrl = ClassLoader.getSystemResource("datacache/TomboloData/com.spacesyntax/osn/" + datasource.getId() + ".json");
            params.put("url", storeUrl);
            return params;
        }

        @Override
        protected Subject applyFeatureAttributesToSubject(Subject subject, SimpleFeature feature) {
            subject.setSubjectType(SubjectTypeUtils.getOrCreate("example", "Test Example"));
            subject.setLabel("example-feature:" + feature.getID());
            subject.setName("Example feature: " + feature.getID());
            return subject;
        }

        @Override
        public Provider getProvider() {
            return new Provider("org.example", "Example");
        }

        @Override
        public List<Datasource> getAllDatasources() throws Exception {
            return null;
        }

        @Override
        public Datasource getDatasource(String datasourceId) throws Exception {
            Datasource datasource = new Datasource(TestGeotoolsDataStoreImporter.class, datasourceId, getProvider(), datasourceId, datasourceId);
            datasourceSetup.accept(datasource);
            return datasource;
        }
    }

    @Before
    public void setUp() throws Exception {
        importer = new TestGeotoolsDataStoreImporter();
        importer.setDownloadUtils(makeTestDownloadUtils());
    }

    @Test
    public void testImportDatasourceImportsSubjects() throws Exception {
        importer.importDatasource("osm_polyline_processed", null, null);
        assertEquals(0, importer.getTimedValueCount());

        Subject subject = SubjectUtils.getSubjectByLabel("example-feature:feature-0");

        assertEquals("Example feature: feature-0", subject.getName());
        assertEquals("example", subject.getSubjectType().getLabel());
        assertEquals(-0.691220, subject.getShape().getCentroid().getX(), 1.0E-6);
        assertEquals(52.053400, subject.getShape().getCentroid().getY(), 1.0E-6);
    }

    @Test
    public void testImportDatasourceImportsTimedAttributes() throws Exception {
        datasourceSetup = datasource -> {
            datasource.addTimedValueAttribute(new Attribute(importer.getProvider(), "abwc_n", "Angular Cost", "", Attribute.DataType.numeric));
        };
        importer.importDatasource("osm_polyline_processed", null, null);
        assertEquals(25, importer.getTimedValueCount());

        Subject streetSegment = SubjectUtils.getSubjectByLabel("example-feature:feature-0");

        // Test fixed values
        Attribute angularCostAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "abwc_n");
        List<TimedValue> angularCostValues = TimedValueUtils.getBySubjectAndAttribute(streetSegment, angularCostAttribute);
        assertEquals(angularCostValues.size(), 1);
        assertEquals(4.880167, angularCostValues.get(0).getValue(), 1.0E-6);
    }

    @Test
    public void testImportDatasourceImportsFixedAttributes() throws Exception {
        datasourceSetup = datasource -> {
            datasource.addFixedValueAttribute(new Attribute(importer.getProvider(), "abwc_n", "Angular Cost", "", Attribute.DataType.numeric));
        };
        importer.importDatasource("osm_polyline_processed", null, null);
        assertEquals(25, importer.getFixedValueCount());

        Subject streetSegment = SubjectUtils.getSubjectByLabel("example-feature:feature-0");

        // Test fixed values
        Attribute angularCostAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "abwc_n");
        FixedValue angularCostValue = FixedValueUtils.getBySubjectAndAttribute(streetSegment, angularCostAttribute);
        assertEquals("4.88016738443536", angularCostValue.getValue());
    }
}