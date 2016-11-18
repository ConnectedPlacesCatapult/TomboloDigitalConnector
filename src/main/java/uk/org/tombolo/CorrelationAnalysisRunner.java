package uk.org.tombolo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.execution.spec.DataExportSpecification;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.exporter.GeoJsonExporter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
 */
public class CorrelationAnalysisRunner extends AbstractRunner {
    private static final Logger log = LoggerFactory.getLogger(CorrelationAnalysisRunner.class);

    public static void main(String[] args) throws Exception {
        validateArguments(args);

        String dataExportSpecificationPath = args[0];
        String dataExportOutputPath = args[1];
        String correlationAnalysisOutputPath = args[2]; // We might want to extend this directory

        // Loading the data export specification for future use
        DataExportSpecification dataExportSpecification =
                DataExportRunner.getSpecification(dataExportSpecificationPath);

        // Exit if the specified exporter is not GeoJsonExporter
        if (!dataExportSpecification.getExporterClass().equals(GeoJsonExporter.class.getCanonicalName())){
            log.error("At the moment we only support GeoJson intermeditate export");
            System.exit(1);
        }

        // If datafile does not exist create it
        if (!Files.exists(Paths.get(dataExportOutputPath))){
            DataExportRunner.run(dataExportSpecificationPath, dataExportOutputPath,
                    "", Boolean.FALSE);
        }

        List<FieldSpecification> fieldSpecifications
                = dataExportSpecification.getDatasetSpecification().getFieldSpecification();

        // Read in data file
        RealMatrix matrix = readDataExport(dataExportOutputPath, fieldSpecifications);

        // Calculate and output correlations
        calculateAndOutputCorrelations(matrix, fieldSpecifications, correlationAnalysisOutputPath);
    }

    private static RealMatrix readDataExport(String dataExportOutputPath, List<FieldSpecification> fieldSpecifications)
            throws ClassNotFoundException, IOException {
        /*
            I tried both using GeoTools DataStore and GeometryJSON parsing but both had problems with our
            GeoJson formatted files. We might want to take another look at some later point.
         */
        JsonReader jsonReader = new JsonReader(new FileReader(dataExportOutputPath));
        JsonObject jsonObject = new JsonParser().parse(jsonReader).getAsJsonObject();

        JsonArray features = jsonObject.getAsJsonArray("features");

        Iterator<JsonElement> featureIterator = features.iterator();

        List<List<Double>> valueMatrix = new ArrayList<>();
        while(featureIterator.hasNext()){
            JsonObject feature = featureIterator.next().getAsJsonObject();
            JsonObject properties = feature.getAsJsonObject("properties");

            List<Double> valueList = new ArrayList<>();
            for (FieldSpecification fieldSpecification : fieldSpecifications) {
                String fieldLabel = fieldSpecification.toField().getLabel();
                try {
                    JsonArray values = properties.getAsJsonArray(fieldLabel);
                    Double value = ((JsonObject)values.get(0)).getAsJsonPrimitive("value").getAsDouble();
                    valueList.add(value);
                }catch (ClassCastException e){
                    // This part is here since there is inconsistency in the format of the Json output for fields.
                    // Some include value and a timestamp but others just the value.
                    try {
                        Double value = properties.getAsJsonPrimitive(fieldLabel).getAsDouble();
                        valueList.add(value);
                    }catch (ClassCastException ee){
                        // One of the fields for this subject has a NaN value.
                        // In this case we cannot use the subject as part of the correlation analysis.
                        valueList = null;
                        break;
                    }
                }
            }

            if (valueList!= null && valueList.size() == fieldSpecifications.size()){
                // The value list is the same size as the number of fields
                valueMatrix.add(valueList);
            }
        }
        jsonReader.close();

        // Turn the valueMatrix List into a RealMatrix
        RealMatrix matrix = new BlockRealMatrix(valueMatrix.size(), fieldSpecifications.size());
        for(int i=0; i<valueMatrix.size(); i++){
            // The i-th subject with non NaN values for all fields
            for (int j=0; j<fieldSpecifications.size(); j++){
                // j-th field
                matrix.setEntry(i,j,valueMatrix.get(i).get(j));
            }
        }
        return matrix;
    }

    private static void calculateAndOutputCorrelations(RealMatrix matrix, List<FieldSpecification> fieldSpecifications,
                                                       String correlationAnalysisOutputPath) throws Exception {
        PearsonsCorrelation correlation = new PearsonsCorrelation(matrix);
        RealMatrix correlationMatrix = correlation.getCorrelationMatrix();
        RealMatrix pValueMatrix = correlation.getCorrelationPValues();
        RealMatrix standardErrorMatrix = correlation.getCorrelationStandardErrors();

        // Output the correlation analysis
        JSONArray correlationArray = new JSONArray();
        for (int i=0; i<correlationMatrix.getRowDimension(); i++){
            for (int j=0; j<correlationMatrix.getColumnDimension(); j++){
                JSONObject correlationObject = new JSONObject();
                correlationObject.put("xFieldLabel", fieldSpecifications.get(i).toField().getLabel());
                correlationObject.put("yFieldLabel", fieldSpecifications.get(j).toField().getLabel());
                correlationObject.put("correlationCoefficient", correlationMatrix.getEntry(i,j));
                correlationObject.put("pValue", pValueMatrix.getEntry(i,j));
                correlationObject.put("standardError", standardErrorMatrix.getEntry(i,j));
                correlationArray.add(correlationObject);
            }
        }
        Writer writer = new OutputStreamWriter(new FileOutputStream(correlationAnalysisOutputPath), "UTF-8");
        writer.write(correlationArray.toJSONString());
        writer.flush();
        writer.close();
    }

    private static void validateArguments(String[] args){
        if (args.length != 3){
            log.error("Use: {} {} {} {}",
                    CorrelationAnalysisRunner.class.getCanonicalName(),
                    "dataExportSpecificationFile",
                    "dataExportOutputFile",
                    "correlationAnalysisOutputFile");
        }
    }
}
