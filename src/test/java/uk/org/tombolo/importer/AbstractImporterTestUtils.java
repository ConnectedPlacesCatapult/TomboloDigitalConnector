package uk.org.tombolo.importer;

import uk.org.tombolo.AbstractTest;

public abstract class AbstractImporterTestUtils extends AbstractTest {

	public static void mockDownloadUtils(Importer importer){
		// Make the downloader point to the mocked data cache
		importer.setDownloadUtils(makeTestDownloadUtils());
	}
}
