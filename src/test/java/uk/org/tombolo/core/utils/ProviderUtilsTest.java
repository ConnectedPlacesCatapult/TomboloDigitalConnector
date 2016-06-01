package uk.org.tombolo.core.utils;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Provider;

import static org.junit.Assert.*;

public class ProviderUtilsTest extends AbstractTest {
    private static final String TEST_PROVIDER_LABEL = "uk.org.tombolo.test";
    private static final String TEST_PROVIDER_NAME = "Tobmolo Test Provider";
    private static final Provider TEST_PROVIDER_1 = new Provider(TEST_PROVIDER_LABEL, TEST_PROVIDER_NAME);
    private static final Provider TEST_PROVIDER_2 = new Provider(TEST_PROVIDER_LABEL, TEST_PROVIDER_NAME);


    @Test
    public void getTestProvider() throws Exception {
        ProviderUtils.save(TEST_PROVIDER_1);
        Provider provider = ProviderUtils.getTestProvider();
        assertTestProvider(provider);
    }

    @Test
    public void save() throws Exception {
        ProviderUtils.save(TEST_PROVIDER_1);
        ProviderUtils.save(TEST_PROVIDER_2);
    }

    @Test
    public void getByLabel() throws Exception {
        ProviderUtils.save(TEST_PROVIDER_1);
        Provider provider = ProviderUtils.getByLabel(TEST_PROVIDER_LABEL);
        assertTestProvider(provider);
    }

    private static void assertTestProvider(Provider provider){
        assertNotNull(provider);
        assertEquals(TEST_PROVIDER_LABEL,provider.getLabel());
        assertEquals(TEST_PROVIDER_NAME,provider.getName());
    }

}