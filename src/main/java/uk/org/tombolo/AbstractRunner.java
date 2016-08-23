package uk.org.tombolo;

import uk.org.tombolo.importer.ConfigurationException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 *
 */
public abstract class AbstractRunner {
    // FIXME: At some point we might want to make this configurable
    private static final String API_KEYS_FILENAME = "apikeys.properties";

    protected static Properties loadApiKeys() throws ConfigurationException {
        Properties apiKeys;
        try {
            apiKeys = new Properties();
            apiKeys.load(new FileReader(API_KEYS_FILENAME));
        }catch (FileNotFoundException e){
            throw new ConfigurationException("Missing API keys file: " + API_KEYS_FILENAME, e);
        } catch (IOException e) {
            throw new ConfigurationException("Could not load API keys from file: " + API_KEYS_FILENAME, e);
        }
        return apiKeys;
    }
}
