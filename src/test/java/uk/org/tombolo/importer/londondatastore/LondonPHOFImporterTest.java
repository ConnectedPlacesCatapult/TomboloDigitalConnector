package uk.org.tombolo.importer.londondatastore;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Using the following test data files:
 *
 * Local: cd309e6a-a89a-3906-9e7a-26f750dce624.xlsx
 */
public class LondonPHOFImporterTest extends AbstractLondonDatastoreTestUtil {
	private static final String DATASOURCE_ID = "phofIndicatorsLondonBorough";
	private LondonPHOFImporter importer;

	@Before
	public void before(){
		importer = new LondonPHOFImporter(TestFactory.DEFAULT_CONFIG);
		mockDownloadUtils(importer);
	}

	@Test
	public void testGetDatasource() throws Exception {
		Datasource datasource = importer.getDatasource(DATASOURCE_ID);
		
		List<Attribute> attributes = datasource.getTimedValueAttributes();
		assertEquals(36, attributes.size());
		
		String a5Name = "1.02ii - School Readiness: The percentage of Year 1 pupils achieving the expected level in the phonics screening check";
		String a5Label = a5Name;
		Attribute a5 = attributes.stream().filter(a -> a.getLabel().equals(a5Label)).findFirst().get();
		assertEquals(importer.getProvider(), a5.getProvider());
		assertEquals(a5Label, a5.getLabel());
		assertEquals(a5Name, a5.getDescription());

		String a135Name = "2.03 - Smoking status at time of delivery";
		String a135Label = a135Name;
		Attribute a135 = attributes.stream().filter(a -> a.getLabel().equals(a135Label)).findFirst().get();
		assertEquals(importer.getProvider(), a135.getProvider());
		assertEquals(a135Label, a135.getLabel());
		assertEquals(a135Name, a135.getDescription());
	}
	
	@Test
	public void testImportDatasource() throws Exception{
		importer.importDatasource(DATASOURCE_ID, null, null, null);
		assertEquals(0, importer.getSubjectCount());
		assertEquals(0, importer.getFixedValueCount());
		assertEquals(221, importer.getTimedValueCount());

		Attribute attribute = AttributeUtils.getByProviderAndLabel(
				importer.getProvider(),
				"1.01ii - Children in poverty (under 16s)");

		TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(cityOfLondon, attribute);
		TimedValue timedValueIslington = TimedValueUtils.getLatestBySubjectAndAttribute(islington, attribute);
		assertEquals(11.43d, timedValue.getValue(), 0.01d);
		assertEquals(34.42d, timedValueIslington.getValue(), 0.01d);
	}
	
}
