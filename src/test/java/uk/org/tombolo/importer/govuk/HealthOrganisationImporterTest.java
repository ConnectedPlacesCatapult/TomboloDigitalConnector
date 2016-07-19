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

public class HealthOrganisationImporterTest extends AbstractTest {
    HealthOrganisationImporter importer;

    @Before
    public void setUp() throws Exception {
        importer = new HealthOrganisationImporter();
        mockDownloadUtils(importer);
    }

    @Test
    public void testGetProvider() throws Exception {
        Provider provider = importer.getProvider();
        assertEquals("uk.gov.data", provider.getLabel());
        assertEquals("data.gov.uk", provider.getName());
    }

    @Test
    public void testImportHospitals() throws Exception {
        int recordsImported = importer.importDatasource("hospital");
        Subject subject = SubjectUtils.getSubjectByLabel("40918");
        assertEquals(1106, recordsImported);
        assertEquals("Guy's Hospital", subject.getName());
        assertEquals(51.5046, subject.getShape().getCoordinate().getOrdinate(1), 0.0001);
        // This is in the form 0.0Ex in the JSON so we test on this
        assertEquals(-0.0889, subject.getShape().getCoordinate().getOrdinate(0), 0.0001);
    }

    @Test
    public void testImportClinics() throws Exception {
        int recordsImported = importer.importDatasource("clinic");
        Subject subject = SubjectUtils.getSubjectByLabel("12366");
        assertEquals(8416, recordsImported);
        assertEquals("Frinton Road Medical Centre", subject.getName());
        assertEquals(51.8042, subject.getShape().getCoordinate().getOrdinate(1), 0.0001);
        assertEquals(1.1863, subject.getShape().getCoordinate().getOrdinate(0), 0.0001);
    }

    @Test
    public void testImportGpSurgeries() throws Exception {
        int recordsImported = importer.importDatasource("gpSurgeries");
        Subject subject = SubjectUtils.getSubjectByLabel("2915");
        assertEquals(9767, recordsImported);
        assertEquals("Blackfriars", subject.getName());
        assertEquals(53.4839, subject.getShape().getCoordinate().getOrdinate(1), 0.0001);
        assertEquals(-2.2547, subject.getShape().getCoordinate().getOrdinate(0), 0.0001);
    }

    @Test
    public void testGetDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("hospital");
        assertEquals("hospital", datasource.getId());
        assertEquals("uk.gov.data", datasource.getProvider().getLabel());
        assertEquals("Hospital", datasource.getName());
        assertEquals("List of Hospitals in England", datasource.getDescription());
        assertEquals("https://data.gov.uk/data/api/service/health/sql?query=SELECT%20*%20FROM%20hospitals%3B", datasource.getUrl());
    }

    @Test
    public void testGetAllDatasources() throws Exception {
        List<Datasource> datasources = importer.getAllDatasources();
        assertEquals(3, datasources.size());
        assertEquals("hospital", datasources.get(0).getId());
    }
}