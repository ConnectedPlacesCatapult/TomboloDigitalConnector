package uk.org.tombolo.importer.ons;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.Importer;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ONSWagesImporterTest extends AbstractTest {
    Subject cityOfLondon;
    Subject islington;
    Subject leeds;

    public Importer importer;

    @Before
    public void before() throws Exception {
        importer = new ONSWagesImporter();
        mockDownloadUtils(importer);
        cityOfLondon = TestFactory.makeNamedSubject("E09000001");
        islington = TestFactory.makeNamedSubject("E09000019");
        leeds = TestFactory.makeNamedSubject("E08000035");
    }

    @Test
    public void getAllDatasources() throws Exception {
        List<Datasource> datasourceList = importer.getAllDatasources();

        assertEquals(1, datasourceList.size());
    }

    @Test
    public void getDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("wages");

        assertEquals(198, datasource.getTimedValueAttributes().size());
    }

    @Test
    public void importDatasource() throws Exception {
        importer.importDatasource("wages", null, null);

        Attribute weeklyPayGrossAllMean = AttributeUtils.getByProviderAndLabel(
                importer.getProvider(),
                "asheTable81aWeeklyPayGrossAllMean");
        Attribute weeklyPayGrossAllMedian = AttributeUtils.getByProviderAndLabel(
                importer.getProvider(),
                "asheTable81aWeeklyPayGrossAllMedian");
        Attribute weeklyPayGrossFemalePartTimeMean = AttributeUtils.getByProviderAndLabel(
                importer.getProvider(),
                "asheTable81aWeeklyPayGrossFemale-Part-TimeMean");
        Attribute annualPayIncentiveFullTimeMean = AttributeUtils.getByProviderAndLabel(
                importer.getProvider(),
                "asheTable88aAnnualPayIncentiveFull-TimeMean");

        TimedValue timedValue;

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon, weeklyPayGrossAllMean);
        assertEquals(1038.9d, timedValue.getValue(), 0.1d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon, weeklyPayGrossAllMedian);
        assertNull(timedValue);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington, weeklyPayGrossFemalePartTimeMean);
        assertEquals(244.0d, timedValue.getValue(), 0.1d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(leeds, annualPayIncentiveFullTimeMean);
        assertEquals(1133d, timedValue.getValue(), 0.1d);

        timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon, annualPayIncentiveFullTimeMean);
        assertNull(timedValue);
    }

}