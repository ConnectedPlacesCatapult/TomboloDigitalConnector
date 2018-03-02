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

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Using the following test data files:
 *
 * Remote: https://www.noo.org.uk/gsf.php5?f=314008&fv=21761
 * Local: 1a02e39e-59ac-3c05-a11e-51d0bbb58a0f.xlsx
 */
public class SportsEnglandImporterTest extends AbstractTest {
	private SportsEnglandImporter importer;

	private Subject islington;

	@Before
	public void setUp() throws Exception {
		importer = new SportsEnglandImporter();
		mockDownloadUtils(importer);

		islington = TestFactory.makeNamedSubject("E09000019");

	}

	@Test
	public void getDatasourceIds() throws Exception {
		List<String> datasources = importer.getDatasourceIds();
		assertEquals(Arrays.asList("adultObesity", "MVPA"), datasources);
	}

	@Test
	public void getDatasource() throws Exception {
		Datasource datasourceAdultObesity = importer.getDatasource("adultObesity");
		assertEquals(8, datasourceAdultObesity.getTimedValueAttributes().size());

		Datasource datasourceMVPA = importer.getDatasource("MVPA");
		assertEquals(8, datasourceMVPA.getTimedValueAttributes().size());
	}
	
	@Test
	public void testImportDatasourceAdultObesity() throws Exception{
		importer.importDatasource("adultObesity", null, null, null);
		// 3 years, 3 attributes
		assertEquals(9, importer.getTimedValueCount());

		Map<String, Double> groundTruthCoL = new HashMap();

		// Test only for fractionOverweight and fractionObese as the rest of the attributes have missing values in the dataset
		groundTruthCoL.put("fractionOverweight", 0.260d);
		groundTruthCoL.put("fractionObese", 0.0775d);
		groundTruthCoL.put("fractionHealthyWeight", 0.62d);

		for (String attributeName : groundTruthCoL.keySet()) {
			Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeName);
			TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington, attribute);
			assertEquals("Value for "+attributeName, groundTruthCoL.get(attributeName), timedValue.getValue(), 0.0001d);
		}
	}

	@Test
	public void testImportDatasourceMVPA() throws Exception{
		importer.importDatasource("MVPA", null, null, null);
		// 3 years, 3 attributes
		assertEquals(9, importer.getTimedValueCount());

		Map<String, Double> groundTruthCoL = new HashMap();

		// Test only for fractionActive as the rest of the attributes have missing values in the dataset
		groundTruthCoL.put("fractionActive", 0.662d);
		groundTruthCoL.put("fractionActive600PlusMVPA", 0.338d);
		groundTruthCoL.put("fractionActive150_599MVPA", 0.325d);

		for (String attributeName : groundTruthCoL.keySet()) {
			Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeName);
			TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(islington, attribute);
			assertEquals("Value for "+attributeName, groundTruthCoL.get(attributeName), timedValue.getValue(), 0.0001d);
		}
	}
}
