package uk.org.tombolo;

import com.github.fge.jsonschema.core.report.ProcessingReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.utils.DatabaseUtils;
import uk.org.tombolo.core.utils.HibernateUtil;
import uk.org.tombolo.execution.DataExportEngine;
import uk.org.tombolo.recipe.DataExportRecipeValidator;
import uk.org.tombolo.importer.ImporterMatcher;

import java.io.*;
import java.util.Properties;

public class DataExportRunner extends AbstractRunner {
    private static final Logger log = LoggerFactory.getLogger(DataExportRunner.class);
    public static String executionSpecPath = "";

    public static void main(String[] args) throws Exception {
        validateArguments(args);

        executionSpecPath = args[0];
        String outputFile = args[1];
        String forceImports = args[2];
        Boolean clearDatabaseCache = Boolean.parseBoolean(args[3]);
        String executionSpec = args[4];

        run(executionSpecPath, outputFile, forceImports, clearDatabaseCache, executionSpec);
    }

    protected static void run(String executionSpecPath, String outputFile,
                              String forceImports, Boolean clearDatabaseCache) throws Exception {
    
        run(executionSpecPath, outputFile, forceImports, clearDatabaseCache, "None");
    }

    protected static void run(String executionSpecPath, String outputFile,
                              String forceImports, Boolean clearDatabaseCache, String executionSpec) throws Exception {
        HibernateUtil.startup();
        if (clearDatabaseCache) {
            DatabaseUtils.clearAllData();
        }

        // Load API keys
        Properties apiKeys = loadApiKeys();

        // Create engine
        DataExportEngine engine = new DataExportEngine(apiKeys, initialiseDowloadUtils());

        if (!executionSpecPath.equalsIgnoreCase("None")) validateSpecification(executionSpecPath);
        if (!executionSpec.equalsIgnoreCase("None")) validateSpecificationOfString(executionSpec);

        try (Writer writer = getOutputWriter(outputFile)) {
            engine.execute(
                    !executionSpecPath.equalsIgnoreCase("None") ? getSpecification(executionSpecPath) : 
                                                                    getSpecificationFromString(executionSpec),
                    writer,
                    new ImporterMatcher(forceImports.trim())
            );
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }

    private static void validateSpecification(String executionSpecPath) throws FileNotFoundException {
        File file = new File(executionSpecPath);
        if (!file.exists()) {
            log.error("File not found: {}", executionSpecPath);
            System.exit(1);
        }

        ProcessingReport report = DataExportRecipeValidator.validate(new FileReader(file));
        if (!report.isSuccess()) {
            DataExportRecipeValidator.display(report);
            System.exit(1);
        }
    }

    private static void validateSpecificationOfString(String executionSpec) {
        ProcessingReport report = DataExportRecipeValidator.validate(new BufferedReader(new InputStreamReader(
                                                                        new ByteArrayInputStream(executionSpec.getBytes()))));
        if (!report.isSuccess()) {
            DataExportRecipeValidator.display(report);
            System.exit(1);
        }
    }

    private static void validateArguments(String[] args) {
        if (args.length != 5){
            log.error("Use: {} {} {} {} {}",
                    DataExportRunner.class.getCanonicalName(),
                    "dataExportSpecFile",
                    "outputFile",
                    "clearDatabaseCache",
                    "forceImports (className:datasourceId,...)",
                    "dataExportSpec"
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
