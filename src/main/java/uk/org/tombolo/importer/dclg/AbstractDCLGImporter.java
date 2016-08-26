package uk.org.tombolo.importer.dclg;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Importer;

/**
 *
 */
public abstract class AbstractDCLGImporter extends AbstractImporter implements Importer{
    public static final Provider PROVIDER
            = new Provider("uk.gov.dclg", "Department for Communities and Local Government");

    @Override
    public Provider getProvider() {
        return PROVIDER;
    }

}
