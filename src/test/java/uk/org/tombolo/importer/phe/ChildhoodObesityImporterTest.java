package uk.org.tombolo.importer.phe;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.importer.Importer;

import java.util.List;

import static org.junit.Assert.*;

public class ChildhoodObesityImporterTest extends AbstractTest {
    private Importer importer;

    @Before
    public void setUp() throws Exception {
        importer = new ChildhoodObesityImporter();
        importer.setDownloadUtils(makeTestDownloadUtils());
        TestFactory.makeNamedSubject("E02000001");  // City of London 001
        TestFactory.makeNamedSubject("E02000564");  // Islington 011
    }

    @Test
    public void getAllDatasources() throws Exception {
        List<Datasource> datasources = importer.getAllDatasources();
        assertEquals(3, datasources.size());
    }

    @Test
    public void getDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("msoaChildhoodObesity2014");

        assertEquals(18, datasource.getAttributes().size());

    }

    @Test
    public void importDatasource() throws Exception {

    }

}