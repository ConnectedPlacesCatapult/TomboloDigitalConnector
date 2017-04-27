package uk.org.tombolo.importer.londondatastore;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.Importer;

/**
 * Abstract class for London Datastor importing
 */
public abstract class AbstractLondonDatastoreImporter extends AbstractImporter implements Importer {
    public static final Provider PROVIDER = new Provider(
            "uk.gov.london",
            "London Datastore - Greater London Authority"
    );

    public AbstractLondonDatastoreImporter(Config config) {
        super(config);
    }

    @Override
    public Provider getProvider() {
        return PROVIDER;
    }

}
