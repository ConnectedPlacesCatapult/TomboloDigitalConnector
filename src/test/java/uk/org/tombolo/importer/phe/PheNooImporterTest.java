package uk.org.tombolo.importer.phe;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;

public class PheNooImporterTest {
	private static final String DATASOURCE_ID = "BMI_categories_2012-2014";
	PheNooImporter importer = new PheNooImporter();
	
	@Test
	public void testGetDatasource() throws Exception {
		Datasource datasource = importer.getDatasource(DATASOURCE_ID);
		
		List<Attribute> attributes = datasource.getAttributes();
		assertEquals(5, attributes.size());
	}
	
	@Test
	public void testImportDatasource() throws Exception{
		int datapoints = importer.importDatasource(DATASOURCE_ID);
		
		//FIXME: Find a way to match Gateshead etc.
		assertEquals(5*(174-24)*1, datapoints);
	}
}
