package uk.org.tombolo.importer.londondatastore;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.org.tombolo.core.Datasource;

public class LondonDatastoreImporterTestLbp {

	LondonDatastoreImporter importer = new LondonDatastoreImporter();
	
	@Test
	public void testGetDatasource() throws Exception {
		Datasource datasource = importer.getDatasource("london-borough-profiles");
		
		assertEquals(6, datasource.getAttributes().size());
		assertEquals("Population density (per hectare) 2015", 
				datasource.getAttributes().get(0).getName());
		assertEquals("% of area that is Greenspace, 2005",
				datasource.getAttributes().get(3).getName());
	}
	
	@Test
	public void testImportDatasource() throws Exception{
		int datapoints = importer.importDatasource("london-borough-profiles");
		
		//FIXME: include assertion on datapoints count
	}

}
