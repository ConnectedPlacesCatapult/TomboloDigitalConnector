package uk.org.tombolo.importer.tfl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.AbstractImporterTestUtils;
import uk.org.tombolo.importer.ConfigurationException;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TfLStationsImporterTest extends AbstractTest {
	public TfLStationsImporter importer;
	private TimedValueUtils mockTimedValueUtils;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void before() throws Exception {
		mockTimedValueUtils = mock(TimedValueUtils.class);
		when(mockTimedValueUtils.save(anyListOf(TimedValue.class))).thenAnswer(AbstractImporterTestUtils.listLengthAnswer);
		importer = new TfLStationsImporter();
		importer.setTimedValueUtils(mockTimedValueUtils);

		// Load api keys
		importer.configure(makeApiKeyProperties());

		AbstractImporterTestUtils.mockDownloadUtils(importer);
	}
	
	@Test
	public void testImportDatasource() throws Exception {
		int count = importer.importDatasource(TfLStationsImporter.DatasourceId.StationList.name());
		assertEquals(302, count);
	}

	@Test
	public void testLoadingOfProperties() throws Exception {
		assertEquals("tflAppIdTest",importer.getConfiguration().getProperty(TfLImporter.PROP_API_APP_ID));
		assertEquals("tflAppKeyTest",importer.getConfiguration().getProperty(TfLImporter.PROP_API_APP_KEY));
	}

	@Test
	public void testNonConfigured() throws Exception {
		mockTimedValueUtils = mock(TimedValueUtils.class);
		when(mockTimedValueUtils.save(anyListOf(TimedValue.class))).thenAnswer(AbstractImporterTestUtils.listLengthAnswer);
		importer = new TfLStationsImporter();
		importer.setTimedValueUtils(mockTimedValueUtils);

		AbstractImporterTestUtils.mockDownloadUtils(importer);
		thrown.expect(ConfigurationException.class);
		thrown.expectMessage("Property apiIdTfl not defined");
		importer.configure(new Properties());
	}

	@Test
	public void testPartiallyConfigured() throws Exception  {
		Properties properties = new Properties();
		properties.put(TfLImporter.PROP_API_APP_ID, "dummy id");

		mockTimedValueUtils = mock(TimedValueUtils.class);
		when(mockTimedValueUtils.save(anyListOf(TimedValue.class))).thenAnswer(AbstractImporterTestUtils.listLengthAnswer);
		importer = new TfLStationsImporter();
		importer.setTimedValueUtils(mockTimedValueUtils);

		AbstractImporterTestUtils.mockDownloadUtils(importer);
		thrown.expect(ConfigurationException.class);
		thrown.expectMessage("Property apiKeyTfl not defined");
		importer.configure(properties);
	}
}
