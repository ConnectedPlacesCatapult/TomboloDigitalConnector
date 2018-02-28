package uk.org.tombolo.importer.dfe;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.AbstractImporter;

/**
 * Abstract class for the school importer indicating the provider
 */
public abstract class AbstractDfEImporter extends AbstractImporter {
    private static final Provider PROVIDER = new Provider("uk.gov.education", "Department for Education");

    public Provider getProvider() {
        return PROVIDER;
    }
}
