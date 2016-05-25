package uk.org.tombolo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.utils.HibernateUtil;
import uk.org.tombolo.execution.spec.DataExportSpecification;
import uk.org.tombolo.importer.DownloadUtils;

import java.io.*;

public class DataExportRunner {
    private static final Logger log = LoggerFactory.getLogger(DataExportRunner.class);

    public static void main(String[] args) throws IOException {
        validateArguments(args);

        String executionSpecPath = args[0];
        String outputFile = args[1];
        Boolean forceImport = Boolean.parseBoolean(args[2]);

        DataExportEngine engine = new DataExportEngine(new DownloadUtils());

        try (Writer writer = getOutputWriter(outputFile)) {
            engine.execute(getSpecification(executionSpecPath), writer, forceImport);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // FIXME: This method should either be responsible for the entire state of HibernateUtil, or not at all.
            // e.g. it should set it up too, or it shouldn't have to shut it down.
            HibernateUtil.shutdown();
        }
    }

    private static DataExportSpecification getSpecification(String specificationPath) throws FileNotFoundException {
        File file = new File(specificationPath);
        if (!file.exists()){
            log.error("File not found: {}", specificationPath);
            System.exit(1);
        }
        return DataExportSpecification.fromJsonFile(file);
    }

    private static void validateArguments(String[] args) {
        if (args.length != 3){
            log.error("Use: {} {} {} {}",
                    DataExportRunner.class.getCanonicalName(),
                    "dataExportSpecFile",
                    "outputFile",
                    "forceImport"
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
