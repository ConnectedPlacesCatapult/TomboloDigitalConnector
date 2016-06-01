package uk.org.tombolo.importer.londondatastore;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.AbstractImporterTestUtils;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.ons.ONSCensusImporter;

public class LondonDatastoreImporterTestLbp extends AbstractTest {
	public Importer importer;
	private TimedValueUtils mockTimedValueUtils;

	@Before
	public void before(){
		mockTimedValueUtils = mock(TimedValueUtils.class);
		when(mockTimedValueUtils.save(anyListOf(TimedValue.class))).thenAnswer(AbstractImporterTestUtils.listLengthAnswer);
		importer = new LondonDatastoreImporter();
		importer.setTimedValueUtils(mockTimedValueUtils);
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
