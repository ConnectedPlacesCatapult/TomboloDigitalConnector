package uk.org.tombolo.importer.londondatastore;

import uk.org.tombolo.core.Provider;
import uk.org.tombolo.importer.AbstractOaImporter;
import uk.org.tombolo.importer.ons.OaImporter;

import java.util.Collections;
import java.util.List;

/**
 * Abstract class for London Datastor importing
 */
public abstract class AbstractLondonDatastoreImporter extends AbstractOaImporter {
    public static final Provider PROVIDER = new Provider(
            "uk.gov.london",
            "London Datastore - Greater London Authority"
    );

    @Override
    public Provider getProvider() {
        return PROVIDER;
    }

    @Override
    protected List<String> getOaDatasourceIds() {
        return Collections.singletonList(OaImporter.OaType.localAuthority.datasourceSpec.getId());
    }

}
