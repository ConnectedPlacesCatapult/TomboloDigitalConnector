package uk.org.tombolo.importer.ons;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.ons.OaImporter;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class OaImporterTest extends AbstractTest {
    OaImporter importer;

    @Before
    public void setUp() throws Exception {
        importer = new OaImporter();
        importer.setDownloadUtils(makeTestDownloadUtils());
    }

    @Test
    public void testGetAllDatasources() throws Exception {
        List<Datasource> datasources = importer.getAllDatasources();
        assertEquals(2, datasources.size());
        assertEquals("lsoa", datasources.get(0).getId());
        assertEquals("msoa", datasources.get(1).getId());
    }

    @Test
    public void testGetDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("lsoa");
        assertEquals("lsoa", datasource.getId());
        assertEquals("uk.gov.ons", datasource.getProvider().getLabel());
        assertEquals("LSOA", datasource.getName());
        assertEquals("Lower Layer Super Output Areas", datasource.getDescription());
    }

    @Test
    public void testImportLsoas() throws Exception {
        int importedCount = importer.importDatasource("lsoa");
        Subject lsoa = SubjectUtils.getSubjectByLabel("E01000002");

        assertEquals("City of London 001B", lsoa.getName());
        assertEquals("lsoa", lsoa.getSubjectType().getLabel());
        assertEquals(-0.0925531560156143, lsoa.getShape().getCentroid().getX(), 0.1E-6);
        assertEquals(51.51821461759632, lsoa.getShape().getCentroid().getY(), 0.1E-6);
        assertEquals(34753, importedCount);
    }

    @Test
    public void testImportMsoas() throws Exception {
        int importedCount = importer.importDatasource("msoa");
        Subject lsoa = SubjectUtils.getSubjectByLabel("E02000120");

        assertEquals("Brent 028", lsoa.getName());
        assertEquals("msoa", lsoa.getSubjectType().getLabel());
        assertEquals(-0.200293982706986, lsoa.getShape().getCentroid().getX(), 0.1E-6);
        assertEquals(51.5401585405278, lsoa.getShape().getCentroid().getY(), 0.1E-6);
        assertEquals(7201, importedCount);
    }
}