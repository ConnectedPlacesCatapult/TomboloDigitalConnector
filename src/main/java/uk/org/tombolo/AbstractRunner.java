package uk.org.tombolo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.recipe.DataExportRecipe;
import uk.org.tombolo.recipe.RecipeDeserializer;
import uk.org.tombolo.importer.ConfigurationException;
import uk.org.tombolo.importer.DownloadUtils;

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
    private static final String API_KEYS_PROPERTY_NAME = "API keys";
    private static final String API_KEYS_FILENAME = "apikeys.properties";
    private static final String SYSTEM_PROPERTIES_PROPERTY_NAME = "System properties";
    private static final String SYSTEM_PROPERTIES_FILENAME = "gradle.properties";
    private static final String FILE_DOWNLOAD_CACHE = "fileDownloadCache";

    protected static Properties loadApiKeys() throws ConfigurationException {
        return loadProperties(API_KEYS_PROPERTY_NAME, API_KEYS_FILENAME);
    }

    public static Properties loadProperties(String propertyName, String propertyFilename) throws ConfigurationException {
        Properties properties;
        try {
            properties = new Properties();
            properties.load(new FileReader(propertyFilename));
        }catch (FileNotFoundException e){
            throw new ConfigurationException("Missing " + propertyName + " file: " + propertyFilename, e);
        } catch (IOException e) {
            throw new ConfigurationException("Could not load " + propertyName + " from file: " + propertyFilename, e);
        }
        return properties;
    }

    protected static DataExportRecipe getSpecification(String specificationPath) throws IOException {
        File file = new File(specificationPath);
        if (!file.exists()){
            log.error("File not found: {}", specificationPath);
            System.exit(1);
        }
        return RecipeDeserializer.fromJsonFile(file, DataExportRecipe.class);
    }

    protected static DataExportRecipe getSpecificationFromString(String specification) throws IOException {
        return RecipeDeserializer.fromJsonString(specification, DataExportRecipe.class);
    }

    protected static DownloadUtils initialiseDowloadUtils() throws ConfigurationException {
        Properties properties = loadProperties(SYSTEM_PROPERTIES_PROPERTY_NAME, SYSTEM_PROPERTIES_FILENAME);
        log.info("Setting file download cache: {}", properties.getProperty(FILE_DOWNLOAD_CACHE));
        DownloadUtils downloadUtils = new DownloadUtils(DownloadUtils.DEFAULT_DATA_CACHE_ROOT);
        if (properties.getProperty(FILE_DOWNLOAD_CACHE) != null)
            downloadUtils = new DownloadUtils(properties.getProperty(FILE_DOWNLOAD_CACHE));
        return downloadUtils;
    }
}
