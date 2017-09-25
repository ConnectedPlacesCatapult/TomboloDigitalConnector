package uk.org.tombolo.field.value;

import uk.org.tombolo.recipe.AttributeMatcher;

/**
 * LatestValueField.java
 * Returns the latest TimedValue for a particular Attribute on the given subject, plus metadata
 *
 * The metadata is regarding the attribute.
 */
public class LatestValueField extends ValuesByTimeField {
    public LatestValueField(String label, AttributeMatcher attribute) {
        super(label, attribute);
    }
}
