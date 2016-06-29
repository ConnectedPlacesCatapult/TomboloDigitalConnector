package uk.org.tombolo.importer.ons;

import org.junit.Before;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.AbstractImporterTestUtils;
import uk.org.tombolo.importer.Importer;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractONSCensusImporterTest extends AbstractImporterTestUtils {
	public Importer importer;
	private TimedValueUtils mockTimedValueUtils;

	@Before
	public void before() throws IOException {
		mockTimedValueUtils = mock(TimedValueUtils.class);
		when(mockTimedValueUtils.save(anyListOf(TimedValue.class))).thenAnswer(AbstractImporterTestUtils.listLengthAnswer);
		importer = new ONSCensusImporter();
		importer.setTimedValueUtils(mockTimedValueUtils);

		// Load api keys
		Properties properties = new Properties();
		properties.load(new FileReader(AbstractImporterTestUtils.getApiKeysLocation()));
		importer.configure(properties);

		AbstractImporterTestUtils.mockDownloadUtils(importer);
	}
}
