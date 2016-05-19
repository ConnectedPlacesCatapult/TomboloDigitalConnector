package uk.org.tombolo;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import uk.org.tombolo.execution.spec.DataExportSpecification;
import uk.org.tombolo.exporter.GeoJsonExporter;

public class DataExportSpecificationBuilder implements JSONAware {
    private JSONObject jsonSpec;
    private JSONArray geographySpec;
    private JSONArray datasourceSpec;
    private JSONArray transformSpec;
    private JSONArray attributeSpec;

    private DataExportSpecificationBuilder() {
        jsonSpec = new JSONObject();
        JSONObject datasetSpec = new JSONObject();
        geographySpec = new JSONArray();
        datasourceSpec = new JSONArray();
        transformSpec = new JSONArray();
        attributeSpec = new JSONArray();

        jsonSpec.put("datasetSpecification", datasetSpec);
        datasetSpec.put("geographySpecification", geographySpec);
        datasetSpec.put("datasourceSpecification", datasourceSpec);
        datasetSpec.put("transformSpecification", transformSpec);
        datasetSpec.put("attributeSpecification", attributeSpec);
    }

    public static DataExportSpecificationBuilder withGeoJsonExporter() {
        DataExportSpecificationBuilder builder =  new DataExportSpecificationBuilder();
        builder.setExporterClass(GeoJsonExporter.class.getCanonicalName());
        return builder;
    }

    public DataExportSpecificationBuilder setExporterClass(String exporterClass) {
        jsonSpec.put("exporterClass", exporterClass);
        return this;
    }

    public DataExportSpecification build() {
        return DataExportSpecification.fromJson(toJSONString());
    }

    @Override
    public String toJSONString() {
        return jsonSpec.toJSONString();
    }

    public DataExportSpecificationBuilder addGeographySpecification(GeographySpecificationBuilder geographySpecificationBuilder) {
        geographySpec.add(geographySpecificationBuilder);
        return this;
    }

    public DataExportSpecificationBuilder addAttributeSpecification(String providerLabel, String attributeLabel) {
        JSONObject attribute = new JSONObject();
        attribute.put("providerLabel", providerLabel);
        attribute.put("attributeLabel", attributeLabel);
        attributeSpec.add(attribute);
        return this;
    }
}
