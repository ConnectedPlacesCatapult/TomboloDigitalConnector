package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.TimedValueUtils;

/**
 * LatestValueField.java
 * Returns the latest TimedValue for a particular Attribute on the given subject, plus metadata
 *
 * The metadata is regarding the attribute.
 */
public class LatestValueField extends ValuesByTimeField implements SingleValueField {
    public LatestValueField(String label, AttributeStruct attribute) {
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
        TimedValueUtils timedValueUtils = new TimedValueUtils();
        return timedValueUtils.getLatestBySubjectAndAttribute(subject, getAttribute())
                .map(timedValue -> timedValue.getValue())
                .orElse(null);
    }
}
