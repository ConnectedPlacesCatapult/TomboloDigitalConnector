package uk.org.tombolo.importer.londondatastore;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.AbstractImporterTestUtils;
import uk.org.tombolo.importer.Importer;

import static org.junit.Assert.assertEquals;

public class LondonDatastoreImporterTestLbp extends AbstractTest {
	public Importer importer;

	@Before
	public void before(){
		importer = new LondonDatastoreImporter();
		AbstractImporterTestUtils.mockDownloadUtils(importer);
	};
	
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
