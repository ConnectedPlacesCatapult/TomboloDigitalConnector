package uk.org.tombolo.importer;


public abstract class AbstractImporterTestUtils {
	
	public static void mockDownloadUtils(Importer importer){
		// Make the downloader point to the mocked data cache
		DownloadUtils downloadUtils = new DownloadUtils("src/test/resources/datacache");
		importer.setDownloadUtils(downloadUtils);
	}
}
