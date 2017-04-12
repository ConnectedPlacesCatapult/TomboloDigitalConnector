package uk.org.tombolo.importer.dft;

import org.hamcrest.core.StringStartsWith;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.org.tombolo.AbstractTest;
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
 * Remote: http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/la/London.csv
 * Local: aHR0cDovL2FwaS5kZnQuZ292LnVrL3YyL3RyYWZmaWNjb3VudHMvZXhwb3J0L2RhdGEvdHJhZmZpYy9yZWdpb24vTG9uZG9uLmNzdg==.csv
 */
public class TrafficCountImporterTest extends AbstractTest {

	private static TrafficCountImporter importer;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void before(){
		importer = new TrafficCountImporter();
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
		assertEquals(Arrays.asList("trafficCounts"),datasources);
	}

	@Test
	public void testGetTrafficCountUrl() throws Exception {
		String url = importer.getTrafficCountUrl("London");
		assertEquals("http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/region/London.csv",url);

		url = importer.getTrafficCountUrl("North East");
		assertEquals("http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/region/North+East.csv",url);

		url = importer.getTrafficCountUrl("Aberdeen City");
		assertEquals("http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/la/Aberdeen+City.csv",url);

		url = importer.getTrafficCountUrl("Bristol, City of");
		assertEquals("http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/la/Bristol%2C+City+of.csv",url);
	}

	@Test
	public void testImportDatasourceUnknown() throws Exception{
		thrown.expect(ConfigurationException.class);
		thrown.expectMessage(new StringStartsWith("Unknown DatasourceId:"));
		importer.importDatasource("xyz");
	}

	@Test
	public void testImportDatasourceNowhere() throws Exception {
		thrown.expect(ConfigurationException.class);
		thrown.expectMessage(new StringStartsWith("Missing geography scope"));
		importer.importDatasource("trafficCounts");
	}

	@Test
	public void testImportDatasourceNorthPole() throws Exception {
		thrown.expect(ConfigurationException.class);
		thrown.expectMessage(new StringStartsWith("Unknown Geography Scope:"));
		importer.importDatasource("trafficCounts", Arrays.asList("North Pole"), null);
	}


	@Test
	public void testImportDatasourceLondon() throws Exception {
		importer.importDatasource("trafficCounts", Arrays.asList("London"), null);
		assertEquals(3, importer.getSubjectCount());
		assertEquals(
				3		// Sensors
				* 15	// Years
				* TrafficCountImporter.COUNT_TYPE.values().length,	// Attributes
				importer.getTimedValueCount());

		// Test the following subject from the input file
		//Year,CP,Region,LocalAuthority,Road,RoadCategory,Easting,Northing,StartJunction,EndJunction,LinkLength_miles,PedalCycles,Motorcycles,CarsTaxis,BusesCoaches,LightGoodsVehicles,V2AxleRigidHGV,V3AxleRigidHGV,V4or5AxleRigidHGV,V3or4AxleArticHGV,V5AxleArticHGV,V6orMoreAxleArticHGV,AllHGVs,AllMotorVehicles
		//"2008","6075","London","Islington","A1","PU","530600","185870","A503 Camden Road","A503 Seven Sisters/Parkhurst Road","0.12","66","85","1334","57","329","57","6","5","3","3","3","77","1883"

		List<Subject> subjects = SubjectUtils.getSubjectByTypeAndLabelPattern(SubjectTypeUtils.getSubjectTypeByLabel("trafficCounter"),"DfT-TrafficCounter-6075");
		assertEquals(1, subjects.size());
		Subject subject = subjects.get(0);
		assertEquals("A1"+" ("+"A503 Camden Road"+" to "+"A503 Seven Sisters/Parkhurst Road"+")", subject.getName());

		testTimedValue(subject, "CountPedalCycles", "2008-12-31T23:59:59", 66.0);
		testTimedValue(subject, "CountMotorcycles", "2008-12-31T23:59:59", 85.0);
		testTimedValue(subject, "CountCarsTaxis", "2008-12-31T23:59:59", 1334.0);
		testTimedValue(subject, "CountBusesCoaches", "2008-12-31T23:59:59", 57.0);
		testTimedValue(subject, "CountLightGoodsVehicles", "2008-12-31T23:59:59", 329.0);
		testTimedValue(subject, "CountHeavyGoodsVehicles", "2008-12-31T23:59:59", 77.0);
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
