package uk.org.tombolo;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.recipe.RecipeDeserializer;

import java.util.List;

public class FieldBuilder implements JSONAware {
    JSONObject jsonSpec;

    FieldBuilder() {
        jsonSpec = new JSONObject();
    }

    public static FieldBuilder latestValue(String providerLabel, String attributeLabel) {
        FieldBuilder spec = new FieldBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.value.LatestValueField")
                .setLabel(attributeLabel)
                .setAttribute(providerLabel, attributeLabel);
        return spec;
    }

    public static FieldBuilder fixedValueField(String providerLabel, String attributeLabel) {
        FieldBuilder spec = new FieldBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.value.FixedValueField")
                .setLabel(attributeLabel)
                .setAttribute(providerLabel, attributeLabel);
        return spec;
    }

    public static FieldBuilder valuesByTime(String providerLabel, String attributeLabel) {
        FieldBuilder spec = new FieldBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.value.ValuesByTimeField")
                .setLabel(attributeLabel)
                .setAttribute(providerLabel, attributeLabel);
        return spec;
    }


    public static FieldBuilder wrapperField(String label, List<FieldBuilder> fieldBuilders) {
        FieldBuilder spec = new FieldBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.WrapperField")
                .setLabel(label)
                .set("fields", fieldBuilders);
        return spec;
    }

    public static FieldBuilder mapToContainingSubjectField(String label, String containingSubjectProvider, String containingSubjectType, FieldBuilder fieldBuilder) {
        FieldBuilder spec = new FieldBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.aggregation.MapToContainingSubjectField")
                .setLabel(label)
                .setSubject(containingSubjectProvider, containingSubjectType)
                .set("field", fieldBuilder);
        return spec;
    }

    public static FieldBuilder fixedAnnotationField(String label, String value) {
        FieldBuilder spec = new FieldBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.value.FixedAnnotationField")
                .setLabel(label)
                .set("value", value);
        return spec;
    }

    public static FieldBuilder fractionOfTotal(String label) {
        FieldBuilder spec = new FieldBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.transformation.FractionOfTotalField")
                .setLabel(label);
        return spec;
    }

    public static FieldBuilder modellingField(String label, String recipe) {
        FieldBuilder spec = new FieldBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.modelling.BasicModellingField")
                .setLabel(label)
                .set("recipe", recipe);
        return spec;
    }

    public static FieldBuilder geographicAggregation(String label, String aggregationSubjectProvider, String aggregationSubjectType, String function, FieldBuilder fieldBuilder) {
        FieldBuilder spec = new FieldBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.aggregation.GeographicAggregationField")
                .setLabel(label)
                .setSubject(aggregationSubjectProvider, aggregationSubjectType)
                .set("function", function)
                .set("field", fieldBuilder);
        return spec;
    }

    public static FieldBuilder percentilesField(String label, Integer percentileCount, Boolean inverse) {
        FieldBuilder spec = new FieldBuilder();
        spec    .setFieldClass("uk.org.tombolo.field.transformation.PercentilesField")
                .setLabel(label)
                .set("percentileCount", percentileCount)
                .set("inverse", inverse);
        return spec;
    }

    private FieldBuilder setAttribute(String providerLabel, String attributeLabel) {
        JSONObject attribute = new JSONObject();
        attribute.put("provider", providerLabel);
        attribute.put("label", attributeLabel);
        jsonSpec.put("attribute", attribute);
        return this;
    }

    private FieldBuilder setSubject(String providerLabel, String subjectType) {
        JSONObject subject = new JSONObject();
        subject.put("provider", providerLabel);
        subject.put("subjectType", subjectType);
        jsonSpec.put("subject", subject);
        return this;
    }

    @Override
    public String toJSONString() {
        return jsonSpec.toJSONString();
    }

    public FieldBuilder set(String key, Object value) {
        jsonSpec.put(key, value);
        return this;
    }

    public FieldBuilder setFieldClass(String fieldClass) {
        return set("fieldClass", fieldClass);
    }

    public FieldBuilder setLabel(String label) {
        return set("label", label);
    }

    public FieldBuilder addDividendAttribute(String providerLabel, String attributeLabel) {
        JSONArray dividendAttributeListObj = (JSONArray) jsonSpec.getOrDefault("dividendAttributes", new JSONArray());
        jsonSpec.put("dividendAttributes", dividendAttributeListObj);
        JSONObject dividendAttributeObj = new JSONObject();
        dividendAttributeObj.put("provider", providerLabel);
        dividendAttributeObj.put("label", attributeLabel);
        dividendAttributeListObj.add(dividendAttributeObj);
        return this;
    }

    public FieldBuilder setDivisorAttribute(String providerLabel, String attributeLabel) {
        JSONObject divisorAttributeObj = new JSONObject();
        divisorAttributeObj.put("provider", providerLabel);
        divisorAttributeObj.put("label", attributeLabel);
        jsonSpec.put("divisorAttribute", divisorAttributeObj);
        return this;
    }

    public FieldRecipe build() {
        return RecipeDeserializer.fromJson(toJSONString(), FieldRecipe.class);
    }
}
