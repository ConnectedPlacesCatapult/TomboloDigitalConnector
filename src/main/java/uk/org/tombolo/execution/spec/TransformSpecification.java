package uk.org.tombolo.execution.spec;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.utils.AttributeUtils;

import java.util.ArrayList;
import java.util.List;

public class TransformSpecification {

    static final class AttributeSpecifier {
        String providerLabel;
        String attributeLabel;

        AttributeSpecifier(String providerLabel, String attributeLabel) {
            this.providerLabel = providerLabel;
            this.attributeLabel = attributeLabel;
        }
    }

    private List<AttributeSpecifier> inputAttributes;
    private String transformClass;
    private Attribute outputAttribute;

    public TransformSpecification(List<AttributeSpecifier> inputAttributes, Attribute outputAttribute, String transformClass){
        this.inputAttributes = inputAttributes;
        this.transformClass = transformClass;
        this.outputAttribute = outputAttribute;
    }

    public String getTransformClass() {
        return transformClass;
    }

    public List<Attribute> getInputAttributes() {
        return procureAttributes(this.inputAttributes);
    }

    public Attribute getOutputAttribute() {
        return outputAttribute;
    }

    private List<Attribute> procureAttributes(List<AttributeSpecifier> specifiers) {
        List<Attribute> attributes = new ArrayList<>();
        for (AttributeSpecifier specifier : specifiers) {
            Attribute attribute = AttributeUtils.getByProviderAndLabel(specifier.providerLabel, specifier.attributeLabel);
            if (null == attribute) {
                throw new IllegalArgumentException("Input attribute " + specifier.attributeLabel + " does not exist");
            }
            attributes.add(attribute);
        }
        return attributes;
    }
}
