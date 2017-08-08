package uk.org.tombolo.importer;

import org.junit.Test;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AbstractImporterTest {

    TestAbstractImporter importer = new TestAbstractImporter(TestFactory.DEFAULT_CONFIG);

    class TestAbstractImporter extends AbstractImporter {
        public TestAbstractImporter(Config config){
            super(config);
        }

        @Override
        protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, String datasourceLocation) throws Exception {

        }

        @Override
        public Provider getProvider() {
            return null;
        }

        @Override
        public Datasource getDatasource(String datasourceId) throws Exception {
            return null;
        }
    }

    @Test
    public void getDatasourceIds() throws Exception {
        assertTrue(importer.getDatasourceIds().isEmpty());
    }

    @Test
    public void getGeographyLabels() throws Exception {
        assertEquals(Arrays.asList("all"), importer.getGeographyLabels());
    }

    @Test
    public void getTemporalLabels() throws Exception {
        assertEquals(Arrays.asList("all"), importer.getTemporalLabels());
    }
}