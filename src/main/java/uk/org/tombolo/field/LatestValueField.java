package uk.org.tombolo.field;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.TimedValueId;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.execution.spec.AttributeMatcher;

/**
 * LatestValueField.java
 * Returns the latest TimedValue for a particular Attribute on the given subject, plus metadata
 *
 * The metadata is regarding the attribute.
 */
public class LatestValueField extends ValuesByTimeField implements SingleValueField {
    public LatestValueField(String label, AttributeMatcher attribute) {
        super(label, attribute);
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        return getTimedValue(subject).getValue().toString();
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        TimedValue timedValue = getTimedValue(subject);
        JSONObject obj = new JSONObject();
        obj.put("timestamp", timedValue.getId().getTimestamp().format(TimedValueId.DATE_TIME_FORMATTER));
        obj.put("value", timedValue.getValue());
        JSONArray values = new JSONArray();
        values.add(obj);
        return withinMetadata(values);
    }

    private TimedValue getTimedValue(Subject subject) throws IncomputableFieldException {
        TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(subject, getAttribute());
        if (timedValue == null) {
            throw new IncomputableFieldException(String.format("No TimedValue found for attribute %s", getAttribute().getLabel()));
        }
        return timedValue;
    }
}
