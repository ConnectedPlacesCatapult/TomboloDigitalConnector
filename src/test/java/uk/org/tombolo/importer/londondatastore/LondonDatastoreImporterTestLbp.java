package uk.org.tombolo.importer.londondatastore;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.importer.Importer;

import static org.junit.Assert.assertEquals;

public class LondonDatastoreImporterTestLbp extends AbstractTest {
	public Importer importer;

	@Before
	public void before(){
		importer = new LondonDatastoreImporter();
		mockDownloadUtils(importer);
	};
	
	@Test
	public void testGetDatasource() throws Exception {
		Datasource datasource = importer.getDatasource("london-borough-profiles");
		
		assertEquals(6, datasource.getTimedValueAttributes().size());
		assertEquals("Population density (per hectare) 2015", 
				datasource.getTimedValueAttributes().get(0).getName());
		assertEquals("% of area that is Greenspace, 2005",
				datasource.getTimedValueAttributes().get(3).getName());
	}
	
	@Test
	public void testImportDatasource() throws Exception{
		int datapoints = importer.importDatasource("london-borough-profiles");
		
		//FIXME: include assertion on datapoints count
	}

}
