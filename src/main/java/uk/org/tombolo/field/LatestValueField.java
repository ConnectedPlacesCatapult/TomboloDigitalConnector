package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.execution.spec.AttributeMatcher;

import java.util.Optional;

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
        JSONObject obj = new JSONObject();
        obj.put("latest", getValue(subject));
        return withinMetadata(obj);
    }

    private Double getValue(Subject subject) {
        return TimedValueUtils.getLatestBySubjectAndAttribute(subject, getAttribute())
                .map(timedValue -> timedValue.getValue())
                .orElse(null);
    }
}
