package uk.org.tombolo;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;

import java.util.List;

public class FieldSpecificationBuilder implements JSONAware {
    JSONObject jsonSpec;

    FieldSpecificationBuilder() {
        jsonSpec = new JSONObject();
    }

    public static FieldSpecificationBuilder latestValue(String providerLabel, String attributeLabel) {
        FieldSpecificationBuilder spec = new FieldSpecificationBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.value.LatestValueField")
                .setLabel(attributeLabel)
                .setAttribute(providerLabel, attributeLabel);
        return spec;
    }

    public static FieldSpecificationBuilder valuesByTime(String providerLabel, String attributeLabel) {
        FieldSpecificationBuilder spec = new FieldSpecificationBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.value.ValuesByTimeField")
                .setLabel(attributeLabel)
                .setAttribute(providerLabel, attributeLabel);
        return spec;
    }


    public static FieldSpecificationBuilder wrapperField(String label, List<FieldSpecificationBuilder> fieldSpecificationBuilders) {
        FieldSpecificationBuilder spec = new FieldSpecificationBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.WrapperField")
                .setLabel(label)
                .set("fieldSpecification", fieldSpecificationBuilders);
        return spec;
    }

    public static FieldSpecificationBuilder mapToContainingSubjectField(String label, String containingSubjectProvider, String containingSubjectType, FieldSpecificationBuilder fieldSpecificationBuilder) {
        FieldSpecificationBuilder spec = new FieldSpecificationBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.aggregation.MapToContainingSubjectField")
                .setLabel(label)
                .set("containingSubjectProvider", containingSubjectProvider)
                .set("containingSubjectType", containingSubjectType)
                .set("fieldSpecification", fieldSpecificationBuilder);
        return spec;
    }

    public static FieldSpecificationBuilder fixedAnnotationField(String label, String value) {
        FieldSpecificationBuilder spec = new FieldSpecificationBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.value.FixedAnnotationField")
                .setLabel(label)
                .set("value", value);
        return spec;
    }

    public static FieldSpecificationBuilder fractionOfTotal(String label) {
        FieldSpecificationBuilder spec = new FieldSpecificationBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.transformation.FractionOfTotalField")
                .setLabel(label);
        return spec;
    }

    public static FieldSpecificationBuilder modellingField(String label, String recipe) {
        FieldSpecificationBuilder spec = new FieldSpecificationBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.modelling.BasicModellingField")
                .setLabel(label)
                .set("recipe", recipe);
        return spec;
    }

    public static FieldSpecificationBuilder geographicAggregation(String label, String aggregationSubjectProvider, String aggregationSubjectType, String aggregationFunction, FieldSpecificationBuilder fieldSpecificationBuilder) {
        FieldSpecificationBuilder spec = new FieldSpecificationBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.aggregation.GeographicAggregationField")
                .setLabel(label)
                .set("aggregationSubjectProvider", aggregationSubjectProvider)
                .set("aggregationSubjectType", aggregationSubjectType)
                .set("aggregationFunction", aggregationFunction)
                .set("fieldSpecification", fieldSpecificationBuilder);
        return spec;
    }

    public static FieldSpecificationBuilder percentilesField(String label, Integer percentileCount, Boolean inverse) {
        FieldSpecificationBuilder spec = new FieldSpecificationBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.transformation.PercentilesField")
                .setLabel(label)
                .set("percentileCount", percentileCount)
                .set("inverse", inverse);
        return spec;
    }

    private FieldSpecificationBuilder setAttribute(String providerLabel, String attributeLabel) {
        JSONObject attribute = new JSONObject();
        attribute.put("providerLabel", providerLabel);
        attribute.put("attributeLabel", attributeLabel);
        jsonSpec.put("attribute", attribute);
        return this;
    }

    @Override
    public String toJSONString() {
        return jsonSpec.toJSONString();
    }

    public FieldSpecificationBuilder set(String key, Object value) {
        jsonSpec.put(key, value);
        return this;
    }

    public FieldSpecificationBuilder setFieldClass(String fieldClass) {
        return set("fieldClass", fieldClass);
    }

    public FieldSpecificationBuilder setLabel(String label) {
        return set("label", label);
    }

    public FieldSpecificationBuilder addDividendAttribute(String providerLabel, String attributeLabel) {
        JSONArray dividendAttributeListObj = (JSONArray) jsonSpec.getOrDefault("dividendAttributes", new JSONArray());
        jsonSpec.put("dividendAttributes", dividendAttributeListObj);
        JSONObject dividendAttributeObj = new JSONObject();
        dividendAttributeObj.put("providerLabel", providerLabel);
        dividendAttributeObj.put("attributeLabel", attributeLabel);
        dividendAttributeListObj.add(dividendAttributeObj);
        return this;
    }

    public FieldSpecificationBuilder setDivisorAttribute(String providerLabel, String attributeLabel) {
        JSONObject divisorAttributeObj = new JSONObject();
        divisorAttributeObj.put("providerLabel", providerLabel);
        divisorAttributeObj.put("attributeLabel", attributeLabel);
        jsonSpec.put("divisorAttribute", divisorAttributeObj);
        return this;
    }

    public FieldSpecification build() {
        return SpecificationDeserializer.fromJson(toJSONString(), FieldSpecification.class);
    }
}
