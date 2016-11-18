package uk.org.tombolo.execution;

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
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.TimedValueId;
import uk.org.tombolo.execution.spec.FieldSpecification;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Execution engine for the correlation analysis
 */
public class CorrelationAnalysisEngine {

    /**
     * Reads in a Data Export file in GeoJson format and turns it into a RealMatrix having a column for each field
     * in the fields specification and a row for each subject in the data export that has a non-NaN value for
     * each of the fields.
     *
     * @param dataExportOutputPath is the location of the otput data.
     * @param fieldSpecifications is a list of field specifications to use in the correlation calculation.
     * @return a matrix of the subject values for the different fields
     * @throws ClassNotFoundException is thrown if the FieldSpecification cannot be cast to a Field
     * @throws IOException if there is a problem reading the data export output file
     */
    public static RealMatrix readGeoJsonDataExport(String dataExportOutputPath, List<FieldSpecification> fieldSpecifications)
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
                    LocalDateTime latestTimestamp = LocalDateTime.parse(
                            ((JsonObject)values.get(0)).getAsJsonPrimitive("timestamp").getAsString(),
                            TimedValueId.DATE_TIME_FORMATTER);
                    Double latestValue = ((JsonObject)values.get(0)).getAsJsonPrimitive("value").getAsDouble();

                    for (int i=1; i<values.size(); i++){
                        LocalDateTime timestamp = LocalDateTime.parse(
                                ((JsonObject)values.get(i)).getAsJsonPrimitive("timestamp").getAsString(),
                                TimedValueId.DATE_TIME_FORMATTER);
                        if (timestamp.isAfter(latestTimestamp)){
                            latestTimestamp = timestamp;
                            latestValue = ((JsonObject)values.get(i)).getAsJsonPrimitive("value").getAsDouble();
                        }
                    }
                    valueList.add(latestValue);
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
                }catch (NullPointerException e){
                    // The feature value does not exist
                    valueList = null;
                    break;
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

    /**
     * Calculates pearson correlation between pair of columns in the in the matrix, assuming that there is a strict
     * one to one relationship between the matrix columns and the field specifications in the list.
     *
     * Writes the correlation, pValue and standard error to a file using JSON format.
     *
     * @param matrix the input matrix where fields are represented by as columns and subjects by rows
     * @param fieldSpecifications a list of field specifications for which the correlations are to be calculated
     * @param correlationAnalysisOutputPath is the file to which the results are written
     * @throws Exception
     */
    public static void calculateAndOutputCorrelations(RealMatrix matrix, List<FieldSpecification> fieldSpecifications,
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

}
