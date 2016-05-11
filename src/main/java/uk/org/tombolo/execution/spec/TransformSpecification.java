package uk.org.tombolo.execution.spec;

import uk.org.tombolo.core.Attribute;

import java.util.List;

public class TransformSpecification {
    private List<Attribute> inputAttributes;
    private String transformClass;
    private Attribute outputAttribute;

    public TransformSpecification(List<Attribute> inputAttributes, Attribute outputAttribute, String transformClass){
        this.inputAttributes = inputAttributes;
        this.transformClass = transformClass;
        this.outputAttribute = outputAttribute;
    }

    public String getTransformClass() {
        return transformClass;
    }

    public List<Attribute> getInputAttributes() {
        return inputAttributes;
    }

    public Attribute getOutputAttribute() {
        return outputAttribute;
    }
}
