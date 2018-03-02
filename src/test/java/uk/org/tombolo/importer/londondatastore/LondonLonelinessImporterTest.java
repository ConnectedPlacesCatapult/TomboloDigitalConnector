package uk.org.tombolo.importer.londondatastore;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Using test file f0637521-9d4f-3edc-96e7-eabf10a96580.xlsx
 */
public class LondonLonelinessImporterTest extends AbstractLondonDatastoreTestUtil {
    LondonLonelinessImporter importer = new LondonLonelinessImporter();
    Subject barking;
    Subject cityOfLondon001;
    Subject cityOfLondon001A;

    @Before
    public void setUp() throws Exception {
        barking = TestFactory.makeNamedSubject("E09000002");
        cityOfLondon001 = TestFactory.makeNamedSubject("E02000001");
        cityOfLondon001A = TestFactory.makeNamedSubject("E01000001");
        mockDownloadUtils(importer);
    }
    @Test
    public void getDatasourceIds() throws Exception {
        List<String> datasourceList = importer.getDatasourceIds();
        assertEquals(1, datasourceList.size());
        assertEquals(Arrays.asList("lonelinessPrevalence"), datasourceList);
    }

    @Test
    public void getDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("lonelinessPrevalence");
        assertEquals("lonelinessPrevalence", datasource.getDatasourceSpec().getId());
        assertEquals(5, datasource.getTimedValueAttributes().size());
    }
    @Test
    public void importDatasource() throws Exception {
        List<String> geographyScope = new ArrayList<>();
        geographyScope.addAll(Arrays.asList("localAuthority","msoa","lsoa"));
        importer.importDatasource("lonelinessPrevalence",geographyScope, null, null);

        testTimedValue(barking, "logOdds", -3.54169908800868);
        testTimedValue(barking, "prevalence", 2.81487697276594);
        testTimedValue(barking, "rankInLondon", 4.0);
        testTimedValue(barking, "rankInEngland", 5.0);

        testTimedValue(cityOfLondon001, "logOdds", -3.99447405797102);
        testTimedValue(cityOfLondon001, "prevalence", 1.80840734709814);
        testTimedValue(cityOfLondon001, "rankInLondon", 742.0);
        testTimedValue(cityOfLondon001, "rankInEngland", 3822.0);
        testTimedValue(cityOfLondon001, "londonDecile", 8.0);

        testTimedValue(cityOfLondon001A, "logOdds", -4.463402162);
        testTimedValue(cityOfLondon001A, "rankInLondon", 4752.0);
        testTimedValue(cityOfLondon001A, "rankInEngland", 31310.0);
        testTimedValue(cityOfLondon001A, "londonDecile", 10.0);

    }
    private void testTimedValue(Subject subject, String attributeLabel, Double value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        TimedValue val = TimedValueUtils.getLatestBySubjectAndAttribute(subject, attribute);
        assertEquals(value, val.getValue(),0.00001);
    }
}
