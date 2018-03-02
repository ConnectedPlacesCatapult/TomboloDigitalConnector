package uk.org.tombolo.field.value;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.TimedValueId;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;
import uk.org.tombolo.recipe.AttributeMatcher;

/**
 * LatestValueField.java
 * Returns the latest TimedValue for a particular Attribute on the given subject, plus metadata
 *
 * The metadata is regarding the attribute.
 */
public class LatestValueField extends BasicValueField implements SingleValueField {
    public LatestValueField(String label, AttributeMatcher attribute) {
        super(label, attribute);
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        return getTimedValue(subject).getValue().toString();
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        TimedValue timedValue = getTimedValue(subject);
        JSONObject obj = new JSONObject();
        if (null != timeStamp && !timeStamp) {
            obj.put(null != this.label ? this.label : "value",
                                            timedValue.getValue());
            return obj;
        }
        obj.put("timestamp", timedValue.getId().getTimestamp().format(TimedValueId.DATE_TIME_FORMATTER));
        obj.put("value", timedValue.getValue());
        return withinMetadata(obj);
    }

    private TimedValue getTimedValue(Subject subject) throws IncomputableFieldException {
        TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(subject, getAttribute());
        if (timedValue == null) {
            throw new IncomputableFieldException(String.format("No TimedValue found for Attribute %s and Subject %s", getAttribute().getLabel(), subject.getName()));
        }
        return timedValue;
    }
}
