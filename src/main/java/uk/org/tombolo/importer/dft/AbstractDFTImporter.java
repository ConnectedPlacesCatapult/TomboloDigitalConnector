package uk.org.tombolo.importer.dft;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Importer;

public abstract class AbstractDFTImporter extends AbstractImporter implements Importer {
    public static final Provider PROVIDER = new Provider(
            "uk.gov.dft",
            "Department for Transport"
    );

    @Override
    public Provider getProvider() {
        return PROVIDER;
    }
}
