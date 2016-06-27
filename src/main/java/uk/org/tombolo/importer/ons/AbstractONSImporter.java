package uk.org.tombolo.importer.ons;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Importer;

import java.io.IOException;

public abstract class AbstractONSImporter extends AbstractImporter implements Importer {
	protected static final String propertiesFile = "/properties/importer/ons/ons.properties";
	public static final String PROP_ONS_API_KEY = "apiKeyOns";

	public AbstractONSImporter() throws IOException{
		loadProperties(propertiesFile);
	}

	public static final Provider PROVIDER = new Provider(
			"uk.gov.ons",
			"Office for National Statistics"
			);

	@Override
	public Provider getProvider() {
		return PROVIDER;
	}
	
}
