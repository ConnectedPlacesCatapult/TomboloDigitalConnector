package uk.org.tombolo.importer.dclg;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.AbstractOaImporter;

public abstract class AbstractDCLGImporter extends AbstractOaImporter {
    public static final Provider PROVIDER
            = new Provider("uk.gov.dclg", "Department for Communities and Local Government");

    @Override
    public Provider getProvider() {
        return PROVIDER;
    }

}
