package uk.org.tombolo.importer.londondatastore;

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

import java.util.List;

import static org.junit.Assert.*;

public class LondonBoroughProfileImporterTest extends AbstractTest {
    LondonBoroughProfileImporter importer = new LondonBoroughProfileImporter();

    Subject cityOfLondon;
    Subject islington;

    @Before
    public void setUp() throws Exception {
        mockDownloadUtils(importer);
        cityOfLondon = TestFactory.makeNamedSubject("E09000001");
        islington = TestFactory.makeNamedSubject("E09000019");
    }

    @Test
    public void getAllDatasources() throws Exception {
        List<Datasource> datasourceList = importer.getAllDatasources();
        assertEquals(1, datasourceList.size());
        assertEquals("londonBoroughProfiles", datasourceList.get(0).getId());
    }

    @Test
    public void getDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("londonBoroughProfiles");

        assertEquals("londonBoroughProfiles", datasource.getId());
        assertEquals("London Borough Profiles", datasource.getName());
        assertEquals(6, datasource.getAttributes().size());
    }

    @Test
    public void importDatasource() throws Exception {
        int valueCount = importer.importDatasource("londonBoroughProfiles");
        assertEquals(12, valueCount);

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