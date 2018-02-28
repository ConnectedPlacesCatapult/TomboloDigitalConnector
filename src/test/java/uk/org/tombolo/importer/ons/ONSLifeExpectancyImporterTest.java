package uk.org.tombolo.importer.ons;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import static org.junit.Assert.assertEquals;

/**
 * Using ad859819-b56d-3777-8d15-136a1e6c547e.xls
 */
public class ONSLifeExpectancyImporterTest extends AbstractTest {
    Subject barking;
    Subject northEast;

    private static ONSLifeExpectancyImporter importer;

    @Before
    public void before(){
        importer = new ONSLifeExpectancyImporter();
        barking = TestFactory.makeNamedSubject("E09000002");
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
    public void testGetDatasourcIds() throws Exception {
        Datasource datasourceCycle = importer.getDatasource("ONSLifeExpectancy");
        assertEquals(12, datasourceCycle.getTimedValueAttributes().size());
    }

    @Test
    public void testGetDatasource() throws Exception {
        Datasource datasourceCycle = importer.getDatasource("ONSLifeExpectancy");
        assertEquals("https://www.ons.gov.uk/file?uri=/peoplepopulationandcommunity/healthandsocialcare/healthandlifeexpectancies/datasets/healthstatelifeexpectancyatbirthandatage65bylocalareasuk/current/refhealthstatelifeexpectancies1.xls", datasourceCycle.getDatasourceSpec().getUrl());
    }
    @Test
    public void testImporter() throws Exception {
        importer.importDatasource("ONSLifeExpectancy", null, null, null);
        testTimedValue(barking, "LEmaleAtBirth", 77.49818);
        testTimedValue(barking, "HLEmaleAtBirth", 58.15019);
        testTimedValue(barking, "DfLEmaleAtBirth", 61.04276);
        testTimedValue(barking, "LEfemaleAtBirth", 81.88093);
        testTimedValue(barking, "HLEfemaleAtBirth", 60.67723);
        testTimedValue(barking, "DfLEfemaleAtBirth", 61.14392);
        testTimedValue(barking, "LEmaleAt65", 17.35316);
        testTimedValue(barking, "HLEmaleAt65", 6.62973);
        testTimedValue(barking, "DfLEmaleAt65", 8.35514);
        testTimedValue(barking, "LEfemaleAt65", 20.45161);
        testTimedValue(barking, "HLEfemaleAt65", 8.82327);
        testTimedValue(barking, "DfLEfemaleAt65", 8.83367);

        testTimedValue(northEast, "LEmaleAtBirth", 77.80904);
        testTimedValue(northEast, "HLEmaleAtBirth", 59.66996);
        testTimedValue(northEast, "DfLEmaleAtBirth", 59.68109);
        testTimedValue(northEast, "LEfemaleAtBirth", 81.53141);
        testTimedValue(northEast, "HLEfemaleAtBirth", 60.59868);
        testTimedValue(northEast, "DfLEfemaleAtBirth", 59.69828);
        testTimedValue(northEast, "LEmaleAt65", 17.77438);
        testTimedValue(northEast, "HLEmaleAt65", 8.7994);
        testTimedValue(northEast, "DfLEmaleAt65", 8.56747);
        testTimedValue(northEast, "LEfemaleAt65", 19.97941);
        testTimedValue(northEast, "HLEfemaleAt65", 9.84445);
        testTimedValue(northEast, "DfLEfemaleAt65", 9.24159);
    }
    private void testTimedValue(Subject subject, String attributeLabel, Double value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        TimedValue val = TimedValueUtils.getLatestBySubjectAndAttribute(subject, attribute);
        assertEquals(value, val.getValue(),0.00001);
    }
}
