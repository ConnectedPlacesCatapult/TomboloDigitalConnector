package uk.org.tombolo.execution.spec;

import com.google.gson.Gson;
import uk.org.tombolo.field.Field;

public class FieldSpecification {
    private String fieldClass;
    private String json;

    public FieldSpecification(String fieldClass, String json){
        this.fieldClass = fieldClass;
        this.json = json;
    }

    public Field toField() throws ClassNotFoundException {
        Gson gson = new Gson();
        return (Field) gson.fromJson(json, Class.forName(fieldClass));
    }
}
