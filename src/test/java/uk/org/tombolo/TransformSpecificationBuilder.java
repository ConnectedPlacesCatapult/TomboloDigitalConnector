package uk.org.tombolo;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class TransformSpecificationBuilder implements JSONAware {
    JSONObject jsonSpec;
    JSONObject outputAttribute;
    JSONArray inputAttributes;

    public TransformSpecificationBuilder(String transformerClass) {
        jsonSpec = new JSONObject();
        outputAttribute = new JSONObject();
        inputAttributes = new JSONArray();
        jsonSpec.put("transformerClass", transformerClass);
        jsonSpec.put("outputAttribute", outputAttribute);
        jsonSpec.put("inputAttributes", inputAttributes);
    }

    @Override
    public String toJSONString() {
        return jsonSpec.toJSONString();
    }

    public TransformSpecificationBuilder setOutputAttribute(String providerPrefix, String attributePrefix) {
        JSONObject provider = new JSONObject();
        outputAttribute.put("label", attributePrefix + "_label");
        outputAttribute.put("name", attributePrefix + "_name");
        outputAttribute.put("description", attributePrefix + "_description");
        outputAttribute.put("provider", provider);
        provider.put("label", providerPrefix + "_label");
        provider.put("name", providerPrefix + "_name");
        return this;
    }

    public TransformSpecificationBuilder addInputAttribute(String providerLabel, String attributeLabel) {
        JSONObject attribute = new JSONObject();
        attribute.put("providerLabel", providerLabel);
        attribute.put("attributeLabel", attributeLabel);
        inputAttributes.add(attribute);
        return this;
    }
}
