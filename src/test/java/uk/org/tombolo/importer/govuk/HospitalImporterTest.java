package uk.org.tombolo.importer.govuk;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import static org.junit.Assert.*;

public class HospitalImporterTest extends AbstractTest {
    HospitalImporter importer;

    @Before
    public void setUp() throws Exception {
        importer = new HospitalImporter();
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
    public void testImportDatasource() throws Exception {
        int recordsImported = importer.importDatasource("hospital");
        Subject subject = SubjectUtils.getSubjectByLabel("40887");
        assertEquals(1106, recordsImported);
        assertEquals("West Berkshire Community Hospital", subject.getName());
    }

    @Test
    public void testGetDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("hospital");
        assertEquals("hospital", datasource.getId());
        assertEquals("uk.gov.data", datasource.getProvider().getLabel());
        assertEquals("Hospital", datasource.getName());
        assertEquals("List of Hospitals in England", datasource.getDescription());
        assertEquals("https://data.gov.uk/data/api/service/health/hospitals/all_hospitals", datasource.getUrl());
    }
}