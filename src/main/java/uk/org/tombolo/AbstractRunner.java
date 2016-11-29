package uk.org.tombolo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.execution.spec.DataExportSpecification;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;
import uk.org.tombolo.importer.ConfigurationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 *
 */
public abstract class AbstractRunner {
    private static final Logger log = LoggerFactory.getLogger(AbstractRunner.class);
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

    protected static DataExportSpecification getSpecification(String specificationPath) throws IOException {
        File file = new File(specificationPath);
        if (!file.exists()){
            log.error("File not found: {}", specificationPath);
            System.exit(1);
        }
        return SpecificationDeserializer.fromJsonFile(file, DataExportSpecification.class);
    }

}
