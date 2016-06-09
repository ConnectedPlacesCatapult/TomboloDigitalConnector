package uk.org.tombolo.execution.spec;

public class FieldSpecification {
    private String fieldClass;
    private String label;

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
}
