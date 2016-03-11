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
		
		assertEquals(1,datasources.size());
		
		Datasource lbp = datasources.get(0);
		assertEquals("london-borough-profiles", lbp.getName());
		assertEquals("london-borough-profiles.xls", lbp.getLocalDatafile());
	}
	
	@Test
	public void testGetDatasource() throws Exception{
		Datasource datasource = importer.getDatasource("london-borough-profiles");
		
		assertEquals(6, datasource.getAttributes().size());
		assertEquals("Population density (per hectare) 2015", 
				datasource.getAttributes().get(0).getName());
		assertEquals("% of area that is Greenspace, 2005",
				datasource.getAttributes().get(3).getName());
	}
	
	@Test
	public void testImportDatasource() throws Exception{
		importer.importDatasource("london-borough-profiles");
	}
}
