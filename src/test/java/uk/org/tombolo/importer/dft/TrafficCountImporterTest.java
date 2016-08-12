package uk.org.tombolo.importer.dft;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.Importer;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TrafficCountImporterTest extends AbstractTest {

	private static Importer importer;

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
	public void testGetAllDatasources() throws Exception {
		List<Datasource> datasources = importer.getAllDatasources();
		assertEquals(220,datasources.size());
	}

	@Test
	public void testGetDatasource() throws Exception {
		Datasource datasource = importer.getDatasource("London");
		assertEquals("http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/region/London.csv",datasource.getRemoteDatafile());
		assertEquals("dft/traffic/region/London.csv", datasource.getLocalDatafile());
		
		datasource = importer.getDatasource("North East");
		assertEquals("http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/region/North+East.csv",datasource.getRemoteDatafile());
		assertEquals("dft/traffic/region/North_East.csv", datasource.getLocalDatafile());
		
		datasource = importer.getDatasource("Aberdeen City");
		assertEquals("http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/la/Aberdeen+City.csv",datasource.getRemoteDatafile());
		assertEquals("dft/traffic/la/Aberdeen_City.csv", datasource.getLocalDatafile());
		
		datasource = importer.getDatasource("Bristol, City of");
		assertEquals("http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/la/Bristol%2C+City+of.csv",datasource.getRemoteDatafile());
		assertEquals("dft/traffic/la/Bristol__City_of.csv", datasource.getLocalDatafile());
	}

	@Test
	public void testImportDatasource() throws Exception {
		int importCount = importer.importDatasource("London");
		assertEquals(
				3		// Sensors
				* 15	// Years
				* TrafficCountImporter.COUNT_TYPE.values().length,	// Attributes
				importCount);

		// Test the following subject from the input file
		//Year,CP,Region,LocalAuthority,Road,RoadCategory,Easting,Northing,StartJunction,EndJunction,LinkLength_miles,PedalCycles,Motorcycles,CarsTaxis,BusesCoaches,LightGoodsVehicles,V2AxleRigidHGV,V3AxleRigidHGV,V4or5AxleRigidHGV,V3or4AxleArticHGV,V5AxleArticHGV,V6orMoreAxleArticHGV,AllHGVs,AllMotorVehicles
		//"2008","6075","London","Islington","A1","PU","530600","185870","A503 Camden Road","A503 Seven Sisters/Parkhurst Road","0.12","66","85","1334","57","329","57","6","5","3","3","3","77","1883"

		List<Subject> subjects = SubjectUtils.getSubjectByTypeAndLabelPattern(SubjectTypeUtils.getSubjectTypeByLabel("sensor"),"DfT-TrafficCounter-6075");
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
