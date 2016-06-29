package uk.org.tombolo.importer.tfl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.AbstractImporterTestUtils;
import uk.org.tombolo.importer.ConfigurationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TfLStationsImporterTest extends AbstractTest {
	public TfLStationsImporter importer;
	private TimedValueUtils mockTimedValueUtils;

	@Before
	public void before() throws IOException {
		mockTimedValueUtils = mock(TimedValueUtils.class);
		when(mockTimedValueUtils.save(anyListOf(TimedValue.class))).thenAnswer(AbstractImporterTestUtils.listLengthAnswer);
		importer = new TfLStationsImporter();
		importer.setTimedValueUtils(mockTimedValueUtils);

		// Load api keys
		Properties properties = new Properties();
		properties.load(new FileReader(AbstractImporterTestUtils.getApiKeysLocation()));
		importer.configure(properties);

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

		try {
			Datasource datasource = importer.getDatasource(TfLStationsImporter.DatasourceId.StationList.name());
			assertTrue("Expected exception not thrown", false);
		}catch (ConfigurationException e){
			assertEquals("Property apiIdTfl not defined", e.getMessage());
		}

		Properties properties = new Properties();
		properties.put(TfLImporter.PROP_API_APP_ID, "dummy id");

		importer.configure(properties);
		try {
			Datasource datasource = importer.getDatasource(TfLStationsImporter.DatasourceId.StationList.name());
			assertTrue("Expected exception not thrown", false);
		}catch (ConfigurationException e){
			assertEquals("Property apiKeyTfl not defined", e.getMessage());
		}
	}
}
