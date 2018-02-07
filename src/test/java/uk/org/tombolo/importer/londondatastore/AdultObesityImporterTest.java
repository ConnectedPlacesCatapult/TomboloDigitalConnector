package uk.org.tombolo.importer.londondatastore;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.DatasourceSpec;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * This test is using data from 0bf8b2be-3dca-3512-8f8d-48bdb704eb3c.xls
 */
public class AdultObesityImporterTest extends AbstractLondonDatastoreTestUtil {
    static final private String DATASOURCE_ID = "adultObesity";
    private AdultObesityImporter importer = new AdultObesityImporter();

    @Before
    public void setUp() throws Exception {
        mockDownloadUtils(importer);
    }


    @Test
    public void getDatasourceSpec() throws Exception {
        DatasourceSpec datasourceSpec = importer.getDatasourceSpec(DATASOURCE_ID);
        assertEquals(AdultObesityImporter.class, datasourceSpec.getImporterClass());
        assertEquals("Local Authority Adult Obesity", datasourceSpec.getName());
        assertEquals("Self reported adult obesity", datasourceSpec.getDescription());
        assertEquals("https://data.london.gov.uk/dataset/obesity-adults", datasourceSpec.getUrl());
    }

    @Test
    public void importDatasource() throws Exception {
        importer.importDatasource(DATASOURCE_ID, null, null, null);

        assertEquals(0, importer.getSubjectCount());
        assertEquals(0, importer.getFixedValueCount());
        assertEquals(10, importer.getTimedValueCount());

        TimedValue fractionUnderweight = TimedValueUtils.getLatestBySubjectAndAttribute(
                cityOfLondon,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "fractionUnderweight")
        );
        assertEquals(TimedValueUtils.parseTimestampString("2013"), fractionUnderweight.getId().getTimestamp());
        assertEquals(0.015d, fractionUnderweight.getValue(), 0.01d);

        TimedValue fractionHealthyWeight = TimedValueUtils.getLatestBySubjectAndAttribute(
                islington,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "fractionHealthyWeight")
        );
        assertEquals(TimedValueUtils.parseTimestampString("2013"), fractionHealthyWeight.getId().getTimestamp());
        assertEquals(0.44d, fractionHealthyWeight.getValue(), 0.01d);

        TimedValue fractionOverweight = TimedValueUtils.getLatestBySubjectAndAttribute(
                cityOfLondon,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "fractionOverweight")
        );
        assertEquals(TimedValueUtils.parseTimestampString("2013"), fractionOverweight.getId().getTimestamp());
        assertEquals(0.30d, fractionOverweight.getValue(), 0.01d);

        TimedValue fractionObese = TimedValueUtils.getLatestBySubjectAndAttribute(
                islington,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "fractionObese")
        );
        assertEquals(TimedValueUtils.parseTimestampString("2013"), fractionObese.getId().getTimestamp());
        assertEquals(0.20, fractionObese.getValue(), 0.01d);

        TimedValue fractionExcessWeight = TimedValueUtils.getLatestBySubjectAndAttribute(
                cityOfLondon,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "fractionExcessWeight")
        );
        assertEquals(TimedValueUtils.parseTimestampString("2013"), fractionExcessWeight.getId().getTimestamp());
        assertEquals(0.61d, fractionExcessWeight.getValue(), 0.01d);

    }

    @Test
    public void getTimedValueAttributes() throws Exception {
        List<Attribute> attributes = importer.getTimedValueAttributes(DATASOURCE_ID);
        assertEquals(5, attributes.size());
    }

}