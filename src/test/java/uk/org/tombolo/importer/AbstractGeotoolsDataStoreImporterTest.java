package uk.org.tombolo.importer;

import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.spacesyntax.OpenSpaceNetworkImporter;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AbstractGeotoolsDataStoreImporterTest extends AbstractTest {

    TestGeotoolsDataStoreImporter importer;

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
            datasource.addTimedValueAttribute(new Attribute(getProvider(), "abwc_n", "Angular Cost", "", Attribute.DataType.numeric));
            return datasource;
        }
    }

    @Before
    public void setUp() throws Exception {
        importer = new TestGeotoolsDataStoreImporter();
        importer.setDownloadUtils(makeTestDownloadUtils());
    }

    @Test
    public void testImportDatasource() throws Exception {
        int importedCount = importer.importDatasource("osm_polyline_processed");
        assertEquals(25, importedCount);
    }
}