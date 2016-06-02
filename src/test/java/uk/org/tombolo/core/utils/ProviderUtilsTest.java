package uk.org.tombolo.core.utils;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Provider;

import static org.junit.Assert.*;

public class ProviderUtilsTest extends AbstractTest {
    private static final String TEST_PROVIDER_LABEL = "uk.org.tombolo.test";
    private static final String TEST_PROVIDER_NAME = "Tobmolo Test Provider";
    private static final String TEST_PROVIDER_NAME_UPDATE = "Tobmolo Test Provider 2.0";

    @Test
    public void getTestProvider() throws Exception {
        Provider testProvider = new Provider(TEST_PROVIDER_LABEL, TEST_PROVIDER_NAME);

        ProviderUtils.save(testProvider);
        Provider provider = ProviderUtils.getTestProvider();

        assertTestProvider(provider);
    }

    @Test
    public void saveClone() throws Exception {
        Provider testProvider = new Provider(TEST_PROVIDER_LABEL, TEST_PROVIDER_NAME);
        Provider testProviderClone = new Provider(TEST_PROVIDER_LABEL, TEST_PROVIDER_NAME);

        ProviderUtils.save(testProvider);
        ProviderUtils.save(testProviderClone);

        Provider provider = ProviderUtils.getTestProvider();
        assertTestProvider(provider);
    }

    @Test
    public void update() throws Exception {
        Provider testProvider = new Provider(TEST_PROVIDER_LABEL, TEST_PROVIDER_NAME);
        Provider testProviderUpdate = new Provider(TEST_PROVIDER_LABEL, TEST_PROVIDER_NAME_UPDATE);

        ProviderUtils.save(testProvider);
        ProviderUtils.save(testProviderUpdate);

        Provider provider = ProviderUtils.getTestProvider();
        assertTestProviderUpdate(provider);
    }


    @Test
    public void getByLabel() throws Exception {
        Provider testProvider = new Provider(TEST_PROVIDER_LABEL, TEST_PROVIDER_NAME);

        ProviderUtils.save(testProvider);

        Provider provider = ProviderUtils.getByLabel(TEST_PROVIDER_LABEL);
        assertTestProvider(provider);
    }

    private static void assertTestProvider(Provider provider){
        assertNotNull(provider);
        assertEquals(TEST_PROVIDER_LABEL,provider.getLabel());
        assertEquals(TEST_PROVIDER_NAME,provider.getName());
    }

    private static void assertTestProviderUpdate(Provider provider){
        assertNotNull(provider);
        assertEquals(TEST_PROVIDER_LABEL,provider.getLabel());
        assertEquals(TEST_PROVIDER_NAME_UPDATE,provider.getName());
    }
}