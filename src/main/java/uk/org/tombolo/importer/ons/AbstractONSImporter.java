package uk.org.tombolo.importer.ons;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.AbstractOaImporter;

public abstract class AbstractONSImporter extends AbstractOaImporter {
	public static final String PROP_ONS_API_KEY = "apiKeyOns";
	
	public static final Provider PROVIDER = new Provider(
			"uk.gov.ons",
			"Office for National Statistics"
			);

	@Override
	public Provider getProvider() {
		return PROVIDER;
	}
	
}
