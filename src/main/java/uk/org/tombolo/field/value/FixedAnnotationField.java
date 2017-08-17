package uk.org.tombolo.field.value;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.SingleValueField;

/**
 * FixedAnnotationField.java
 * Returns a fixed value for annotation purposes.
 */
public class FixedAnnotationField extends AbstractField implements SingleValueField {
    private final String value;

    public FixedAnnotationField(String label, String value) {
        super(label);
        this.value = value;
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) {
        return value;
    }

}
