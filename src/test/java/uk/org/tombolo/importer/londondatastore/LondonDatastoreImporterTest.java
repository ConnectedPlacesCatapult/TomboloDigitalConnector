package uk.org.tombolo.importer.londondatastore;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import uk.org.tombolo.core.Datasource;

public class LondonDatastoreImporterTest {

	LondonDatastoreImporter importer = new LondonDatastoreImporter();
	
	@Test
	public void testGetAllDatasources() throws Exception{
		List<Datasource> datasources = importer.getAllDatasources();
		
		// We have at least two data-sources defined
		assertTrue(datasources.size() > 1);
		
		for (Datasource datasource : datasources){
			if (datasource.getName().equals("london-borough-profiles")){
				assertEquals("london-borough-profiles.xls", datasource.getLocalDatafile());
			}
		}
	}
}
