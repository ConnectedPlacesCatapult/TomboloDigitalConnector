package uk.org.tombolo.field.value;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;
import uk.org.tombolo.recipe.AttributeMatcher;

/**
 * For a given subject, returns the {@link FixedValue} of a specified attribute.
 */
public class FixedValueField extends BasicValueField implements SingleValueField {

    public FixedValueField(String label, AttributeMatcher attribute) {
        super(label, attribute);
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        return getFixedValue(subject).getValue().toString();
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        FixedValue fixedValue = getFixedValue(subject);
        JSONObject obj = new JSONObject();
        obj.put(null != this.label ? this.label : "value", fixedValue.getValue());
        return obj;
    }

    private FixedValue getFixedValue(Subject subject) throws IncomputableFieldException {
        FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, getAttribute());
        if (fixedValue == null) {
            throw new IncomputableFieldException(String.format("No FixedValue found for attribute %s", getAttribute().getLabel()));
        }
        return fixedValue;
    }
}
