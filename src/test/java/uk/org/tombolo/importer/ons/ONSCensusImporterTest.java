package uk.org.tombolo.importer;

import static org.junit.Assert.*;

import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import uk.org.tombolo.datacatalogue.DatasourceDetails;

public class ONSCensusImporterTest {

	@Test
	public void testGetDatasetDetails() throws IOException, ParseException{
		
		ONSCensusImporter onsImporter = new ONSCensusImporter();
		
		String datasetId = "OT102EW";		
		
		// FIXME: This call requires network connection ... perhaps we should mock the json output of the ONS
		DatasourceDetails datasourceDetails = onsImporter.getDatasetDetails(datasetId);
		
		assertEquals(datasetId, datasourceDetails.getName());
		assertEquals("Population density (Out of term-time population)",datasourceDetails.getDescription());
		assertEquals(3, datasourceDetails.getAttributes().size());
		assertEquals("CL_0000855", datasourceDetails.getAttributes().get(0).getName());	
		assertEquals("All usual residents", datasourceDetails.getAttributes().get(0).getDescription());
		assertEquals("CL_0000857", datasourceDetails.getAttributes().get(1).getName());
		assertEquals("Area (Hectares)", datasourceDetails.getAttributes().get(1).getDescription());
		assertEquals("CL_0000858", datasourceDetails.getAttributes().get(2).getName());
		assertEquals("Density (Persons per hectare)", datasourceDetails.getAttributes().get(2).getDescription());
	}
}
