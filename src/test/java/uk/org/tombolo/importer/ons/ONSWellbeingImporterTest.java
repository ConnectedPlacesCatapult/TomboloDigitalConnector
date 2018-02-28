package uk.org.tombolo.importer.ons;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Using fa2b0217-a324-3969-9f1d-19c246472d65.xls
 */
public class ONSWellbeingImporterTest  extends AbstractTest {
    private static uk.org.tombolo.importer.ons.ONSWellbeingImporter importer;
    Subject islington;
    Subject northEast;

    @Before
    public void before(){
        importer = new ONSWellbeingImporter();
        islington = TestFactory.makeNamedSubject("E09000019");
        northEast = TestFactory.makeNamedSubject("E12000001");
        mockDownloadUtils(importer);

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
        Datasource datasource = importer.getDatasource("ONSWellbeing");
        assertEquals("https://www.ons.gov.uk/file?uri=/peoplepopulationandcommunity/wellbeing/datasets/headlineestimatesofpersonalwellbeing/april2016tomarch2017localauthorityupdate/headlinewellbeinglocalauthorityupdate2016to2017.xls", datasource.getDatasourceSpec().getUrl());
    }

    @Test
    public void testImportDatasource() throws Exception {
        importer.importDatasource("ONSWellbeing", null, null, null);
        testTimedValue(islington, "lifeSatisfactionMeanRatings", 7.38);
        testTimedValue(islington, "worthwhileMeanRatings", 7.44);
        testTimedValue(islington, "happinessMeanRatings", 7.31);
        testTimedValue(islington, "anxietyMeanRatings", 3.26);

        testTimedValue(northEast, "lifeSatisfactionMeanRatings", 7.61);
        testTimedValue(northEast, "worthwhileMeanRatings", 7.79);
        testTimedValue(northEast, "happinessMeanRatings", 7.43);
        testTimedValue(northEast, "anxietyMeanRatings", 2.84);


    }

    private void testTimedValue(Subject subject, String attributeLabel, Double value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        TimedValue val = TimedValueUtils.getLatestBySubjectAndAttribute(subject, attribute);
        assertEquals("Value for key (" + subject.getLabel() + "," + attributeLabel + ")", value, val.getValue());
    }
}
