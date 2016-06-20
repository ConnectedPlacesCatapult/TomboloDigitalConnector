package uk.org.tombolo;

import com.github.fge.jsonschema.core.report.ProcessingReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.utils.HibernateUtil;
import uk.org.tombolo.execution.spec.DataExportSpecification;
import uk.org.tombolo.execution.spec.DataExportSpecificationValidator;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;
import uk.org.tombolo.importer.DownloadUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class DataExportRunner {
    private static final Logger log = LoggerFactory.getLogger(DataExportRunner.class);

    public static void main(String[] args) throws IOException {
        validateArguments(args);

        String executionSpecPath = args[0];
        String outputFile = args[1];
        Boolean forceImport = Boolean.parseBoolean(args[2]);

        HibernateUtil.startup();

        DataExportEngine engine = new DataExportEngine(new DownloadUtils());

        validateSpecification(executionSpecPath);

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

    private static void validateSpecification(String executionSpecPath) {
        ProcessingReport report = DataExportSpecificationValidator.validate(new File(executionSpecPath));
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
