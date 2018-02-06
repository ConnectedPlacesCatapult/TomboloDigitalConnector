package uk.org.tombolo.field.value;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.SingleValueField;

/**
 * ConstantField.java
 * Returns a fixed value for annotation purposes.
 */
public class ConstantField extends AbstractField implements SingleValueField {
    private final String value;

    public ConstantField(String label, String value) {
        super(label);
        this.value = value;
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) {
        return value;
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) {
        JSONObject obj = new JSONObject();
        obj.put(label, value);
        return obj;
    }

}
