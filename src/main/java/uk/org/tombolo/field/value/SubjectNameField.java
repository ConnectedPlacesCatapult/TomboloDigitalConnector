package uk.org.tombolo.field.value;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;

/**
 * SubjectNameField.java
 * Returns the name of the given subject
 */
public class SubjectNameField implements Field, SingleValueField {
    private final String label;

    public SubjectNameField(String label) {
        this.label = label;
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        return subject.getName();
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        JSONObject obj = new JSONObject();
        obj.put(label, valueForSubject(subject));
        return obj;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
