package uk.org.tombolo.importer.londondatastore;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.utils.TimedValueUtils;

public class LondonDatastoreImporterBaW {
	private static final String DATASOURCE_ID = "walking-cycling-borough";
	LondonDatastoreImporter importer = new LondonDatastoreImporter(new TimedValueUtils());
	
	@Test
	public void testGetDatasource() throws Exception {
		Datasource datasource = importer.getDatasource(DATASOURCE_ID);
		
		List<Attribute> attributes = datasource.getAttributes();
		assertEquals(2, attributes.size());
	}
	
	@Test
	public void testImportDatasource() throws Exception{
		int datapoints = importer.importDatasource(DATASOURCE_ID);
		
		assertEquals(2*33*4, datapoints);
	}
}
