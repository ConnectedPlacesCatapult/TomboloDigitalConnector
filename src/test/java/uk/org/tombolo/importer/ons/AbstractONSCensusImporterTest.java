package uk.org.tombolo.importer.ons;

import org.junit.Before;
import uk.org.tombolo.importer.AbstractImporterTestUtils;
import uk.org.tombolo.importer.Importer;

public abstract class AbstractONSCensusImporterTest extends AbstractImporterTestUtils {
	public Importer importer;

	@Before
	public void before() throws Exception {
		importer = new ONSCensusImporter();
		AbstractImporterTestUtils.mockDownloadUtils(importer);
		importer.configure(makeApiKeyProperties());
	}
}
