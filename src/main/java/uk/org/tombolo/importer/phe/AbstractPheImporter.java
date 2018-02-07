package uk.org.tombolo.importer.phe;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.AbstractOaImporter;

/**
 * Abstract importer for all date provided by Public Health England.
 */
public abstract class AbstractPheImporter extends AbstractOaImporter {
    protected static final Provider PROVIDER = new Provider(
            "uk.gov.phe",
            "Public Health England"
    );

    @Override
    public Provider getProvider() {
        return PROVIDER;
    }


}
