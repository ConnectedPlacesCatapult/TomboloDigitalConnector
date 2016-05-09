package uk.org.tombolo.importer.tfl;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.AbstractImporterTestUtils;
import uk.org.tombolo.importer.Importer;

import java.util.List;

public class TfLStationsImporterTest {
	public TfLStationsImporter importer;
	private AbstractImporterTestUtils.MockTimedValueUtils mockTimedValueUtils;

	@Before
	public void before(){
		mockTimedValueUtils = new AbstractImporterTestUtils.MockTimedValueUtils();
		importer = new TfLStationsImporter(mockTimedValueUtils);
		AbstractImporterTestUtils.mockDownloadUtils(importer);
	}
	
	@Test
	public void testImportDatasource() throws Exception {
		int count = importer.importDatasource("StationList");
		assertEquals(302, count);
	}	
}
