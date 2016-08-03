package uk.org.tombolo;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import java.util.List;

public class FieldSpecificationBuilder implements JSONAware {
    JSONObject jsonSpec;

    FieldSpecificationBuilder() {
        jsonSpec = new JSONObject();
    }

    public static FieldSpecificationBuilder latestValue(String providerLabel, String attributeLabel) {
        FieldSpecificationBuilder spec = new FieldSpecificationBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.LatestValueField")
                .setLabel(attributeLabel)
                .setAttribute(providerLabel, attributeLabel);
        return spec;
    }

    public static FieldSpecificationBuilder valuesByTime(String providerLabel, String attributeLabel) {
        FieldSpecificationBuilder spec = new FieldSpecificationBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.ValuesByTimeField")
                .setLabel(attributeLabel)
                .setAttribute(providerLabel, attributeLabel);
        return spec;
    }


    public static FieldSpecificationBuilder wrapperField(String label, List<FieldSpecificationBuilder> fieldSpecificationBuilders) {
        FieldSpecificationBuilder spec = new FieldSpecificationBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.WrapperField")
                .setLabel(label)
                .setFieldSpecification(fieldSpecificationBuilders);
        return spec;
    }

    public static FieldSpecificationBuilder mapToContainingSubjectField(String label, String subjectType, FieldSpecificationBuilder fieldSpecificationBuilder) {
        FieldSpecificationBuilder spec = new FieldSpecificationBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.MapToContainingSubjectField")
                .setLabel(label)
                .setContainingSubjectType(subjectType)
                .setFieldSpecification(fieldSpecificationBuilder);
        return spec;
    }

    public static FieldSpecificationBuilder fixedAnnotationField(String label, String value) {
        FieldSpecificationBuilder spec = new FieldSpecificationBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.FixedAnnotationField")
                .setLabel(label)
                .setValue(value);
        return spec;
    }

    public static FieldSpecificationBuilder fractionOfTotal(String label) {
        FieldSpecificationBuilder spec = new FieldSpecificationBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.FractionOfTotalField")
                .setLabel(label);
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

    public FieldSpecificationBuilder setFieldClass(String fieldClass) {
        jsonSpec.put("fieldClass", fieldClass);
        return this;
    }

    public FieldSpecificationBuilder setLabel(String label) {
        jsonSpec.put("label", label);
        return this;
    }

    public FieldSpecificationBuilder setFieldSpecification(List<FieldSpecificationBuilder> fields) {
        jsonSpec.put("fieldSpecification", fields);
        return this;
    }

    private FieldSpecificationBuilder setFieldSpecification(FieldSpecificationBuilder field) {
        jsonSpec.put("fieldSpecification", field);
        return this;
    }

    public FieldSpecificationBuilder setValue(String value) {
        jsonSpec.put("value", value);
        return this;
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

    public FieldSpecificationBuilder setContainingSubjectType(String subjectType) {
        jsonSpec.put("containingSubjectType", subjectType);
        return this;
    }
}
