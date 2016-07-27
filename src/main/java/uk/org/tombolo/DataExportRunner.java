package uk.org.tombolo;

import com.github.fge.jsonschema.core.report.ProcessingReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.utils.HibernateUtil;
import uk.org.tombolo.execution.spec.DataExportSpecification;
import uk.org.tombolo.execution.spec.DataExportSpecificationValidator;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;
import uk.org.tombolo.importer.ConfigurationException;
import uk.org.tombolo.importer.DownloadUtils;

import java.io.*;
import java.util.Properties;

public class DataExportRunner {
    private static final Logger log = LoggerFactory.getLogger(DataExportRunner.class);
    // FIXME: At some point we might want to make this configurable
    private static final String API_KEYS_FILENAME = "apikeys.properties";

    public static void main(String[] args) throws Exception {
        validateArguments(args);

        String executionSpecPath = args[0];
        String outputFile = args[1];
        Boolean clearDatabaseCache = Boolean.parseBoolean(args[2]);

        HibernateUtil.startup();

        // Load API keys
        Properties apiKeys;
        try {
            apiKeys = new Properties();
            apiKeys.load(new FileReader(API_KEYS_FILENAME));
        }catch (FileNotFoundException e){
            throw new ConfigurationException("Missing API keys file: " + API_KEYS_FILENAME, e);
        }

        // Create engine
        DataExportEngine engine = new DataExportEngine(apiKeys, new DownloadUtils());

        validateSpecification(executionSpecPath);

        try (Writer writer = getOutputWriter(outputFile)) {
            engine.execute(getSpecification(executionSpecPath), writer, clearDatabaseCache);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // FIXME: This method should either be responsible for the entire state of HibernateUtil, or not at all.
            // e.g. it should set it up too, or it shouldn't have to shut it down.
            HibernateUtil.shutdown();
        }
    }

    private static void validateSpecification(String executionSpecPath) throws FileNotFoundException {
        ProcessingReport report = DataExportSpecificationValidator.validate(new FileReader(executionSpecPath));
        if (!report.isSuccess()) {
            DataExportSpecificationValidator.display(report);
            System.exit(1);
        }
    }

    private static DataExportSpecification getSpecification(String specificationPath) throws IOException {
        File file = new File(specificationPath);
        if (!file.exists()){
            log.error("File not found: {}", specificationPath);
            System.exit(1);
        }
        return SpecificationDeserializer.fromJsonFile(file, DataExportSpecification.class);
    }

    private static void validateArguments(String[] args) {
        if (args.length != 3){
            log.error("Use: {} {} {} {}",
                    DataExportRunner.class.getCanonicalName(),
                    "dataExportSpecFile",
                    "outputFile",
                    "clearDatabaseCache"
            );
            System.exit(1);
        }
    }

    private static Writer getOutputWriter(String path) {
        try {
            return new FileWriter(path);
        } catch (IOException e) {
            log.error("Error initialising output writer: {}", path);
            System.exit(1);
            return null;
        }
    }
}
