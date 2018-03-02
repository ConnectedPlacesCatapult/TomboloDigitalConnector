package uk.org.tombolo;

import org.apache.commons.math3.linear.RealMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.utils.DatabaseUtils;
import uk.org.tombolo.core.utils.HibernateUtil;
import uk.org.tombolo.execution.CorrelationAnalysisEngine;
import uk.org.tombolo.execution.DataExportEngine;
import uk.org.tombolo.exporter.CSVExporter;
import uk.org.tombolo.exporter.GeoJsonExporter;
import uk.org.tombolo.importer.ImporterMatcher;
import uk.org.tombolo.recipe.DataExportRecipe;
import uk.org.tombolo.recipe.FieldRecipe;

import java.io.Writer;
import java.util.List;
import java.util.Properties;

public class DataExportRunner extends AbstractRunner {
    private static final Logger log = LoggerFactory.getLogger(DataExportRunner.class);
    private static final DataExportRunner runner = new DataExportRunner();

    public static void main(String[] args) throws Exception {
        Boolean isString = Boolean.parseBoolean(args[0]);
        String recipe = args[1];
        String output = args[2];
        String correlation = args[3];
        String forceImports = args[4];
        Boolean clearDatabaseCache = Boolean.parseBoolean(args[5]);

        run(isString, recipe, output, correlation, forceImports, clearDatabaseCache);
    }

    private static void run(Boolean isString, String recipe, String output, String correlation, String forceImports,
                            Boolean clearDatabaseCache) throws Exception {
        HibernateUtil.startup();
        if (clearDatabaseCache) {
            DatabaseUtils.clearAllData();
        }

        // Load API keys
        Properties apiKeys = runner.loadApiKeys();

        // Loading the recipe for future use
        DataExportRecipe dataExportRecipe = runner.getRecipe(recipe, isString);

        // Create engine
        DataExportEngine engine = new DataExportEngine(apiKeys, runner.initialiseDowloadUtils());

        try (Writer writer = runner.getOutputWriter(output)) {
            engine.execute(dataExportRecipe, writer, new ImporterMatcher(forceImports));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        } finally {
            HibernateUtil.shutdown();
        }

        // Perform the correlation analysis
        if (!correlation.equals("None")) {
            runner.runCorrelation(dataExportRecipe, output, correlation);
        }
    }

    /**
     * Performs correlation analysis.
     *
     * Calculates the correlation between all input fields and output the Pearson correlation coefficient,
     * the pValue and the standard error to a JSON file.
     *
     * Currently we only support the use of GeoJson as the intermediate data export format.
     * We chose this since it is a cleaner implementation than our CSV exporter
     * and the additional benefit is to be able to visualise the intermediate data in QGIS.
     *
     * @param dataExportRecipe Data export recipe
     * @param output File containing the exported fields to use in the correlation analysis. We get this file by running
     *               the recipe.
     * @param correlation Output file for the correlation results
     * @throws Exception
     */
    private void runCorrelation(DataExportRecipe dataExportRecipe, String output, String correlation) throws Exception {
        List<FieldRecipe> fields = dataExportRecipe.getDataset().getFields();

        // Read in data file
        RealMatrix matrix;
        if (dataExportRecipe.getExporter().equals(GeoJsonExporter.class.getCanonicalName())){
            matrix = CorrelationAnalysisEngine.readGeoJsonDataExport(output, fields);
        }else if(dataExportRecipe.getExporter().equals(CSVExporter.class.getCanonicalName())){
            matrix = CorrelationAnalysisEngine.readCSVDataExport(output, fields);
        }else {
            throw new Error("Unknown exporter class for intermediate data.");
        }

        // Calculate and output correlations
        CorrelationAnalysisEngine.calculateAndOutputCorrelations(matrix, fields, correlation);
    }
}
