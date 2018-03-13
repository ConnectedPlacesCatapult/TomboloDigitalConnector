package uk.org.tombolo;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.importer.Importer;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ImporterSpecificationRunnerTest {

    Importer importer;
    Datasource datasource;
    ImporterSpecificationRunner importerSpecificationRunner = new ImporterSpecificationRunner();;


    @Before
    public void setUp() throws Exception {

        importer = importerSpecificationRunner.getImporter("uk.org.tombolo.importer.dft.TrafficCountImporter");
        datasource = importerSpecificationRunner.getDatasource("trafficVolume", importer);

    }

    @Test
    public void testGetDatasourceIds() throws Exception {
        List<String> datasources = importerSpecificationRunner.getDatasourceIds(importer);
        assertEquals(2, datasources.size());
    }

    @Test
    public void testGetDatasource() throws Exception {
        assertEquals("trafficCounter", datasource.getSubjectTypes().get(0).getLabel());
    }

    @Test
    public void testGetTimedValueAttributes() throws Exception {
        List<String> timedValues = importerSpecificationRunner.getTimedValueAttributes(datasource);
        assertEquals(6, timedValues.size());
    }

    @Test
    public void testGetFixedValueAttributes() throws Exception {
        List<String> fixedValues = importerSpecificationRunner.getFixedValueAttributes(datasource);
        assertEquals(4, fixedValues.size());
    }

    @Test
    public void testGetSubjectType() throws Exception {
        List<String> subjectTypes = importerSpecificationRunner.getSubjectType(datasource);
        assertEquals(1, subjectTypes.size());
    }

    @Test
    public void testGetProvider() throws Exception {
        assertEquals("uk.gov.dft", importer.getProvider().getLabel());
    }
}
