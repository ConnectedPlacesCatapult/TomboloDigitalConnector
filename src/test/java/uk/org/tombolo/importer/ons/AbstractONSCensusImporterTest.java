package uk.org.tombolo.importer.ons;

import org.junit.Before;

import uk.org.tombolo.importer.AbstractImporterTestUtils;
import uk.org.tombolo.importer.Importer;

public abstract class AbstractONSCensusImporterTest extends AbstractImporterTestUtils {
	public Importer importer;
	private MockTimedValueUtils mockTimedValueUtils;

	@Before
	public void before(){
		mockTimedValueUtils = new MockTimedValueUtils();
		importer = new ONSCensusImporter(mockTimedValueUtils);
		AbstractImporterTestUtils.mockDownloadUtils(importer);
	}
}
