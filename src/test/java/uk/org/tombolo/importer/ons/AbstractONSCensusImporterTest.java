package uk.org.tombolo.importer.ons;

import org.junit.Before;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.importer.Importer;

public abstract class AbstractONSCensusImporterTest extends AbstractTest {
	public Importer importer;

	@Before
	public void before() throws Exception {
		importer = new ONSCensusImporter();
		mockDownloadUtils(importer);
		importer.configure(makeApiKeyProperties());
	}
}
