package uk.org.tombolo;

import com.github.fge.jsonschema.core.report.ProcessingReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.importer.ConfigurationException;
import uk.org.tombolo.importer.DownloadUtils;
import uk.org.tombolo.recipe.DataExportRecipe;
import uk.org.tombolo.recipe.DataExportRecipeValidator;
import uk.org.tombolo.recipe.RecipeDeserializer;

import java.io.*;
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

    protected Properties loadApiKeys() throws ConfigurationException {
        return loadProperties(API_KEYS_PROPERTY_NAME, API_KEYS_FILENAME);
    }

    public static Properties loadProperties(String propertyName, String propertyFilename) throws ConfigurationException {
        Properties properties;
        try {
            properties = new Properties();
            properties.load(new FileReader(propertyFilename));
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Missing " + propertyName + " file: " + propertyFilename, e);
        } catch (IOException e) {
            throw new ConfigurationException("Could not load " + propertyName + " from file: " + propertyFilename, e);
        }
        return properties;
    }

    protected DataExportRecipe getRecipe(String recipe, boolean isString) throws IOException {
        validateRecipe(recipe, isString);

        if (isString) {
            return RecipeDeserializer.fromJsonString(recipe, DataExportRecipe.class);
        }
        File file = new File(recipe);
        if (!file.exists()) {
            log.error("Could not find recipe file: {}", recipe);
            System.exit(1);
        }
        return RecipeDeserializer.fromJsonFile(file, DataExportRecipe.class);
    }

    protected DownloadUtils initialiseDowloadUtils() throws ConfigurationException {
        Properties properties = loadProperties(SYSTEM_PROPERTIES_PROPERTY_NAME, SYSTEM_PROPERTIES_FILENAME);
        log.info("Setting file download cache: {}", properties.getProperty(FILE_DOWNLOAD_CACHE));
        DownloadUtils downloadUtils = new DownloadUtils(DownloadUtils.DEFAULT_DATA_CACHE_ROOT);
        if (properties.getProperty(FILE_DOWNLOAD_CACHE) != null)
            downloadUtils = new DownloadUtils(properties.getProperty(FILE_DOWNLOAD_CACHE));
        return downloadUtils;
    }

    private void validateRecipe(String recipe, boolean isString) throws FileNotFoundException {
        ProcessingReport report = DataExportRecipeValidator.validate(!isString? new FileReader(recipe) :
                new BufferedReader(new InputStreamReader(new ByteArrayInputStream(recipe.getBytes()))));
        if (!report.isSuccess()) {
            DataExportRecipeValidator.display(report);
            System.exit(1);
        }
    }

    protected Writer getOutputWriter(String path) {
        try {
            return new FileWriter(path);
        } catch (IOException e) {
            log.error("Error initialising output writer: {}", path);
            System.exit(1);
            return null;
        }
    }
}
