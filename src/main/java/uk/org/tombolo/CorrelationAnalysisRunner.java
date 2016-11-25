package uk.org.tombolo;

import org.apache.commons.math3.linear.RealMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.execution.CorrelationAnalysisEngine;
import uk.org.tombolo.execution.spec.DataExportSpecification;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.exporter.CSVExporter;
import uk.org.tombolo.exporter.GeoJsonExporter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * A Runner class for performing correlation analysis.
 *
 * Arguments:
 * - Data export specification file
 * - File containing the exported fields to use in the correlation analysis.
 *   If this file does not exist, we run the export using the export specification file.
 * - Output file for the correlation results
 *
 * Currently we only support the use of GeoJson as the intermediate data export format.
 * We chose this since it is a cleaner implementation than our CSV exporter
 * and the additional benefit is to be able to visualise the intermediate data in QGIS.
 *
 * We calculate correlation between all input fields and output the Pearson correlation coefficient,
 * the pValue and the standard error to a JSON file.
 */
public class CorrelationAnalysisRunner extends AbstractRunner {
    private static final Logger log = LoggerFactory.getLogger(CorrelationAnalysisRunner.class);

    public static void main(String[] args) throws Exception {
        validateArguments(args);

        String dataExportSpecificationPath = args[0];
        String dataExportOutputPath = args[1];
        String correlationAnalysisOutputPath = args[2]; // We might want to extend this directory
        String forceImports = args[3];
        Boolean clearDatabaseCache = Boolean.parseBoolean(args[4]);


        // Loading the data export specification for future use
        DataExportSpecification dataExportSpecification =
                DataExportRunner.getSpecification(dataExportSpecificationPath);

        // If datafile does not exist create it
        if (!Files.exists(Paths.get(dataExportOutputPath))){
            DataExportRunner.run(dataExportSpecificationPath, dataExportOutputPath,
                    forceImports, clearDatabaseCache);
        }

        List<FieldSpecification> fieldSpecifications
                = dataExportSpecification.getDatasetSpecification().getFieldSpecification();

        // Read in data file
        RealMatrix matrix;
        if (dataExportSpecification.getExporterClass().equals(GeoJsonExporter.class.getCanonicalName())){
            matrix = CorrelationAnalysisEngine.readGeoJsonDataExport(dataExportOutputPath, fieldSpecifications);
        }else if(dataExportSpecification.getExporterClass().equals(CSVExporter.class.getCanonicalName())){
            matrix = CorrelationAnalysisEngine.readCSVDataExport(dataExportOutputPath, fieldSpecifications);
        }else {
            throw new Error("Unknown exporter class for intermdiate data");
        }

        // Calculate and output correlations
        CorrelationAnalysisEngine.calculateAndOutputCorrelations(matrix, fieldSpecifications, correlationAnalysisOutputPath);
    }

    private static void validateArguments(String[] args){
        if (args.length != 5){
            log.error("Use: {} {} {} {} {} {}",
                    CorrelationAnalysisRunner.class.getCanonicalName(),
                    "dataExportSpecificationFile",
                    "dataExportOutputFile",
                    "correlationAnalysisOutputFile",
                    "forceImports",
                    "clearDatabaseCache");
        }
    }
}
