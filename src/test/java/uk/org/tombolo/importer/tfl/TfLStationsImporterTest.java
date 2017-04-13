package uk.org.tombolo.importer.tfl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.importer.ConfigurationException;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Using the following test data files:
 *
 * Local: aHR0cHM6Ly9kYXRhLnRmbC5nb3YudWsvdGZsL3N5bmRpY2F0aW9uL2ZlZWRzL3N0YXRpb25zLWZhY2lsaXRpZXMueG1sP2FwcF9pZD10ZmxBcHBJZFRlc3QmYXBwX2tleT10ZmxBcHBLZXlUZXN0.xml
 */
public class TfLStationsImporterTest extends AbstractTest {
	public TfLStationsImporter importer;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void before() throws Exception {
		importer = new TfLStationsImporter();
		mockDownloadUtils(importer);
		importer.configure(makeApiKeyProperties());
	}
	
	@Test
	public void testImportDatasource() throws Exception {
		importer.importDatasource(TfLStationsImporter.DatasourceId.StationList.name());
		assertEquals(302, importer.getTimedValueCount());	// FIXME: Double check this when we get to refactor importer
	}

	@Test
	public void testLoadingOfProperties() throws Exception {
		assertEquals("tflAppIdTest",importer.getConfiguration().getProperty(TfLImporter.PROP_API_APP_ID));
		assertEquals("tflAppKeyTest",importer.getConfiguration().getProperty(TfLImporter.PROP_API_APP_KEY));
	}

	@Test
	public void testNonConfigured() throws Exception {
		importer = new TfLStationsImporter();
		mockDownloadUtils(importer);

		thrown.expect(ConfigurationException.class);
		thrown.expectMessage("Property apiIdTfl not defined");
		importer.configure(new Properties());
	}

	@Test
	public void testPartiallyConfigured() throws Exception  {
		Properties properties = new Properties();
		properties.put(TfLImporter.PROP_API_APP_ID, "dummy id");

		importer = new TfLStationsImporter();
		mockDownloadUtils(importer);

		thrown.expect(ConfigurationException.class);
		thrown.expectMessage("Property apiKeyTfl not defined");
		importer.configure(properties);
	}
}
