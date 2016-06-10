package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.TimedValueUtils;

public class LatestValueField extends ValuesByTimeField implements SingleValueField {
    public LatestValueField(String label, AttributeStruct attribute) {
        super(label, attribute);
    }

    @Override
    public String valueForSubject(Subject subject) {
        return getValue(subject).toString();
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) {
        JSONObject obj = new JSONObject();
        obj.put("latest", getValue(subject));
        return obj;
    }

    private Double getValue(Subject subject) {
        TimedValueUtils timedValueUtils = new TimedValueUtils();
        return timedValueUtils.getLatestBySubjectAndAttribute(subject, getAttribute())
                .map(timedValue -> timedValue.getValue())
                .orElse(null);
    }
}
