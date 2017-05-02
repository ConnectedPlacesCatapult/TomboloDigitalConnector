package uk.org.tombolo.importer.dfe;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.GeneralImporter;

/**
 * Abstract class for the school importer indicating the provider
 */
public abstract class AbstractDfEImporter extends GeneralImporter {
    private static final Provider PROVIDER = new Provider("uk.gov.education", "Department for Education");

    public AbstractDfEImporter(Config config) {
        super(config);
    }

    public Provider getProvider() {
        return PROVIDER;
    }
}
