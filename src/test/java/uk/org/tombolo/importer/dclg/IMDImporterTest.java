package uk.org.tombolo.importer.dclg;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class IMDImporterTest extends AbstractTest {
    IMDImporter imdImporter = new IMDImporter();

    Subject subject1;
    Subject subject2;

    @Before
    public void setUp() throws Exception {
        imdImporter.setDownloadUtils(makeTestDownloadUtils());

        subject1 = TestFactory.makeNamedSubject("E01000001");
        subject2 = TestFactory.makeNamedSubject("E01000002");
    }

    @Test
    public void getAllDatasources() throws Exception {
        List<Datasource> datasources = imdImporter.getAllDatasources();

        assertEquals(1, datasources.size());
        assertEquals("imd", datasources.get(0).getId());
    }

    @Test
    public void getDatasource() throws Exception {
        Datasource datasource = imdImporter.getDatasource("imd");

        assertEquals("imd", datasource.getId());
        assertEquals("uk.gov.dclg", datasource.getProvider().getLabel());
        assertEquals(53, datasource.getTimedValueAttributes().size());
    }

    @Test
    public void importDatasource() throws Exception {
        imdImporter.importDatasource("imd");
        assertEquals(2*53, imdImporter.getTimedValueCount());

        Attribute attribute1 = AttributeUtils.getByProviderAndLabel(AbstractDCLGImporter.PROVIDER, "imd.score");
        Attribute attribute2 = AttributeUtils.getByProviderAndLabel(AbstractDCLGImporter.PROVIDER, "imd.disability.rank");
        Attribute attribute3 = AttributeUtils.getByProviderAndLabel(AbstractDCLGImporter.PROVIDER, "workingAgePopulation");

        assertEquals(12.389d, TimedValueUtils.getLatestBySubjectAndAttribute(subject1, attribute1).getValue(), 0.001d);
        assertEquals(25876d, TimedValueUtils.getLatestBySubjectAndAttribute(subject1, attribute2).getValue(), 0.001d);
        assertEquals(702.75d, TimedValueUtils.getLatestBySubjectAndAttribute(subject1, attribute3).getValue(), 0.001d);

        assertEquals(7.345d, TimedValueUtils.getLatestBySubjectAndAttribute(subject2, attribute1).getValue(), 0.001d);
        assertEquals(25426d, TimedValueUtils.getLatestBySubjectAndAttribute(subject2, attribute2).getValue(), 0.001d);
        assertEquals(747.25d, TimedValueUtils.getLatestBySubjectAndAttribute(subject2, attribute3).getValue(), 0.001d);
    }

}