package uk.org.tombolo.importer.ons;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class CensusImporterTest extends AbstractTest {
    private static final String MTW_ID = "qs701ew";
    private static CensusImporter importer;

    Subject cityOfLondon01;
    Subject cityOfLondon02;
    Subject islington01;
    Subject islington02;


    @Before
    public void setup() throws IOException {
        importer = new CensusImporter(TestFactory.DEFAULT_CONFIG);
        mockDownloadUtils(importer);

        cityOfLondon01 = TestFactory.makeNamedSubject("E01000001");
        cityOfLondon02 = TestFactory.makeNamedSubject("E01000002");
        islington01 = TestFactory.makeNamedSubject("E01002766");
        islington02 = TestFactory.makeNamedSubject("E01002767");
    }

    @Test
    public void getTimedValueAttributes() throws Exception {
        List<Attribute> attributes = importer.getTimedValueAttributes(MTW_ID);

        assertEquals(13, attributes.size());
    }

    @Test
    public void getDataUrl() throws Exception {
        assertEquals("https://www.nomisweb.co.uk/api/v01/dataset/nm_568_1.bulk.csv?time=latest&measures=20100&rural_urban=total&geography=TYPE298", importer.getDataUrl(MTW_ID));
    }

    @Test
    public void importDatasource() throws Exception {
        importer.importDatasource(MTW_ID, null, null, null);
        assertEquals(0, importer.getSubjectCount());
        assertEquals(0, importer.getFixedValueCount());
        assertEquals(52, importer.getTimedValueCount());

        Attribute attribute01 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "Method of Travel to Work: All categories: Method of travel to work");

        TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon01, attribute01);
        assertEquals(1221d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon02, attribute01);
        assertEquals(1196d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington01, attribute01);
        assertEquals(1366d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington02, attribute01);
        assertEquals(1628d, timedValue.getValue(), 0.0d);

        Attribute attribute02 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "Method of Travel to Work: On foot");

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon01, attribute02);
        assertEquals(445d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon02, attribute02);
        assertEquals(428d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington01, attribute02);
        assertEquals(98d, timedValue.getValue(), 0.0d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington02, attribute02);
        assertEquals(122d, timedValue.getValue(), 0.0d);
    }

}