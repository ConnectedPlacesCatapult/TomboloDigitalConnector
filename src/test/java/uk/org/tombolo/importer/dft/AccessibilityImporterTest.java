package uk.org.tombolo.importer.dft;

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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * Using the following test data files:
 *
 * Remote: https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357458/acs0501.xls
 * Local: a3faf937-48bb-36b4-8958-dd9a709fc9d3.xls
 *
 * Remote: https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357460/acs0502.xls
 * Local: 76b20598-45e0-3a6f-a9e1-3f73aaff0074.xls
 *
 * Remote: https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357461/acs0503.xls
 * Local: c41f2008-202b-38a1-adf1-0524cc4ae41c.xls
 *
 * Remote: https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357464/acs0504.xls
 * Local: b35edb6f-be66-35cd-89e4-6b37681334cb.xls
 *
 * Remote: https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357467/acs0505.xls
 * Local: 7616e60e-b597-3fa6-92eb-6c77c72184bf.xls
 *
 * Remote: https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357468/acs0506.xls
 * Local: aHR0cHM6Ly93d3cuZ292LnVrL2dvdmVybm1lbnQvdXBsb2Fkcy9zeXN0ZW0vdXBsb2Fkcy9hdHRhY2htZW50X2RhdGEvZmlsZS8zNTc0NjgvYWNzMDUwNi54bHM=.xls
 *
 * Remote: https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357469/acs0507.xls
 * Local: ad79ebbb3-339b-34cd-807b-00350da67033.xls
 *
 * Remote: https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357467/acs0508.xls
 * Local: 767f0676-d56b-3d2f-ab29-754898185b8e.xls
 *
 */
public class AccessibilityImporterTest extends AbstractTest {
    AccessibilityImporter importer;
    private Subject cityofLondon001A;
    private Subject cityofLondon001B;
    private Subject islington015E;

    //Create this private class to avoid importing the oa data that slows down the test
    private class ImporterTest extends AccessibilityImporter {
        @Override
        protected List<String> getOaDatasourceIds() {
            return Collections.emptyList();
        }
    }

    @Before
    public void setUp() throws Exception {
        importer = new ImporterTest();
        importer.setDownloadUtils(makeTestDownloadUtils());
        cityofLondon001A = TestFactory.makeNamedSubject("E01000001");
        cityofLondon001B = TestFactory.makeNamedSubject("E01000002");
        islington015E = TestFactory.makeNamedSubject("E01002766");
    }

    @Test
    public void getAllDatasources() throws Exception {
        List<String> datasources = importer.getDatasourceIds();

        assertEquals(7, datasources.size());

        assertEquals(Arrays.asList(
                "acs0501", "acs0502", "acs0503", "acs0504", "acs0505", "acs0506", "acs0507"),
                datasources);
    }

    @Test
    public void getDataSourceAttributeCount() throws Exception {
        Datasource asc0501 = importer.getDatasource("acs0501");
        assertEquals("acs0501", asc0501.getDatasourceSpec().getId());
        assertEquals(103, asc0501.getTimedValueAttributes().size());

        Datasource asc0502 = importer.getDatasource("acs0502");
        assertEquals("acs0502", asc0502.getDatasourceSpec().getId());
        assertEquals(67, asc0502.getTimedValueAttributes().size());

        Datasource asc0503 = importer.getDatasource("acs0503");
        assertEquals("acs0503", asc0503.getDatasourceSpec().getId());
        assertEquals(79, asc0503.getTimedValueAttributes().size());

        Datasource asc0504 = importer.getDatasource("acs0504");
        assertEquals("acs0504", asc0504.getDatasourceSpec().getId());
        assertEquals(48, asc0504.getTimedValueAttributes().size());

        Datasource asc0505 = importer.getDatasource("acs0505");
        assertEquals("acs0505", asc0505.getDatasourceSpec().getId());
        assertEquals(67, asc0505.getTimedValueAttributes().size());

        Datasource asc0506 = importer.getDatasource("acs0506");
        assertEquals("acs0506", asc0506.getDatasourceSpec().getId());
        assertEquals(67, asc0506.getTimedValueAttributes().size());

        Datasource asc0507 = importer.getDatasource("acs0507");
        assertEquals("acs0507", asc0507.getDatasourceSpec().getId());
        assertEquals(79, asc0507.getTimedValueAttributes().size());

    }

    @Test
    public void getDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("acs0501");

        assertEquals("acs0501", datasource.getDatasourceSpec().getId());
        assertEquals("Employment centres", datasource.getDatasourceSpec().getName());
        assertEquals("Travel time, destination and origin indicators to Employment centres by mode of travel", datasource.getDatasourceSpec().getDescription());
        assertEquals(103, datasource.getTimedValueAttributes().size());

        // Testing attribute nr 27
        // 500emplcar40	EMPLO032	Number of employment centres with at least 500 jobs available by car within 40 minutes	Number between 0 and 10
        Attribute attribute = datasource.getTimedValueAttributes().get(26);
        assertEquals("EMPLO032", attribute.getLabel());
        assertEquals("Number of employment centres with at least 500 jobs available by car within 40 minutes", attribute.getDescription());
    }

    @Test
    public void importDatasource() throws Exception {
        importer.importDatasource("acs0501", null, null, null);

        Attribute emplo032 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "EMPLO032");
        Attribute emplo070 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "EMPLO070");
        Attribute emplo108 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "EMPLO108");

        // This value only exist up to 2010
        TimedValue tvLonA032 = TimedValueUtils.getLatestBySubjectAndAttribute(cityofLondon001B, emplo032);
        assertEquals(10d, tvLonA032.getValue(), 0.1d);
        assertEquals(LocalDateTime.of(2010, 12, 31, 23, 59, 59),tvLonA032.getId().getTimestamp());

        List<TimedValue> tvLonA32s = TimedValueUtils.getBySubjectAndAttribute(cityofLondon001B, emplo032);
        assertEquals(4, tvLonA32s.size());

        // This value is available for all years
        TimedValue tvLonA070 = TimedValueUtils.getLatestBySubjectAndAttribute(cityofLondon001B, emplo070);
        assertEquals(14d, tvLonA070.getValue(), 0.1d);
        assertEquals(LocalDateTime.of(2013, 12, 31, 23, 59, 59),tvLonA070.getId().getTimestamp());

        List<TimedValue> tvLonA070s = TimedValueUtils.getBySubjectAndAttribute(cityofLondon001B, emplo070);
        assertEquals(7, tvLonA070s.size());

        TimedValue tvIslA070 = TimedValueUtils.getLatestBySubjectAndAttribute(islington015E, emplo070);
        assertEquals(90d, tvIslA070.getValue(), 0.1d);
        assertEquals(LocalDateTime.of(2013, 12, 31, 23, 59, 59),tvIslA070.getId().getTimestamp());

        List<TimedValue> tvIslA070s = TimedValueUtils.getBySubjectAndAttribute(islington015E, emplo070);
        assertEquals(7, tvIslA070s.size());

        // This has some values missing for City London
        TimedValue tvLonA108 = TimedValueUtils.getLatestBySubjectAndAttribute(cityofLondon001B, emplo108);
        assertEquals(89.3d, tvLonA108.getValue(), 0.1d);
        assertEquals(LocalDateTime.of(2013, 12, 31, 23, 59, 59),tvLonA108.getId().getTimestamp());

        List<TimedValue> tvLonA108s = TimedValueUtils.getBySubjectAndAttribute(cityofLondon001B, emplo108);
        assertEquals(5, tvLonA108s.size());

        TimedValue tvIslA108 = TimedValueUtils.getLatestBySubjectAndAttribute(islington015E, emplo108);
        assertEquals(88.8d, tvIslA108.getValue(), 0.1d);
        assertEquals(LocalDateTime.of(2013, 12, 31, 23, 59, 59),tvIslA108.getId().getTimestamp());

        List<TimedValue> tvIslA108s = TimedValueUtils.getBySubjectAndAttribute(islington015E, emplo108);
        assertEquals(6, tvIslA108s.size());


        // The overall expected value count
        int expectedValueCount =
                (3 * 7 * 103)       // Subjects * Years * Attributes
                        - (22*3)    // 2007 Nulls
                        - (22*3)    // 2008 Nulls
                        - (22*3)    // 2009 Nulls
                        - (0*3)     // 2010 Nulls
                        - (12*2)    // 2011 Nulls
                        - (22*3)    // 2011 Missing columns
                        - (34*3)    // 2012 Nulls
                        - (22*3)    // 2012 Missing columns
                        - (0*3)     // 2013 Nulls
                        - (22*3)    // 2013 Missing columns
                        - 3         // FIXME: Find out why these are missing
                ;

        assertEquals(expectedValueCount, importer.getTimedValueCount());
    }

}