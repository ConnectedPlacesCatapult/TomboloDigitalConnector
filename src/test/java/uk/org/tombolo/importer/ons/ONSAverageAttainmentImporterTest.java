package uk.org.tombolo.importer.ons;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.ons.ONSAverageAttainmentImporter;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ONSAverageAttainmentImporterTest extends AbstractTest {
    private static uk.org.tombolo.importer.ons.ONSAverageAttainmentImporter importer;
    Subject barking;
    Subject islington;

    @Before
    public void before(){
        importer = new ONSAverageAttainmentImporter(TestFactory.DEFAULT_CONFIG);
        mockDownloadUtils(importer);
        barking = TestFactory.makeNamedSubject("E09000002");
        islington = TestFactory.makeNamedSubject("E09000019");
    }

    @Test
    public void testGetProvider(){
        Provider provider = importer.getProvider();
        assertEquals("uk.gov.ons", provider.getLabel());
        assertEquals("Office for National Statistics", provider.getName());
    }

    @Test
    public void testGetDatasourceIds() throws Exception {
        List<String> datasources = importer.getDatasourceIds();
        assertEquals(1,datasources.size());
    }

    @Test
    public void testGetDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("ONSAverageAttainment");
        assertEquals("https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/652293/SFR57_2017_LA__tables.xlsx", datasource.getDatasourceSpec().getUrl());
    }

    @Test
    public void testImportDatasource() throws Exception {
        importer.importDatasource("ONSAverageAttainment", null, null, null);
        System.out.println(importer.getTimedValueCount());
        testTimedValue(barking, "average_attainment", 46.2);

    }

    private void testTimedValue(Subject subject, String attributeLabel, Double value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        TimedValue val = TimedValueUtils.getLatestBySubjectAndAttribute(subject, attribute);
        assertEquals("Value for key (" + subject.getLabel() + "," + attributeLabel + ")", value, val.getValue());
    }

}
