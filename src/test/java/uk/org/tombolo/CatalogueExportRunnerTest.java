package uk.org.tombolo;

import org.junit.Test;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.importer.nhschoices.HealthOrganisationImporter;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class CatalogueExportRunnerTest {

    @Test
    public void testGetDatasources() throws Exception {

        Stream<Datasource> datasources = CatalogueExportRunner.getDatasources(HealthOrganisationImporter.class);

        assertEquals(3,datasources.count());
    }
}