package uk.org.tombolo.importer.ons;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.utils.AttributeUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Using the following test data files:
 *
 * Remote; http://data.statistics.gov.uk/ons/datasets/csv/CSV_OT102EW_2011STATH_1_EN.zip
 * Local: 1877d607-95df-3ee3-ba35-d69637198668.zip
 */
public class ONSCensusImporterTest extends AbstractONSCensusImporterTest {

	public static final String datasourceId = "OT102EW";

	@Before
	public void addSubjectFixtures() {
		TestFactory.makeNamedSubject("E01002766");
		TestFactory.makeNamedSubject("E08000035");
	}



	@Test
	public void testGetDatasourceIds() throws Exception{
		List<String> datasources = importer.getDatasourceIds();

		assertEquals(695, datasources.size());
	}
	
	@Test
	public void testGetDatasetDetails() throws Exception{		
						
		// FIXME: This call requires network connection ... perhaps we should mock the json output of the ONS
		Datasource datasourceDetails = importer.getDatasource(datasourceId);
		
		assertEquals(datasourceId, datasourceDetails.getDatasourceSpec().getName());
		assertEquals("Population density (Out of term-time population)",datasourceDetails.getDatasourceSpec().getDescription());
		assertEquals(3, datasourceDetails.getTimedValueAttributes().size());
		assertEquals("CL_0000855", datasourceDetails.getTimedValueAttributes().get(0).getLabel());
		assertEquals("All usual residents", datasourceDetails.getTimedValueAttributes().get(0).getDescription());
		assertEquals("CL_0000857", datasourceDetails.getTimedValueAttributes().get(1).getLabel());
		assertEquals("Area (Hectares)", datasourceDetails.getTimedValueAttributes().get(1).getDescription());
		assertEquals("CL_0000858", datasourceDetails.getTimedValueAttributes().get(2).getLabel());
		assertEquals("Density (Persons per hectare)", datasourceDetails.getTimedValueAttributes().get(2).getDescription());
		assertEquals("http://data.statistics.gov.uk/ons/datasets/csv/CSV_OT102EW_2011STATH_1_EN.zip", importer.getDataFile());
	}
		
	@Test
	public void testLoadDataset() throws Exception{
		importer.importDatasource(datasourceId, null, null, null);
		
		assertEquals(3 + 3, importer.getTimedValueCount());
		
		Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "CL_0000857");
		assertEquals("Area (Hectares)", attribute.getDescription());
		
	}

	@Test
	public void testLoadingOfProperties() throws Exception {
		assertEquals("onsApiKeyTest",importer.getConfiguration().getProperty(AbstractONSImporter.PROP_ONS_API_KEY));
	}
}
