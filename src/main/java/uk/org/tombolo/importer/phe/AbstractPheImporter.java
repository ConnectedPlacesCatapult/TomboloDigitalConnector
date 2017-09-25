package uk.org.tombolo.importer.phe;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Config;

/**
 * Abstract importer for all date provided by Public Health England.
 */
public abstract class AbstractPheImporter extends AbstractImporter {
    protected static final Provider PROVIDER = new Provider(
            "uk.gov.phe",
            "Public Health England"
    );

    public AbstractPheImporter(Config config) {
        super(config);
    }

    @Override
    public Provider getProvider() {
        return PROVIDER;
    }


}
