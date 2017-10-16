package uk.org.tombolo.importer.ons;

import org.junit.Before;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;

public abstract class AbstractONSCensusImporterTest extends AbstractTest {
	public CensusImporter importer;

	@Before
	public void before() throws Exception {
		importer = new CensusImporter(TestFactory.DEFAULT_CONFIG);
		mockDownloadUtils(importer);
		importer.configure(makeApiKeyProperties());
	}
}
