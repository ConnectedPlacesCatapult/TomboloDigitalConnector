package uk.org.tombolo.importer;

import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.*;
import uk.org.tombolo.importer.spacesyntax.OpenSpaceNetworkImporter;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class AbstractGeotoolsDataStoreImporterTest extends AbstractTest {

    TestGeotoolsDataStoreImporter importer;
    Consumer<Datasource> datasourceSetup = (o) -> {};

    class TestGeotoolsDataStoreImporter extends AbstractGeotoolsDataStoreImporter {
        @Override
        protected String getTableNameForDatasource(Datasource datasource) {
            return datasource.getId();
        }

        @Override
        protected LocalDateTime getTimestampForFeature(SimpleFeature feature) {
            return TimedValueUtils.parseTimestampString((String) feature.getAttribute("time_modified"));
        }

        @Override
        public SubjectType getSubjectType() {
            return SubjectTypeUtils.getOrCreate("example", "Test Example");
        }

        @Override
        public String getFeatureSubjectLabel(SimpleFeature feature, SubjectType subjectType) {
            return "example-feature:" + feature.getID();
        }

        @Override
        public String getFeatureSubjectName(SimpleFeature feature, SubjectType subjectType) {
            return "Example feature: " + feature.getID();
        }

        @Override
        public String getEncoding() {
            return "EPSG:27700";
        }

        @Override
        public Map<String, Object> getParamsForDatasource(Datasource datasource) {
            Map<String, Object> params = new HashMap<>();
            URL storeUrl = ClassLoader.getSystemResource("datacache/TomboloData/com.spacesyntax/osn/" + datasource.getId() + ".json");
            params.put("url", storeUrl);
            return params;
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
            Datasource datasource = new Datasource(datasourceId, getProvider(), datasourceId, datasourceId);
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
        int importedCount = importer.importDatasource("osm_polyline_processed");
        assertEquals(0, importedCount);

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
        int importedCount = importer.importDatasource("osm_polyline_processed");
        assertEquals(25, importedCount);

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
        int importedCount = importer.importDatasource("osm_polyline_processed");
        assertEquals(25, importedCount);

        Subject streetSegment = SubjectUtils.getSubjectByLabel("example-feature:feature-0");

        // Test fixed values
        Attribute angularCostAttribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "abwc_n");
        FixedValue angularCostValue = FixedValueUtils.getBySubjectAndAttribute(streetSegment, angularCostAttribute);
        assertEquals("4.88016738443536", angularCostValue.getValue());
    }
}