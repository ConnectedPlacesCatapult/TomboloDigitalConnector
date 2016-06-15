package uk.org.tombolo.field.organicity;

import org.json.simple.JSONObject;
import uk.org.tombolo.execution.spec.AttributeMatcher;
import uk.org.tombolo.field.ValuesByTimeField;

/**
 * ValuesByTimeWithUrnField.java
 * Just as ValuesByTimeField, but with provided URN metadata included.
 */
public class ValuesByTimeWithUrnField extends ValuesByTimeField {
    private final String urn;

    public ValuesByTimeWithUrnField(String label, AttributeMatcher attribute, String urn) {
        super(label, attribute);
        this.urn = urn;
    }

    @Override
    protected JSONObject withinMetadata(JSONObject contents) {
        JSONObject obj = super.withinMetadata(contents);
        JSONObject attrAttrs = new JSONObject();
        attrAttrs.put("urn", urn);
        ((JSONObject) obj.get(label)).put("attributes", attrAttrs);
        return obj;
    }
}
