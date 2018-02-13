package uk.org.tombolo.importer.ons;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Using the following test data files:
 *
 * lsoas 0220da63-778d-3712-a4bf-eafa8e5ab7ab.json
 * msoas 8812b68c-a1e4-3583-8a4b-3fcbae0d3ed7.json
 * las 6081d478-b3b1-3764-aa95-00ec0c92d213.json
 */
public class OaImporterTest extends AbstractTest {
    OaImporter importer;

    @Before
    public void setUp() throws Exception {
        importer = new OaImporter();
        importer.setDownloadUtils(makeTestDownloadUtils());
    }

    @Test
    public void testGetDatasourceIds() throws Exception {
        List<String> datasources = importer.getDatasourceIds();
        assertEquals(Arrays.asList("lsoa", "msoa", "localAuthority"), datasources);
    }

    @Test
    public void testGetDatasourceLSOA() throws Exception {
        Datasource datasource = importer.getDatasource("lsoa");
        assertEquals("lsoa", datasource.getDatasourceSpec().getId());
        assertEquals("uk.gov.ons", importer.getProvider().getLabel());
        assertEquals("LSOA", datasource.getDatasourceSpec().getName());
        assertEquals("Lower Layer Super Output Areas", datasource.getDatasourceSpec().getDescription());
    }

    @Test
    public void testGetDatasourceMSOA() throws Exception {
        Datasource datasource = importer.getDatasource("msoa");
        assertEquals("msoa", datasource.getDatasourceSpec().getId());
        assertEquals("uk.gov.ons", importer.getProvider().getLabel());
        assertEquals("MSOA", datasource.getDatasourceSpec().getName());
        assertEquals("Middle Layer Super Output Areas", datasource.getDatasourceSpec().getDescription());
    }

    @Test
    public void testImportLsoas() throws Exception {
        importer.importDatasource("lsoa", null, null, null);
        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(importer.getProvider().getLabel(), "lsoa");
        Subject lsoa = SubjectUtils.getSubjectByTypeAndLabel(subjectType, "E01000002");

        assertEquals("City of London 001B", lsoa.getName());
        assertEquals("lsoa", lsoa.getSubjectType().getLabel());
        assertEquals(-0.09252710274629854, lsoa.getShape().getCentroid().getX(), 0.1E-6);
        assertEquals(51.51821627457435, lsoa.getShape().getCentroid().getY(), 0.1E-6);
        assertEquals(100, importer.getSubjectCount());
    }

    @Test
    public void testImportMsoas() throws Exception {
        importer.importDatasource("msoa", null, null, null);
        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(importer.getProvider().getLabel(), "msoa");
        Subject lsoa = SubjectUtils.getSubjectByTypeAndLabel(subjectType,"E02000093");

        assertEquals("Brent 001", lsoa.getName());
        assertEquals("msoa", lsoa.getSubjectType().getLabel());
        assertEquals(-0.2746307279027593, lsoa.getShape().getCentroid().getX(), 0.1E-6);
        assertEquals(51.59338282612998, lsoa.getShape().getCentroid().getY(), 0.1E-6);
        assertEquals(100, importer.getSubjectCount());
    }

    @Test
    public void testImportLocalAuthorities() throws Exception {
        importer.importDatasource("localAuthority", null, null, null);
        SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(importer.getProvider().getLabel(), "localAuthority");
        Subject localAuthority = SubjectUtils.getSubjectByTypeAndLabel(subjectType,"E06000001");

        assertEquals("Hartlepool", localAuthority.getName());
        assertEquals("localAuthority", localAuthority.getSubjectType().getLabel());
        assertEquals(-1.2592784934731256, localAuthority.getShape().getCentroid().getX(), 0.1E-6);
        assertEquals(54.66957856523336, localAuthority.getShape().getCentroid().getY(), 0.1E-6);
        assertEquals(7, importer.getSubjectCount());
    }
}