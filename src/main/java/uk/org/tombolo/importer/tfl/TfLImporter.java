package uk.org.tombolo.importer.tfl;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.ConfigurationException;

public abstract class TfLImporter extends AbstractImporter {

	protected static final String PROP_API_APP_ID = "apiIdTfl";
	protected static final String PROP_API_APP_KEY = "apiKeyTfl";

	public static final Provider PROVIDER = new Provider(
			"uk.gov.tfl",
			"Transport for London"
			);

	public TfLImporter(Config config) {
		super(config);
	}

	@Override
	public Provider getProvider() {
		return PROVIDER;
	}

	@Override
	public void verifyConfiguration() throws ConfigurationException {
		if (properties.getProperty(PROP_API_APP_ID) == null)
			throw new ConfigurationException("Property "+PROP_API_APP_ID+" not defined");
		if (properties.getProperty(PROP_API_APP_KEY) == null)
			throw new ConfigurationException("Property "+PROP_API_APP_KEY+" not defined");
	}

}
