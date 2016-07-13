package uk.org.tombolo.importer.govuk;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.util.List;

import static org.junit.Assert.*;

public class LsoaImporterTest extends AbstractTest {
    LsoaImporter importer;

    @Before
    public void setUp() throws Exception {
        importer = new LsoaImporter();
        importer.setDownloadUtils(makeTestDownloadUtils());
        importer.setTimedValueUtils(new TimedValueUtils());
    }

    @Test
    public void testGetProvider() throws Exception {
        Provider provider = importer.getProvider();
        assertEquals("uk.gov.data", provider.getLabel());
        assertEquals("data.gov.uk", provider.getName());
    }

    @Test
    public void testGetAllDatasources() throws Exception {
        List<Datasource> datasources = importer.getAllDatasources();
        assertEquals(1, datasources.size());
        assertEquals("lsoa", datasources.get(0).getId());
    }

    @Test
    public void testGetDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("lsoa");
        assertEquals("lsoa", datasource.getId());
        assertEquals("uk.gov.data", datasource.getProvider().getLabel());
        assertEquals("LSOA", datasource.getName());
        assertEquals("Lower Layer Super Output Areas", datasource.getDescription());
    }

    @Test
    public void testImportDatasource() throws Exception {
        int importedCount = importer.importDatasource("lsoa");
        Subject lsoa = SubjectUtils.getSubjectByLabel("E01000002");

        assertEquals("City of London 001B", lsoa.getName());
        assertEquals("lsoa", lsoa.getSubjectType().getLabel());
        assertEquals(-0.0925531560156143, lsoa.getShape().getCentroid().getX(), 0.1E-6);
        assertEquals(51.51821461759632, lsoa.getShape().getCentroid().getY(), 0.1E-6);
        assertEquals(34753, importedCount);
    }
}