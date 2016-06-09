package uk.org.tombolo.execution.spec;

import com.google.gson.JsonObject;
import org.json.simple.JSONObject;

public class FieldSpecification {
    private String fieldClass;
    private String label;
    private JSONObject data;

    public FieldSpecification(String fieldClass, String label){
        this.fieldClass = fieldClass;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getFieldClass() {
        return fieldClass;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public JSONObject getData() {
        return data;
    }
}
