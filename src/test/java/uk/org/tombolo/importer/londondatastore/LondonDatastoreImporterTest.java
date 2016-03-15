package uk.org.tombolo.importer.londondatastore;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import uk.org.tombolo.core.Attribute;
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
	
	@Test
	public void testGetDatasourceLbp() throws Exception {
		Datasource datasource = importer.getDatasource("london-borough-profiles");
		
		assertEquals(6, datasource.getAttributes().size());
		assertEquals("Population density (per hectare) 2015", 
				datasource.getAttributes().get(0).getName());
		assertEquals("% of area that is Greenspace, 2005",
				datasource.getAttributes().get(3).getName());
	}
	
	@Test
	public void testGetDatasourcePhof() throws Exception {
		Datasource datasource = importer.getDatasource("phof-indicators-data-london-borough");
		
		List<Attribute> attributes = datasource.getAttributes();
		assertEquals(150, attributes.size());

		PHOFLabelExtractor extractor = new PHOFLabelExtractor();

		
		Attribute a5 = attributes.get(4);
		String a5Name = "1.02ii - School Readiness: The percentage of Year 1 pupils achieving the expected level in the phonics screening check";
		assertEquals(importer.getProvider(), a5.getProvider());
		assertEquals(extractor.extractLabel(a5Name), a5.getLabel());
		assertEquals(a5Name, a5.getName());
		assertEquals(a5Name, a5.getDescription());

		Attribute a135 = attributes.get(134);
		String a135Name = "Supporting Information - Deprivation score (IMD 2010)";
		assertEquals(importer.getProvider(), a135.getProvider());
		assertEquals(extractor.extractLabel(a135Name), a135.getLabel());
		assertEquals(a135Name, a135.getName());
		assertEquals(a135Name, a135.getDescription());
	}
	
	@Test
	public void testImportDatasourceLBP() throws Exception{
		importer.importDatasource("london-borough-profiles");
	}
	
	@Test
	public void testImportDatasourcePhof() throws Exception{
		importer.importDatasource("phof-indicators-data-london-borough");
	}
}
