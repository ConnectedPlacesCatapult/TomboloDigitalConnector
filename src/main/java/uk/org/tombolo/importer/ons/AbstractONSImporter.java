package uk.org.tombolo.importer.ons;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Importer;

public abstract class AbstractONSImporter extends AbstractImporter implements Importer {
	public static final Provider PROVIDER = new Provider(
			"uk.gov.ons",
			"Office for National Statistics"
			);

	@Override
	public Provider getProvider() {
		return PROVIDER;
	}
	
}
