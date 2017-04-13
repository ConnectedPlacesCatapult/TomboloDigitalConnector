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
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.Importer;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AccessibilityImporterTest extends AbstractTest {
    Importer importer;

    @Before
    public void setUp() throws Exception {
        importer = new AccessibilityImporter();
        importer.setDownloadUtils(makeTestDownloadUtils());

        TestFactory.makeNamedSubject(TestFactory.DEFAULT_PROVIDER, "E01000001");
        TestFactory.makeNamedSubject(TestFactory.DEFAULT_PROVIDER, "E01000002");
        TestFactory.makeNamedSubject(TestFactory.DEFAULT_PROVIDER, "E01002766");
    }

    @Test
    public void getAllDatasources() throws Exception {
        List<Datasource> datasources = importer.getAllDatasources();

        assertEquals(8, datasources.size());

        assertEquals("acs0501", datasources.get(0).getId());
        assertEquals(103, datasources.get(0).getTimedValueAttributes().size());

        assertEquals("acs0502", datasources.get(1).getId());
        assertEquals(67, datasources.get(1).getTimedValueAttributes().size());

        assertEquals("acs0503", datasources.get(2).getId());
        assertEquals(79, datasources.get(2).getTimedValueAttributes().size());

        assertEquals("acs0504", datasources.get(3).getId());
        assertEquals(48, datasources.get(3).getTimedValueAttributes().size());

        assertEquals("acs0505", datasources.get(4).getId());
        assertEquals(67, datasources.get(4).getTimedValueAttributes().size());

        assertEquals("acs0506", datasources.get(5).getId());
        assertEquals(67, datasources.get(5).getTimedValueAttributes().size());

        assertEquals("acs0507", datasources.get(6).getId());
        assertEquals(79, datasources.get(6).getTimedValueAttributes().size());

        assertEquals("acs0508", datasources.get(7).getId());
        assertEquals(79, datasources.get(7).getTimedValueAttributes().size());

    }

    @Test
    public void getDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("acs0501");

        assertEquals("acs0501", datasource.getId());
        assertEquals("acs0501", datasource.getName());
        assertEquals("Travel time, destination and origin indicators to Employment centres by mode of travel", datasource.getDescription());
        assertEquals(103, datasource.getTimedValueAttributes().size());

        // Testing attribute nr 27
        // 500emplcar40	EMPLO032	Number of employment centres with at least 500 jobs available by car within 40 minutes	Number between 0 and 10
        Attribute attribute = datasource.getTimedValueAttributes().get(26);
        assertEquals("500emplcar40", attribute.getName());
        assertEquals("EMPLO032", attribute.getLabel());
        assertEquals("Number of employment centres with at least 500 jobs available by car within 40 minutes", attribute.getDescription());
    }

    @Test
    public void importDatasource() throws Exception {
        int valueCount = importer.importDatasource("acs0501");

        Subject cityOfLondon = SubjectUtils.getSubjectByLabel("E01000002");
        Subject islington = SubjectUtils.getSubjectByLabel("E01002766");
        Attribute emplo032 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "EMPLO032");
        Attribute emplo070 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "EMPLO070");
        Attribute emplo108 = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "EMPLO108");

        // This value only exist up to 2010
        TimedValue tvLonA032 = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon, emplo032);
        assertEquals(10d, tvLonA032.getValue(), 0.1d);
        assertEquals(LocalDateTime.of(2010, 12, 31, 23, 59, 59),tvLonA032.getId().getTimestamp());

        List<TimedValue> tvLonA32s = TimedValueUtils.getBySubjectAndAttribute(cityOfLondon, emplo032);
        assertEquals(4, tvLonA32s.size());

        // This value is available for all years
        TimedValue tvLonA070 = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon, emplo070);
        assertEquals(14d, tvLonA070.getValue(), 0.1d);
        assertEquals(LocalDateTime.of(2013, 12, 31, 23, 59, 59),tvLonA070.getId().getTimestamp());

        List<TimedValue> tvLonA070s = TimedValueUtils.getBySubjectAndAttribute(cityOfLondon, emplo070);
        assertEquals(7, tvLonA070s.size());

        TimedValue tvIslA070 = TimedValueUtils.getLatestBySubjectAndAttribute(islington, emplo070);
        assertEquals(90d, tvIslA070.getValue(), 0.1d);
        assertEquals(LocalDateTime.of(2013, 12, 31, 23, 59, 59),tvIslA070.getId().getTimestamp());

        List<TimedValue> tvIslA070s = TimedValueUtils.getBySubjectAndAttribute(islington, emplo070);
        assertEquals(7, tvIslA070s.size());

        // This has some values missing for City London
        TimedValue tvLonA108 = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon, emplo108);
        assertEquals(89.3d, tvLonA108.getValue(), 0.1d);
        assertEquals(LocalDateTime.of(2013, 12, 31, 23, 59, 59),tvLonA108.getId().getTimestamp());

        List<TimedValue> tvLonA108s = TimedValueUtils.getBySubjectAndAttribute(cityOfLondon, emplo108);
        assertEquals(5, tvLonA108s.size());

        TimedValue tvIslA108 = TimedValueUtils.getLatestBySubjectAndAttribute(islington, emplo108);
        assertEquals(88.8d, tvIslA108.getValue(), 0.1d);
        assertEquals(LocalDateTime.of(2013, 12, 31, 23, 59, 59),tvIslA108.getId().getTimestamp());

        List<TimedValue> tvIslA108s = TimedValueUtils.getBySubjectAndAttribute(islington, emplo108);
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

        assertEquals(expectedValueCount, valueCount);
    }

}