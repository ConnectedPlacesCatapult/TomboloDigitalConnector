package uk.org.tombolo.importer.tfl;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Importer;

import java.io.IOException;

public abstract class TfLImporter extends AbstractImporter implements Importer {

	protected static final String propertiesFile = "/properties/importer/tfl/tfl.properties";
	protected static final String PROP_API_APP_ID = "apiIdTfl";
	protected static final String PROP_API_APP_KEY = "apiKeyTfl";

	public TfLImporter() throws IOException {
		loadProperties(propertiesFile);
	}

	public static final Provider PROVIDER = new Provider(
			"uk.gov.tfl",
			"Transport for London"
			);

	@Override
	public Provider getProvider() {
		return PROVIDER;
	}

	
}
