package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.execution.spec.AttributeMatcher;

import java.time.format.DateTimeFormatter;

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
    public String valueForSubject(Subject subject) {
        Double value = getValue(subject);
        if (null == value)
            return null;
        return value.toString();
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) {
        TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(subject, getAttribute());
        if (timedValue == null)
            return withinMetadata(null);
        JSONObject obj = new JSONObject();
        obj.put("timestamp", timedValue.getId().getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        obj.put("value", timedValue.getValue());
        return withinMetadata(obj);
    }

    private Double getValue(Subject subject) {
        TimedValue timedValue = TimedValueUtils.getLatestBySubjectAndAttribute(subject, getAttribute());
        return (null != timedValue) ? timedValue.getValue() : null;
    }
}
