package uk.org.tombolo.importer.londondatastore;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.AbstractImporterTestUtils;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.ons.ONSCensusImporter;

public class LondonDatastoreImporterTest {
	public Importer importer;
	private TimedValueUtils mockTimedValueUtils;

	@Before
	public void before(){
		mockTimedValueUtils = mock(TimedValueUtils.class);
		when(mockTimedValueUtils.save(anyListOf(TimedValue.class))).thenAnswer(AbstractImporterTestUtils.listLengthAnswer);
		importer = new ONSCensusImporter(mockTimedValueUtils);
		AbstractImporterTestUtils.mockDownloadUtils(importer);
	}
	
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
