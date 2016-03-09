package uk.org.tombolo.importer.ons;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.datacatalogue.DatasourceDetails;
import uk.org.tombolo.importer.ons.ONSCensusImporter;

public class ONSCensusImporterTest {

	ONSCensusImporter onsImporter = new ONSCensusImporter();
	
	@Test
	public void testGetAllDatasources() throws IOException, ParseException{
		
		List<DatasourceDetails> datasources = onsImporter.getAllDatasources();
		
		assertEquals(701, datasources.size());
	}
	
	@Test
	public void testGetDatasetDetails() throws IOException, ParseException{
				
		String datasetId = "OT102EW";		
		
		// FIXME: This call requires network connection ... perhaps we should mock the json output of the ONS
		DatasourceDetails datasourceDetails = onsImporter.getDatasetDetails(datasetId);
		
		assertEquals(datasetId, datasourceDetails.getName());
		assertEquals("Population density (Out of term-time population)",datasourceDetails.getDescription());
		assertEquals(3, datasourceDetails.getAttributes().size());
		assertEquals("CL_0000855", datasourceDetails.getAttributes().get(0).getLabel());	
		assertEquals("All usual residents", datasourceDetails.getAttributes().get(0).getDescription());
		assertEquals("CL_0000857", datasourceDetails.getAttributes().get(1).getLabel());
		assertEquals("Area (Hectares)", datasourceDetails.getAttributes().get(1).getDescription());
		assertEquals("CL_0000858", datasourceDetails.getAttributes().get(2).getLabel());
		assertEquals("Density (Persons per hectare)", datasourceDetails.getAttributes().get(2).getDescription());
	}
	
	@Test
	public void testLoadDataset() throws IOException, ParseException{
		
		String datasetId = "OT102EW";		

		int count = onsImporter.loadDataset(datasetId);
		
		assertEquals(-1, count);
		
	}
	
	@Test
	public void testSaveProvider() {
		Provider provider = onsImporter.getProvider();
		
		onsImporter.saveProvider(provider);
	}
	
	@Test
	public void testSaveAttributes() throws IOException, ParseException{
		
		String datasetId = "OT102EW";		
		
		// FIXME: This call requires network connection ... perhaps we should mock the json output of the ONS
		DatasourceDetails datasourceDetails = onsImporter.getDatasetDetails(datasetId);

		onsImporter.saveAttributes(datasourceDetails.getAttributes());
		
		// FIXME: Add assertions based on the db
	}
}
