package uk.org.tombolo.importer.londondatastore;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;

public class LondonDatastoreImporterLsoaAtlas {
	private static final String DATASOURCE_ID = "lsoa-atlas";
	LondonDatastoreImporter importer = new LondonDatastoreImporter();
	
	@Test
	public void testGetDatasource() throws Exception {
		Datasource datasource = importer.getDatasource(DATASOURCE_ID);
		
		List<Attribute> attributes = datasource.getAttributes();
		assertEquals(3, attributes.size());
	}
	
	@Test
	public void testImportDatasource() throws Exception{
		int datapoints = importer.importDatasource(DATASOURCE_ID);
		
		assertEquals(4835*4, datapoints);
	}
}
