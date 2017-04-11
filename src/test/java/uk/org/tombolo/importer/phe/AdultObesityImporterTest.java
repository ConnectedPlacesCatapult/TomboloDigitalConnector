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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AdultObesityImporterTest extends AbstractTest {
	private static final String DATASOURCE_ID = "laAdultObesity2014";
	AdultObesityImporter importer = new AdultObesityImporter();

	private Subject cityOfLondon;

	@Before
	public void addSubjectFixtures() {
		cityOfLondon = TestFactory.makeNamedSubject(TestFactory.DEFAULT_PROVIDER, "E09000001");
	}

	@Before
	public void setDownloadUtils() {
		mockDownloadUtils(importer);
	}
	
	@Test
	public void testGetDatasource() throws Exception {
		Datasource datasource = importer.getDatasource(DATASOURCE_ID);
		
		List<Attribute> attributes = datasource.getTimedValueAttributes();
		assertEquals(5, attributes.size());
	}
	
	@Test
	public void testImportDatasource() throws Exception{
		int datapoints = importer.importDatasource(DATASOURCE_ID);
		
		//FIXME: Find a way to match Gateshead etc.
		assertEquals(5, datapoints);

		Map<String, Double> groundTruthCoL = new HashMap();

		groundTruthCoL.put("fractionUnderweight", 0.001916d);
		groundTruthCoL.put("fractionHealthyWeight", 0.530573d);
		groundTruthCoL.put("fractionOverweight", 0.287439d);
		groundTruthCoL.put("fractionObese", 0.180070d);
		groundTruthCoL.put("fractionExcessWeight", 0.467510d);

		for (String attributeName : groundTruthCoL.keySet()) {
			Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeName);
			TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon, attribute);
			assertEquals("Value for "+attributeName, groundTruthCoL.get(attributeName), timedValue.getValue(), 0.0001d);
		}
	}
}
