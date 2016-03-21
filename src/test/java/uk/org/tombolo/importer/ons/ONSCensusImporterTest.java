package uk.org.tombolo.importer.ons;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.importer.ons.ONSCensusImporter;

public class ONSCensusImporterTest {

	ONSCensusImporter onsImporter = new ONSCensusImporter();
	
	@Test
	public void testGetAllDatasources() throws IOException, ParseException{
		
		List<Datasource> datasources = onsImporter.getAllDatasources();
		
		assertEquals(701, datasources.size());
	}
	
	@Test
	public void testGetDatasetDetails() throws IOException, ParseException{
				
		String datasetId = "OT102EW";		
		
		// FIXME: This call requires network connection ... perhaps we should mock the json output of the ONS
		Datasource datasourceDetails = onsImporter.getDatasource(datasetId);
		
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
		// FIXME: Mock this data so that the test will not take a very very very long time
		String datasetId = "OT102EW";		

		int count = onsImporter.importDatasource(datasetId);
		
		// FIXME: Justify this number
		assertEquals(350623, count);
		
	}	
}
