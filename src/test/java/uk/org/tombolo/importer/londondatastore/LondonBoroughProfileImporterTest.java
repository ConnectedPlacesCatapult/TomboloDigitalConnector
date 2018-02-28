package uk.org.tombolo.importer.londondatastore;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *  Using the following test data files:
 *
 *  Local: 633bdc29-cd58-36bf-bba2-8ff48ea7d46a.csv
 */
public class LondonBoroughProfileImporterTest extends AbstractLondonDatastoreTestUtil {
    LondonBoroughProfileImporter importer = new LondonBoroughProfileImporter();

    @Before
    public void setUp() throws Exception {
        mockDownloadUtils(importer);
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

        assertEquals("londonBoroughProfiles", datasource.getDatasourceSpec().getId());
        assertEquals("London Borough Profiles", datasource.getDatasourceSpec().getName());
        assertEquals(6, datasource.getTimedValueAttributes().size());
    }

    @Test
    public void importDatasource() throws Exception {
        TestFactory.makeNamedSubjectType("localAuthority");
        importer.importDatasource("londonBoroughProfiles", null, null, null);
        assertEquals(0, importer.getSubjectCount());
        assertEquals(0, importer.getFixedValueCount());
        assertEquals(12, importer.getTimedValueCount());

        TimedValue populationDensity = TimedValueUtils.getLatestBySubjectAndAttribute(
                cityOfLondon,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "populationDensity")
        );
        assertEquals(30.3d, populationDensity.getValue(), 0.1d);

        TimedValue householdIncome = TimedValueUtils.getLatestBySubjectAndAttribute(
                cityOfLondon,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "householdIncome")
        );
        assertEquals(63620.0d, householdIncome.getValue(), 0.1d);

        TimedValue medianHousePrice = TimedValueUtils.getLatestBySubjectAndAttribute(
                cityOfLondon,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "medianHousePrice")
        );
        assertEquals(799999.0d, medianHousePrice.getValue(), 0.1d);

        TimedValue fractionGreenspace = TimedValueUtils.getLatestBySubjectAndAttribute(
                cityOfLondon,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "fractionGreenspace")
        );
        assertEquals(4.8d, fractionGreenspace.getValue(), 0.1d);

        TimedValue carbonEmission = TimedValueUtils.getLatestBySubjectAndAttribute(
                cityOfLondon,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "carbonEmission")
        );
        assertEquals(1036.0d, carbonEmission.getValue(), 0.1d);

        TimedValue carsPerHousehold = TimedValueUtils.getLatestBySubjectAndAttribute(
                cityOfLondon,
                AttributeUtils.getByProviderAndLabel(importer.getProvider(), "carsPerHousehold")
        );
        assertEquals(0.4d, carsPerHousehold.getValue(), 0.1d);
    }
}