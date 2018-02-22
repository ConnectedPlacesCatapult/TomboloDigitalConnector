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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Using the following test data files:
 *
 * Remote: https://www.noo.org.uk/gsf.php5?f=314008&fv=21761
 * Local: 1a02e39e-59ac-3c05-a11e-51d0bbb58a0f.xlsx
 */
public class AdultObesityImporterTest extends AbstractTest {
	//Create this private class to avoid importing the oa data that slows down the test
	private class ImporterTest extends AdultObesityImporter {
		@Override
		protected List<String> getOaDatasourceIds() {

			return Collections.emptyList();
		}
	}
	private static final String DATASOURCE_ID = "adultObesity";
	private AdultObesityImporter importer = new ImporterTest();

	private Subject islington;

	@Before
	public void addSubjectFixtures() {
		islington = TestFactory.makeNamedSubject("E09000019");
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
		importer.importDatasource(DATASOURCE_ID, null, null, null);

		// 3 years, 3 attributes
		assertEquals(9, importer.getTimedValueCount());

		Map<String, Double> groundTruthCoL = new HashMap();

		// Test only for fractionOverweight and fractionObese as the rest of the attributes have missing values in the dataset
		groundTruthCoL.put("fractionOverweight", 0.260d);
		groundTruthCoL.put("fractionObese", 0.0775d);
		groundTruthCoL.put("fractionHealthyWeight", 0.62d);

		for (String attributeName : groundTruthCoL.keySet()) {
			Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeName);
			System.out.println(attribute.getLabel());
			TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington, attribute);
			assertEquals("Value for "+attributeName, groundTruthCoL.get(attributeName), timedValue.getValue(), 0.0001d);
		}
	}
}
