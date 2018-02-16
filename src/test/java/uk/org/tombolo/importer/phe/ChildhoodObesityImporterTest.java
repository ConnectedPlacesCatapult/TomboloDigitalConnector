package uk.org.tombolo.importer.phe;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Using the following test data files:
 *
 * Remote: http://www.noo.org.uk/securefiles/161024_1352/20150511_MSOA_Ward_Obesity.xlsx
 * Local: c686dc46-0b81-3ede-b85d-29cd912f86be.xlsx
 */
public class ChildhoodObesityImporterTest extends AbstractTest {
    private ChildhoodObesityImporter importer;

    private Subject cityOfLondon001;
    private Subject finsburyPark;
    private Subject leeds;

    @Before
    public void setUp() throws Exception {
        importer = new ChildhoodObesityImporter();
        mockDownloadUtils(importer);

        cityOfLondon001 = TestFactory.makeNamedSubject("E02000001");  // City of London 001
        finsburyPark = TestFactory.makeNamedSubject("E05000371");  // Finsbury Park
        leeds = TestFactory.makeNamedSubject("E08000035"); // Leeds
    }

    @Test
    public void getDatasourceIds() throws Exception {
        List<String> datasources = importer.getDatasourceIds();
        assertEquals(Arrays.asList("childhoodObesityLA", "childhoodObesityMSOA", "childhoodObesityWard"), datasources);
    }

    @Test
    public void getDatasource() throws Exception {
        Datasource datasourceLA = importer.getDatasource("childhoodObesityLA");
        assertEquals(12, datasourceLA.getTimedValueAttributes().size());

        Datasource datasourceMSOA = importer.getDatasource("childhoodObesityMSOA");
        assertEquals(12, datasourceMSOA.getTimedValueAttributes().size());

        Datasource datasourceWard = importer.getDatasource("childhoodObesityWard");
        assertEquals(12, datasourceWard.getTimedValueAttributes().size());
    }

    @Test
    public void importDatasourceWard() throws Exception {
        importer.importDatasource("childhoodObesityWard", Arrays.asList("ward"), null, null);

        Map<String, Double> groundTruthCoL001 = new HashMap();

        groundTruthCoL001.put("Reception_Obese_%", 12.6499163078488d/100.);
        groundTruthCoL001.put("Reception_Obese_LCI", 10.0088058246682d/100.);
        groundTruthCoL001.put("Reception_Obese_UCI", 15.8650930439096d/100.);

        groundTruthCoL001.put("Year6_ExcessWeight_%", 41.3519022837799d/100.);
        groundTruthCoL001.put("Year6_ExcessWeight_LCI", 36.479137705224d/100.);
        groundTruthCoL001.put("Year6_ExcessWeight_UCI", 46.4001005504903d/100.);

        groundTruthCoL001.put("Year6_Obese_%", 25.2708697857725d/100.);
        groundTruthCoL001.put("Year6_Obese_LCI", 21.1379194228944d/100.);
        groundTruthCoL001.put("Year6_Obese_UCI", 29.9054706457531d/100.);

        groundTruthCoL001.put("Reception_ExcessWeight_%", 24.582235216267d/100.);
        groundTruthCoL001.put("Reception_ExcessWeight_LCI", 20.9979461342944d/100.);
        groundTruthCoL001.put("Reception_ExcessWeight_UCI", 28.5571922578152d/100.);

        for (String attributeName : groundTruthCoL001.keySet()) {
            Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeName);
            TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(finsburyPark, attribute);
            assertEquals("Value for "+attributeName, groundTruthCoL001.get(attributeName), timedValue.getValue(), 0.0001d);
        }
    }

    @Test
    public void importDatasourceMSOA() throws Exception {
        importer.importDatasource("childhoodObesityMSOA", Arrays.asList("msoa"), null, null);

        Map<String, Double> groundTruthCoL001 = new HashMap();

        groundTruthCoL001.put("Reception_Obese_%", 13.2530120481928d/100.);
        groundTruthCoL001.put("Reception_Obese_LCI", 7.56430649275991d/100.);
        groundTruthCoL001.put("Reception_Obese_UCI", 22.1927463845418d/100.);

        groundTruthCoL001.put("Year6_ExcessWeight_%", 40d/100.);
        groundTruthCoL001.put("Year6_ExcessWeight_LCI", 27.6083897302565d/100.);
        groundTruthCoL001.put("Year6_ExcessWeight_UCI", 53.8185622524106d/100.);

        groundTruthCoL001.put("Year6_Obese_%", 28d/100.);
        groundTruthCoL001.put("Year6_Obese_LCI", 17.4741706659112d/100.);
        groundTruthCoL001.put("Year6_Obese_UCI", 41.6651236959566d/100.);

        groundTruthCoL001.put("Reception_ExcessWeight_%", 28.9156626506024d/100.);
        groundTruthCoL001.put("Reception_ExcessWeight_LCI", 20.2674488491748d/100.);
        groundTruthCoL001.put("Reception_ExcessWeight_UCI", 39.4292208345229d/100.);

        for (String attributeName : groundTruthCoL001.keySet()) {
            Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeName);
            TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon001, attribute);
            assertEquals("Value for "+attributeName, groundTruthCoL001.get(attributeName), timedValue.getValue(), 0.0001d);
        }
    }

    @Test
    public void importDatasourceLA() throws Exception {
        importer.importDatasource("childhoodObesityLA", Arrays.asList("localAuthority"), null, null);

        Map<String, Double> groundTruthCoL001 = new HashMap();

        groundTruthCoL001.put("Reception_Obese_%", 8.99749373433584d/100.);
        groundTruthCoL001.put("Reception_Obese_LCI", 8.6675250022043d/100.);
        groundTruthCoL001.put("Reception_Obese_UCI", 9.33873978681711d/100.);

        groundTruthCoL001.put("Year6_ExcessWeight_%", 34.218667138059d/100.);
        groundTruthCoL001.put("Year6_ExcessWeight_LCI", 33.6032225305155d/100.);
        groundTruthCoL001.put("Year6_ExcessWeight_UCI", 34.8394690949314d/100.);

        groundTruthCoL001.put("Year6_Obese_%", 19.6659006540569d/100.);
        groundTruthCoL001.put("Year6_Obese_LCI", 19.1531845465194d/100.);
        groundTruthCoL001.put("Year6_Obese_UCI", 20.1889143941324d/100.);

        groundTruthCoL001.put("Reception_ExcessWeight_%", 21.9298245614035d/100.);
        groundTruthCoL001.put("Reception_ExcessWeight_LCI", 21.4484446971911d/100.);
        groundTruthCoL001.put("Reception_ExcessWeight_UCI", 22.4189248405252d/100.);

        for (String attributeName : groundTruthCoL001.keySet()) {
            Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeName);
            TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(leeds, attribute);
            assertEquals("Value for "+attributeName, groundTruthCoL001.get(attributeName), timedValue.getValue(), 0.0001d);
        }
    }

}
