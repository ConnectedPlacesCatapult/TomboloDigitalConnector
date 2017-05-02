package uk.org.tombolo.importer.londondatastore;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *  Using the following test data files:
 *
 *  Local: aHR0cHM6Ly9maWxlcy5kYXRhcHJlc3MuY29tL2xvbmRvbi9kYXRhc2V0L2xvbmRvbi1ib3JvdWdoLXByb2ZpbGVzLzIwMTUtMDktMjRUMTU6NDk6NTIvbG9uZG9uLWJvcm91Z2gtcHJvZmlsZXMuY3N2.csv
 */
public class LondonBoroughProfileImporterTest extends AbstractTest {
    LondonBoroughProfileImporter importer = new LondonBoroughProfileImporter(TestFactory.DEFAULT_CONFIG);

    Subject cityOfLondon;
    Subject islington;

    @Before
    public void setUp() throws Exception {
        mockDownloadUtils(importer);
        cityOfLondon = TestFactory.makeNamedSubject(TestFactory.DEFAULT_PROVIDER, "E09000001");
        islington = TestFactory.makeNamedSubject(TestFactory.DEFAULT_PROVIDER, "E09000019");
    }

    @Test
    public void getDatasourceIds() throws Exception {
        List<String> datasourceList = importer.getDatasourceIds();
        assertEquals(1, datasourceList.size());
        assertEquals(Arrays.asList("londonBoroughProfiles"), datasourceList);
    }

    @Test
    public void getDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("londonBoroughProfiles");

        assertEquals("londonBoroughProfiles", datasource.getId());
        assertEquals("London Borough Profiles", datasource.getName());
        assertEquals(6, datasource.getTimedValueAttributes().size());
    }

    @Test
    public void importDatasource() throws Exception {
        importer.importDatasource("londonBoroughProfiles");
        assertEquals(12, importer.getTimedValueCount());

        TimedValue populationDensity = TimedValueUtils.getLatestBySubjectAndAttribute(
                cityOfLondon,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "populationDensity")
        );
        assertEquals(28.2d, populationDensity.getValue(), 0.1d);

        TimedValue householdIncome = TimedValueUtils.getLatestBySubjectAndAttribute(
                cityOfLondon,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "householdIncome")
        );
        assertEquals(99390.0d, householdIncome.getValue(), 0.1d);

        TimedValue medianHousePrice = TimedValueUtils.getLatestBySubjectAndAttribute(
                cityOfLondon,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "medianHousePrice")
        );
        assertEquals(765000.0d, medianHousePrice.getValue(), 0.1d);

        TimedValue fractionGreenspace = TimedValueUtils.getLatestBySubjectAndAttribute(
                cityOfLondon,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "fractionGreenspace")
        );
        assertEquals(4.8d, fractionGreenspace.getValue(), 0.1d);

        TimedValue carbonEmission = TimedValueUtils.getLatestBySubjectAndAttribute(
                cityOfLondon,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "carbonEmission")
        );
        assertEquals(1417.5d, carbonEmission.getValue(), 0.1d);

        TimedValue carsPerHousehold = TimedValueUtils.getLatestBySubjectAndAttribute(
                cityOfLondon,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "carsPerHousehold")
        );
        assertEquals(0.4d, carsPerHousehold.getValue(), 0.1d);
    }
}