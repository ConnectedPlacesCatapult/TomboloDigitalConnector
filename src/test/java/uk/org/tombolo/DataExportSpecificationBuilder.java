package uk.org.tombolo;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import uk.org.tombolo.execution.spec.DataExportSpecification;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;
import uk.org.tombolo.exporter.CSVExporter;
import uk.org.tombolo.exporter.GeoJsonExporter;

public class DataExportSpecificationBuilder implements JSONAware {
    private JSONObject jsonSpec;
    private JSONArray subjectSpec;
    private JSONArray datasourceSpec;
    private JSONArray fieldSpec;

    private DataExportSpecificationBuilder() {
        jsonSpec = new JSONObject();
        JSONObject datasetSpec = new JSONObject();
        subjectSpec = new JSONArray();
        datasourceSpec = new JSONArray();
        fieldSpec = new JSONArray();

        jsonSpec.put("datasetSpecification", datasetSpec);
        datasetSpec.put("subjectSpecification", subjectSpec);
        datasetSpec.put("datasourceSpecification", datasourceSpec);
        datasetSpec.put("fieldSpecification", fieldSpec);
    }

    public DataExportSpecificationBuilder setExporterClass(String exporterClass) {
        jsonSpec.put("exporterClass", exporterClass);
        return this;
    }

    public DataExportSpecification build() {
        return SpecificationDeserializer.fromJson(toJSONString(), DataExportSpecification.class);
    }

    @Override
    public String toJSONString() {
        return jsonSpec.toJSONString();
    }

    public DataExportSpecificationBuilder addSubjectSpecification(SubjectSpecificationBuilder subjectSpecificationBuilder) {
        subjectSpec.add(subjectSpecificationBuilder);
        return this;
    }

    public DataExportSpecificationBuilder addFieldSpecification(FieldSpecificationBuilder fieldSpecificationBuilder) {
        fieldSpec.add(fieldSpecificationBuilder);
        return this;
    }

    public DataExportSpecificationBuilder addDatasourceSpecification(String importerClass, String datasourceId, String configFile) {
        JSONObject datasource = new JSONObject();
        datasource.put("importerClass", importerClass);
        datasource.put("datasourceId", datasourceId);
        datasource.put("configurationFile", configFile);
        datasourceSpec.add(datasource);
        return this;
    }

    public static DataExportSpecificationBuilder withCSVExporter() {
        DataExportSpecificationBuilder builder =  new DataExportSpecificationBuilder();
        builder.setExporterClass(CSVExporter.class.getCanonicalName());
        return builder;
    }

    public static DataExportSpecificationBuilder withGeoJsonExporter() {
        DataExportSpecificationBuilder builder = new DataExportSpecificationBuilder();
        builder.setExporterClass(GeoJsonExporter.class.getCanonicalName());
        return builder;
    }
}
