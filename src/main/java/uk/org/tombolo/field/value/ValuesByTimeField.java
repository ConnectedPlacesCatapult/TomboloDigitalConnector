package uk.org.tombolo.field.value;

import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.recipe.AttributeMatcher;

/**
 * ValuesByTimeField.java
 * Returns all TimedValues on an Attribute for a given Subject, plus metadata.
 *
 * The metadata is regarding the attribute.
 */
public class ValuesByTimeField extends AbstractField {

    public ValuesByTimeField(String label) {
        super(label);
    }

    public ValuesByTimeField(String label, AttributeMatcher attribute) {
        super(label, attribute);
    }
}
