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

public class LondonDatastoreImporterTestPhof {
	private static final String DATASOURCE_ID = "phof-indicators-data-london-borough";
	private LondonDatastoreImporter importer;
	private TimedValueUtils mockTimedValueUtils;

	@Before
	public void before(){
		mockTimedValueUtils = mock(TimedValueUtils.class);
		when(mockTimedValueUtils.save(anyListOf(TimedValue.class))).thenAnswer(AbstractImporterTestUtils.listLengthAnswer);
		importer = new LondonDatastoreImporter(mockTimedValueUtils);
		AbstractImporterTestUtils.mockDownloadUtils(importer);
	}

	@Test
	public void testGetDatasource() throws Exception {
		Datasource datasource = importer.getDatasource(DATASOURCE_ID);
		
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
	public void testImportDatasource() throws Exception{
		int datapoints = importer.importDatasource(DATASOURCE_ID);
		assertEquals(30908, datapoints);
	}
	
}
