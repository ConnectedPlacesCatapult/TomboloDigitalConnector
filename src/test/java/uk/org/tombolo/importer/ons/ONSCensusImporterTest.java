package uk.org.tombolo.importer.ons;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.utils.AttributeUtils;

public class ONSCensusImporterTest extends AbstractONSCensusImporterTest {

	public static final String datasourceId = "OT102EW";

	@Before
	public void addGeography() {
		TestFactory.makeNamedGeography("E01002766");
	}
	
	@Test
	public void testGetAllDatasources() throws Exception{
		// FIXME: This call requires network connection ... perhaps we should mock the json output of the ONS
		List<Datasource> datasources = importer.getAllDatasources();
		
		// FIXME: For some reason this has changed in the API
		//assertEquals(701, datasources.size());
		
		assertEquals(695, datasources.size());
	}
	
	@Test
	public void testGetDatasetDetails() throws Exception{		
						
		// FIXME: This call requires network connection ... perhaps we should mock the json output of the ONS
		Datasource datasourceDetails = importer.getDatasource(datasourceId);
		
		assertEquals(datasourceId, datasourceDetails.getName());
		assertEquals("Population density (Out of term-time population)",datasourceDetails.getDescription());
		assertEquals(3, datasourceDetails.getAttributes().size());
		assertEquals("CL_0000855", datasourceDetails.getAttributes().get(0).getLabel());	
		assertEquals("All usual residents", datasourceDetails.getAttributes().get(0).getDescription());
		assertEquals("CL_0000857", datasourceDetails.getAttributes().get(1).getLabel());
		assertEquals("Area (Hectares)", datasourceDetails.getAttributes().get(1).getDescription());
		assertEquals("CL_0000858", datasourceDetails.getAttributes().get(2).getLabel());
		assertEquals("Density (Persons per hectare)", datasourceDetails.getAttributes().get(2).getDescription());
		assertEquals("http://data.statistics.gov.uk/ons/datasets/csv/CSV_OT102EW_2011STATH_1_EN.zip", datasourceDetails.getRemoteDatafile());
		assertEquals("csv/CSV_OT102EW_2011STATH_1_EN.zip", datasourceDetails.getLocalDatafile());
	}
		
	@Test
	public void testLoadDataset() throws Exception{
		Datasource datasource = importer.getDatasource(datasourceId);		
		int count = importer.importDatasource(datasource);
		
		assertEquals(3, count);
		
		Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), "CL_0000857");
		assertEquals("Area (Hectares)", attribute.getName());
		
	}	
}
