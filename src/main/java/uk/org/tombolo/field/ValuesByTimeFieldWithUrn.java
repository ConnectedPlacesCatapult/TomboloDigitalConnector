package uk.org.tombolo.field;

import org.json.simple.JSONObject;

public class ValuesByTimeFieldWithUrn extends ValuesByTimeField {
    private final String urn;

    public ValuesByTimeFieldWithUrn(String label, AttributeStruct attribute, String urn) {
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
