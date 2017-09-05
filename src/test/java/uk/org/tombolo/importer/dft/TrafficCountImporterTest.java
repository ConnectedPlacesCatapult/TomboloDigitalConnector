package uk.org.tombolo.importer.dft;

import org.hamcrest.core.StringStartsWith;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.ConfigurationException;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * Using the following test data files:
 *
 *
 * Remote: http://api.dft.gov.uk/v3/trafficcounts/export/region/London.csv
 * Local: d16f455b-e573-3733-8613-dcab739c7a96.csv
 *
 * Remote: http://api.dft.gov.uk/v3/trafficcounts/export/data/traffic/la/Aberdeen+City.csv
 * Local: 6d8eed14-92dc-3fe5-b7f5-305d2e2d1229.csv
 */
public class TrafficCountImporterTest extends AbstractTest {

	private static TrafficCountImporter importer;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void before(){
		importer = new TrafficCountImporter(TestFactory.DEFAULT_CONFIG);
		mockDownloadUtils(importer);
	}

	@Test
	public void testGetProvider(){
		Provider provider = importer.getProvider();
		assertEquals("uk.gov.dft", provider.getLabel());
		assertEquals("Department for Transport", provider.getName());
	}

	@Test
	public void testGetDatasourcIds() throws Exception {
		List<String> datasources = importer.getDatasourceIds();
		assertEquals(Arrays.asList("trafficVolume", "trafficCounts"),datasources);
	}

	@Test
	public void testGetTrafficCountUrl() throws Exception {
		String url = importer.getTrafficCountUrl(TrafficCountImporter.DatasourceId.trafficCounts,"London");
		assertEquals("http://api.dft.gov.uk/v3/trafficcounts/export/region/London.csv",url);

		url = importer.getTrafficCountUrl(TrafficCountImporter.DatasourceId.trafficCounts, "Aberdeen City");
		assertEquals("http://api.dft.gov.uk/v3/trafficcounts/export/la/Aberdeen+City.csv",url);

		url = importer.getTrafficCountUrl(TrafficCountImporter.DatasourceId.trafficVolume, "North East");
		assertEquals("http://api.dft.gov.uk/v3/trafficcounts/export/data/traffic/region/North+East.csv",url);

		url = importer.getTrafficCountUrl(TrafficCountImporter.DatasourceId.trafficVolume, "Bristol, City of");
		assertEquals("http://api.dft.gov.uk/v3/trafficcounts/export/data/traffic/la/Bristol%2C+City+of.csv",url);
	}

	@Test
	public void testImportDatasourceUnknown() throws Exception{
		thrown.expect(ConfigurationException.class);
		thrown.expectMessage(new StringStartsWith("Unknown DatasourceId:"));
		importer.importDatasource("xyz", null, null, null);
	}

	@Test
	public void testImportDatasourceNowhere() throws Exception {
		thrown.expect(ConfigurationException.class);
		thrown.expectMessage(new StringStartsWith("Missing geography scope"));
		importer.importDatasource("trafficCounts", null, null, null);
	}

	@Test
	public void testImportDatasourceNorthPole() throws Exception {
		thrown.expect(ConfigurationException.class);
		thrown.expectMessage(new StringStartsWith("Unknown Geography Scope:"));
		importer.importDatasource("trafficCounts", Arrays.asList("North Pole"), null, null);
	}

	@Test
	public void testImportDatasourceCountLondon() throws Exception {
		importer.importDatasource("trafficCounts", Arrays.asList("London"), null, null);
		assertEquals(4, importer.getSubjectCount());
		assertEquals(4*4, importer.getFixedValueCount());
		assertEquals(
				4		// Sensors
				* 9	// Years
				* TrafficCountImporter.CountAttribute.values().length	// Attributes
				- TrafficCountImporter.CountAttribute.values().length, // One missing value
				importer.getTimedValueCount());

		// Test the following subject from the input file
		//AADFYear,CP,Estimation_method,Estimation_method_detailed,Region,LocalAuthority,Road,RoadCategory,Easting,Northing,StartJunction,EndJunction,LinkLength_km,LinkLength_miles,PedalCycles,Motorcycles,CarsTaxis,BusesCoaches,LightGoodsVehicles,V2AxleRigidHGV,V3AxleRigidHGV,V4or5AxleRigidHGV,V3or4AxleArticHGV,V5AxleArticHGV,V6orMoreAxleArticHGV,AllHGVs,AllMotorVehicles
		//"2004","6075","Estimated","Estimated using previous year's AADF on this link","London","Islington","A1","PU","530600","185870","A503 Camden Road","A503 Seven Sisters/Parkhurst Road","0.20","0.12000000000000000","671","1774","26484","1254","5727","944","167","106","17","30","43","1307","36546"

		List<Subject> subjects = SubjectUtils.getSubjectByTypeAndLabelPattern(SubjectTypeUtils.getSubjectTypeByProviderAndLabel(importer.getProvider().getLabel(), "trafficCounter"),"DfT-TrafficCounter-6075");
		assertEquals(1, subjects.size());
		Subject subject = subjects.get(0);
		assertEquals("A1"+" ("+"A503 Camden Road"+" to "+"A503 Seven Sisters/Parkhurst Road"+")", subject.getName());

		testTimedValue(subject, "CountPedalCycles", "2004-12-31T23:59:59", 671.0);
		testTimedValue(subject, "CountMotorcycles", "2004-12-31T23:59:59", 1774.0);
		testTimedValue(subject, "CountCarsTaxis", "2004-12-31T23:59:59", 26484.0);
		testTimedValue(subject, "CountBusesCoaches", "2004-12-31T23:59:59", 1254.0);
		testTimedValue(subject, "CountLightGoodsVehicles", "2004-12-31T23:59:59", 5727.0);
		testTimedValue(subject, "CountHeavyGoodsVehicles", "2004-12-31T23:59:59", 1307.0);
	}

	@Test
	public void testImportDatasourceVolumeAberdeenCity() throws Exception {
		importer.importDatasource("trafficVolume", Arrays.asList("Aberdeen City"), null, null);
		assertEquals(2, importer.getSubjectCount());
		assertEquals(2*4, importer.getFixedValueCount());

		assertEquals(
				2		// Sensors
						* 17	// Years
						* TrafficCountImporter.VolumeAttribute.values().length,	// Attributes
				importer.getTimedValueCount());

		// Test the following subject from the input file
		// Year,CP,Estimation_method,Estimation_method_detailed,Region,LocalAuthority,Road,RoadCategory,Easting,Northing,StartJunction,EndJunction,LinkLength_miles,PedalCycles,Motorcycles,CarsTaxis,BusesCoaches,LightGoodsVehicles,V2AxleRigidHGV,V3AxleRigidHGV,V4or5AxleRigidHGV,V3or4AxleArticHGV,V5AxleArticHGV,V6orMoreAxleArticHGV,AllHGVs,AllMotorVehicles
		// "2008","1041","Estimated","Estimated using previous year's AADF on this link","Scotland","Aberdeen City","A956","PU","394400","809000","A978","B997 Balgownie Road","0.81000000000000000","49.37","45.83","5419.24","144.57","851.76","327.87","47.6","48.49","16.85","56.47","68.89","566.17","7027.56"

		List<Subject> subjects = SubjectUtils.getSubjectByTypeAndLabelPattern(SubjectTypeUtils.getSubjectTypeByProviderAndLabel(importer.getProvider().getLabel(), "trafficCounter"),"DfT-TrafficCounter-1041");
		assertEquals(1, subjects.size());
		Subject subject = subjects.get(0);
		assertEquals("A956"+" ("+"A978"+" to "+"B997 Balgownie Road"+")", subject.getName());

		testTimedValue(subject, "VolumePedalCycles", "2008-12-31T23:59:59", 49.37);
		testTimedValue(subject, "VolumeMotorcycles", "2008-12-31T23:59:59", 45.83);
		testTimedValue(subject, "VolumeCarsTaxis", "2008-12-31T23:59:59", 5419.24);
		testTimedValue(subject, "VolumeBusesCoaches", "2008-12-31T23:59:59", 144.57);
		testTimedValue(subject, "VolumeLightGoodsVehicles", "2008-12-31T23:59:59", 851.76);
		testTimedValue(subject, "VolumeHeavyGoodsVehicles", "2008-12-31T23:59:59", 566.17);
	}

	private void testTimedValue(Subject subject, String attributeLabel, String timestamp, Double value){
		Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(),attributeLabel);
		List<TimedValue> counts = TimedValueUtils.getBySubjectAndAttribute(subject, attribute);
		TimedValue count = counts.stream()
				.filter(e -> timestamp.equals(e.getId().getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
				.findAny()
				.orElse(null);
		assertEquals("Value for key ("+subject.getLabel()+","+attributeLabel+","+timestamp+")", value, count.getValue(),0.1);
	}
}
