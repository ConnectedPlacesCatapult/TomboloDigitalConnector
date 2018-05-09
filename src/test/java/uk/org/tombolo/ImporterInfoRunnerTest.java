package uk.org.tombolo;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.importer.Importer;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ImporterInfoRunnerTest {

    Importer importer;
    Datasource datasource;
    ImporterInfoRunner importerSpecificationRunner = new ImporterInfoRunner();;


    @Ignore
    @Before
    public void setUp() throws Exception {

        importer = importerSpecificationRunner.getImporter("uk.org.tombolo.importer.dft.TrafficCountImporter");
        datasource = importerSpecificationRunner.getDatasource("trafficVolume", importer);

    }

    @Ignore
    @Test
    public void testGetDatasourceIds() throws Exception {
        List<String> datasources = importerSpecificationRunner.getDatasourceIds(importer);
        assertEquals(2, datasources.size());
    }

    @Ignore
    @Test
    public void testGetDatasource() throws Exception {
        assertEquals("trafficCounter", datasource.getSubjectTypes().get(0).getLabel());
    }

    @Ignore
    @Test
    public void testGetTimedValueAttributes() throws Exception {
        List<String> timedValues = importerSpecificationRunner.getTimedValueAttributes(datasource);
        assertEquals(6, timedValues.size());
    }

    @Ignore
    @Test
    public void testGetFixedValueAttributes() throws Exception {
        List<String> fixedValues = importerSpecificationRunner.getFixedValueAttributes(datasource);
        assertEquals(4, fixedValues.size());
    }

    @Ignore
    @Test
    public void testGetSubjectType() throws Exception {
        List<String> subjectTypes = importerSpecificationRunner.getSubjectType(datasource);
        assertEquals(1, subjectTypes.size());
    }

    @Ignore
    @Test
    public void testGetProvider() throws Exception {
        assertEquals("uk.gov.dft", importer.getProvider().getLabel());
    }
}
