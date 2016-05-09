package uk.org.tombolo.importer;


import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.util.List;

public abstract class AbstractImporterTestUtils {

	public static class MockTimedValueUtils extends TimedValueUtils {
		public int numberOfSavedRecords = 0;

		public int save(List<TimedValue> timedValues){
			numberOfSavedRecords += timedValues.size();
			return timedValues.size();
		}
	}

	public static void mockDownloadUtils(Importer importer){
		// Make the downloader point to the mocked data cache
		DownloadUtils downloadUtils = new DownloadUtils("src/test/resources/datacache");
		importer.setDownloadUtils(downloadUtils);
	}
}
