package uk.org.tombolo.importer.ons;

import org.junit.Before;

import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.AbstractImporterTestUtils;
import uk.org.tombolo.importer.Importer;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractONSCensusImporterTest extends AbstractImporterTestUtils {
	public Importer importer;
	private TimedValueUtils mockTimedValueUtils;

	@Before
	public void before(){
		mockTimedValueUtils = mock(TimedValueUtils.class);
		when(mockTimedValueUtils.save(anyListOf(TimedValue.class))).thenAnswer(AbstractImporterTestUtils.listLengthAnswer);
		importer = new ONSCensusImporter(mockTimedValueUtils);
		AbstractImporterTestUtils.mockDownloadUtils(importer);
	}
}
