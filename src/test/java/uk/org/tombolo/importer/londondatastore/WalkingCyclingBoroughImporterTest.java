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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *  Using the following test data files:
 *
 *  Local: aHR0cHM6Ly9maWxlcy5kYXRhcHJlc3MuY29tL2xvbmRvbi9kYXRhc2V0L3dhbGtpbmctYW5kLWN5Y2xpbmctYm9yb3VnaC93YWxraW5nLWN5Y2xpbmctYm9yb3VnaC54bHM=.xls
 */
public class WalkingCyclingBoroughImporterTest extends AbstractTest {
	private static final String DATASOURCE_ID = "walkingCyclingBorough";
	public WalkingCyclingBoroughImporter importer;

	Subject cityOfLondon;

	@Before
	public void addSubjectFixtures() {
		cityOfLondon = TestFactory.makeNamedSubject("E09000001");
	}

	@Before
	public void before(){
		importer = new WalkingCyclingBoroughImporter(TestFactory.DEFAULT_CONFIG);
		mockDownloadUtils(importer);
	};

	@Test
	public void testGetDatasourceIds() throws Exception {
		List<String> datasources = importer.getDatasourceIds();
		assertEquals(Arrays.asList("walkingCyclingBorough"), datasources);
	}

	@Test
	public void testGetDatasource() throws Exception {
		Datasource datasource = importer.getDatasource(DATASOURCE_ID);
		
		List<Attribute> attributes = datasource.getTimedValueAttributes();
		assertEquals(2, attributes.size());
	}
	
	@Test
	public void testImportDatasource() throws Exception{
		importer.importDatasource(DATASOURCE_ID, null, null, null);
		
		assertEquals(8, importer.getTimedValueCount());

		LocalDateTime year2012 = LocalDateTime.parse("2012-12-31T23:59:59");
		LocalDateTime year2014 = LocalDateTime.parse("2014-12-31T23:59:59");

		List<TimedValue> walk5xWeek = TimedValueUtils.getBySubjectAndAttribute(
				cityOfLondon,
				AttributeUtils.getByProviderAndLabel(importer.getProvider(), "walk5xWeek"));
		assertEquals(4, walk5xWeek.size());
		TimedValue walk5xWeek2012 = walk5xWeek.stream().filter(a -> a.getId().getTimestamp().equals(year2012)).findFirst().orElse(null);
		assertEquals(31.0d, walk5xWeek2012.getValue(), 0.1d);
		TimedValue walk5xWeek2014 = walk5xWeek.stream().filter(a -> a.getId().getTimestamp().equals(year2014)).findFirst().orElse(null);
		assertEquals(56.7d, walk5xWeek2014.getValue(), 0.1d);

		List<TimedValue> cycle1xWeek = TimedValueUtils.getBySubjectAndAttribute(
				cityOfLondon,
				AttributeUtils.getByProviderAndLabel(importer.getProvider(), "cycle1xWeek"));
		assertEquals(4, cycle1xWeek.size());
		TimedValue cycle1xWeek2012 = cycle1xWeek.stream().filter(a -> a.getId().getTimestamp().equals(year2012)).findFirst().orElse(null);
		assertEquals(16.0d, cycle1xWeek2012.getValue(), 0.1d);
		TimedValue cycle1xWeek2014 = cycle1xWeek.stream().filter(a -> a.getId().getTimestamp().equals(year2014)).findFirst().orElse(null);
		assertEquals(7.4d, cycle1xWeek2014.getValue(), 0.1d);
	}
}
