package uk.org.tombolo.execution.spec;

import uk.org.tombolo.field.Field;

public class FieldSpecification {
    private String fieldClass;
    private String json;

    public FieldSpecification(String fieldClass, String json){
        this.fieldClass = fieldClass;
        this.json = json;
    }

    public Field toField() throws ClassNotFoundException {
        return (Field) SpecificationDeserializer.fromJson(json, Class.forName(fieldClass));
    }
}
