package uk.org.tombolo.importer.ons;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.Importer;

public abstract class AbstractONSImporter extends AbstractImporter implements Importer {
	public static final String PROP_ONS_API_KEY = "apiKeyOns";
	
	public static final Provider PROVIDER = new Provider(
			"uk.gov.ons",
			"Office for National Statistics"
			);

	public AbstractONSImporter(Config config) {
		super(config);
	}

	@Override
	public Provider getProvider() {
		return PROVIDER;
	}
	
}
