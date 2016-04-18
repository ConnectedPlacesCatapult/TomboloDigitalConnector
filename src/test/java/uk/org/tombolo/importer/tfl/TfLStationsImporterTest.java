package uk.org.tombolo.importer.tfl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TfLStationsImporterTest {
	TfLStationsImporter importer = new TfLStationsImporter();
	
	@Test
	public void testImportDatasource() throws Exception {
		int count = importer.importDatasource("StationList");
		assertEquals(301, count);
	}	
}
