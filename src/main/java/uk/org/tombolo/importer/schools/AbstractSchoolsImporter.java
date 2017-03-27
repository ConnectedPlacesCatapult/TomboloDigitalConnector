package uk.org.tombolo.importer.schools;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Importer;

/**
 * Abstract class fot the school importer indicating the provider
 */
public abstract class AbstractSchoolsImporter extends XLSImporter {
    private static final Provider PROVIDER = new Provider("uk.gov.education", "Department of Education");

    public Provider getProvider() {
        return PROVIDER;
    }
}
