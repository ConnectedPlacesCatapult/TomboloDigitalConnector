package uk.org.tombolo;

import org.junit.Ignore;
import org.junit.Test;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.dft.AccessibilityImporter;
import uk.org.tombolo.importer.nhschoices.HealthOrganisationImporter;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class CatalogueExportRunnerTest {

    /**
     * This test is ignored for the time being until we make the runner a thin class and move the logic
     * into a CatalogueExportEngine. Currently the Runner needs apiKeys and downloadUtils to be configured.
     */
    @Test
    public void testGetDatasources() throws Exception {

        CatalogueExportRunner catalogueExportRunner = new CatalogueExportRunner();

        Importer importer = catalogueExportRunner.getImporter(AccessibilityImporter.class);

        List<String> datasources = catalogueExportRunner.getDatasourceIds(importer);

        assertEquals(7,datasources.size());
    }
}