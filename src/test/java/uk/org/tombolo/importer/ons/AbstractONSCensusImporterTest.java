package uk.org.tombolo.importer.ons;

import org.junit.Before;
import uk.org.tombolo.AbstractTest;

public abstract class AbstractONSCensusImporterTest extends AbstractTest {
	public ONSCensusImporter importer;

	@Before
	public void before() throws Exception {
		importer = new ONSCensusImporter();
		mockDownloadUtils(importer);
		importer.configure(makeApiKeyProperties());
	}
}
