package uk.org.tombolo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.utils.HibernateUtil;
import uk.org.tombolo.execution.spec.DataExportSpecification;

import java.io.*;

public class DataExportRunner {
    private static final Logger log = LoggerFactory.getLogger(DataExportRunner.class);

    public final void main(String[] args) throws IOException {
        validateArguments(args);

        String executionSpecPath = args[0];
        String outputFile = args[1];
        Boolean forceImport = Boolean.parseBoolean(args[2]);

        DataExportEngine engine = new DataExportEngine();

        try (Writer writer = getOutputWriter(outputFile)) {
            engine.execute(getSpecification(executionSpecPath), writer, forceImport);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DataExportSpecification getSpecification(String specificationPath) throws FileNotFoundException {
        File file = new File(specificationPath);
        if (!file.exists()){
            log.error("File not found: {}", specificationPath);
            System.exit(1);
        }
        return DataExportSpecification.fromJsonFile(file);
    }

    private void validateArguments(String[] args) {
        if (args.length != 3){
            log.error("Use: {} {} {} {}",
                    this.getClass().getCanonicalName(),
                    "dataExportSpecFile",
                    "outputFile",
                    "forceImport"
            );
            System.exit(1);
        }
    }

    private Writer getOutputWriter(String path) {
        try {
            return new FileWriter(path);
        } catch (IOException e) {
            log.error("Error initialising output writer: {}", path);
            System.exit(1);
            return null;
        }
    }
}
