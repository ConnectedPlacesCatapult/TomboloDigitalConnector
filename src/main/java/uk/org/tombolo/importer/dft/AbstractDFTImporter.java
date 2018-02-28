package uk.org.tombolo.importer.dft;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.AbstractOaImporter;

public abstract class AbstractDFTImporter extends AbstractOaImporter {
    public static final Provider PROVIDER = new Provider(
            "uk.gov.dft",
            "Department for Transport"
    );

    @Override
    public Provider getProvider() {
        return PROVIDER;
    }
}
