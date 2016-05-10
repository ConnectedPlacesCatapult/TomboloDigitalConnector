package uk.org.tombolo.importer.londondatastore;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.AbstractImporterTestUtils;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.ons.ONSCensusImporter;

public class LondonDatastoreImporterBaW {
	private static final String DATASOURCE_ID = "walking-cycling-borough";
	public Importer importer;
	private TimedValueUtils mockTimedValueUtils;

	@Before
	public void before(){
		mockTimedValueUtils = mock(TimedValueUtils.class);
		when(mockTimedValueUtils.save(anyListOf(TimedValue.class))).thenAnswer(AbstractImporterTestUtils.listLengthAnswer);
		importer = new LondonDatastoreImporter(mockTimedValueUtils);
		AbstractImporterTestUtils.mockDownloadUtils(importer);
	};
	
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
