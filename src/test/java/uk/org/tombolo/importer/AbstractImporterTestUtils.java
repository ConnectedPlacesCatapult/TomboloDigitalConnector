package uk.org.tombolo.importer;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.TimedValue;

import java.util.ArrayList;

public abstract class AbstractImporterTestUtils extends AbstractTest {

	public static Answer listLengthAnswer = new Answer() {
		@Override
		public Object answer(InvocationOnMock invocation) throws Throwable {
			return ((ArrayList<TimedValue>) invocation.getArguments()[0]).size();
		}
	};

	public static void mockDownloadUtils(Importer importer){
		// Make the downloader point to the mocked data cache
		importer.setDownloadUtils(makeTestDownloadUtils());
	}
}
