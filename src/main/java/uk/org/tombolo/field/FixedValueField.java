package uk.org.tombolo.field;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.execution.spec.AttributeMatcher;

public class FixedValueField extends ValuesByTimeField implements SingleValueField {
    public FixedValueField(String label, AttributeMatcher attribute) {
        super(label, attribute);
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        return getFixedValue(subject).getValue().toString();
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        FixedValue fixedValue = getFixedValue(subject);
        JSONObject obj = new JSONObject();
        obj.put("value", fixedValue.getValue());
        JSONArray values = new JSONArray();
        values.add(obj);
        return withinMetadata(values);
    }

    private FixedValue getFixedValue(Subject subject) throws IncomputableFieldException {
        FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, getAttribute());
        if (fixedValue == null) {
            throw new IncomputableFieldException(String.format("No FixedValue found for attribute %s", getAttribute().getLabel()));
        }
        return fixedValue;
    }
}
