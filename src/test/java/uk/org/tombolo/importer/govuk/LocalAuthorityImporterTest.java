package uk.org.tombolo.importer.govuk;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class LocalAuthorityImporterTest extends AbstractTest {
    LocalAuthorityImporter importer;

    @Before
    public void setUp() throws Exception {
        importer = new LocalAuthorityImporter();
        importer.setDownloadUtils(makeTestDownloadUtils());
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
        assertEquals("localAuthority", datasources.get(0).getId());
    }

    @Test
    public void testGetDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("localAuthority");
        assertEquals("localAuthority", datasource.getId());
        assertEquals("uk.gov.data", datasource.getProvider().getLabel());
        assertEquals("Local Authority", datasource.getName());
        assertEquals("Local Authority", datasource.getDescription());
    }

    @Test
    public void testImportLocalAuthorities() throws Exception {
        int importedCount = importer.importDatasource("localAuthority");
        Subject localAuthority = SubjectUtils.getSubjectByLabel("E09000001");

        assertEquals("City of London", localAuthority.getName());
        assertEquals("localAuthority", localAuthority.getSubjectType().getLabel());
        assertEquals(-0.0924380432663645, localAuthority.getShape().getCentroid().getX(), 0.1E-6);
        assertEquals(51.5144138099102, localAuthority.getShape().getCentroid().getY(), 0.1E-6);
        assertEquals(174, importedCount);
    }
}