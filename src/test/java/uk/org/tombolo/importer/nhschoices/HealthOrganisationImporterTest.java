package uk.org.tombolo.importer.nhschoices;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Using the following test data files:
 *
 * Remote: https://data.gov.uk/data/api/service/health/sql?query=SELECT%20*%20FROM%20gp_surgeries%3B
 * Local: aHR0cHM6Ly9kYXRhLmdvdi51ay9kYXRhL2FwaS9zZXJ2aWNlL2hlYWx0aC9zcWw_cXVlcnk9U0VMRUNUJTIwKiUyMEZST00lMjBncF9zdXJnZXJpZXMlM0I=.json
 *
 * Remote: https://data.gov.uk/data/api/service/health/sql?query=SELECT%20*%20FROM%20clinics%3B
 * Local: aHR0cHM6Ly9kYXRhLmdvdi51ay9kYXRhL2FwaS9zZXJ2aWNlL2hlYWx0aC9zcWw_cXVlcnk9U0VMRUNUJTIwKiUyMEZST00lMjBjbGluaWNzJTNC.json
 *
 * Remote: https://data.gov.uk/data/api/service/health/sql?query=SELECT%20*%20FROM%20hospitals%3B
 * Local: aHR0cHM6Ly9kYXRhLmdvdi51ay9kYXRhL2FwaS9zZXJ2aWNlL2hlYWx0aC9zcWw_cXVlcnk9U0VMRUNUJTIwKiUyMEZST00lMjBob3NwaXRhbHMlM0I=
 */
public class HealthOrganisationImporterTest extends AbstractTest {
    private HealthOrganisationImporter importer;

    @Before
    public void setUp() throws Exception {
        importer = new HealthOrganisationImporter(TestFactory.DEFAULT_CONFIG);
        mockDownloadUtils(importer);
    }

    @Test
    public void testGetProvider() throws Exception {
        Provider provider = importer.getProvider();
        assertEquals("uk.nhs", provider.getLabel());
        assertEquals("NHS Choices", provider.getName());
    }

    @Test
    public void testImportHospitals() throws Exception {
        importer.importDatasource("hospital", null, null, null);
        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(importer.getProvider().getLabel(), "hospital");
        Subject subject = SubjectUtils.getSubjectByTypeAndLabel(subjectType,"40918");
        assertEquals(1106, importer.getSubjectCount());
        assertEquals("Guy's Hospital", subject.getName());
        assertEquals(51.5046, subject.getShape().getCoordinate().getOrdinate(1), 0.0001);
        // This is in the form 0.0Ex in the JSON so we test on this
        assertEquals(-0.0889, subject.getShape().getCoordinate().getOrdinate(0), 0.0001);
    }

    @Test
    public void testImportClinics() throws Exception {
        importer.importDatasource("clinic", null, null, null);
        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(importer.getProvider().getLabel(), "clinic");
        Subject subject = SubjectUtils.getSubjectByTypeAndLabel(subjectType, "12366");
        assertEquals(8416, importer.getSubjectCount());
        assertEquals("Frinton Road Medical Centre", subject.getName());
        assertEquals(51.8042, subject.getShape().getCoordinate().getOrdinate(1), 0.0001);
        assertEquals(1.1863, subject.getShape().getCoordinate().getOrdinate(0), 0.0001);
    }

    @Test
    public void testImportGpSurgeries() throws Exception {
        importer.importDatasource("gpSurgeries", null, null, null);
        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(importer.getProvider().getLabel(), "gpSurgeries");
        Subject subject = SubjectUtils.getSubjectByTypeAndLabel(subjectType, "2915");
        assertEquals(9767, importer.getSubjectCount());
        assertEquals("Blackfriars", subject.getName());
        assertEquals(53.4839, subject.getShape().getCoordinate().getOrdinate(1), 0.0001);
        assertEquals(-2.2547, subject.getShape().getCoordinate().getOrdinate(0), 0.0001);
    }

    @Test
    public void testGetDatasource() throws Exception {
        DatasourceSpec datasourceSpec = importer.getDatasource("hospital").getDatasourceSpec();
        assertEquals("hospital", datasourceSpec.getId());
        assertEquals("uk.nhs", importer.getProvider().getLabel());
        assertEquals("Hospital", datasourceSpec.getName());
        assertEquals("List of Hospitals in England", datasourceSpec.getDescription());
        assertEquals("https://data.gov.uk/data/api/service/health/sql?query=SELECT%20*%20FROM%20hospitals%3B", datasourceSpec.getUrl());
    }

    @Test
    public void testGetDatasourceIds() throws Exception {
        List<String> datasources = importer.getDatasourceIds();
        assertEquals(Arrays.asList("hospital", "clinic", "gpSurgeries"), datasources);
    }
}