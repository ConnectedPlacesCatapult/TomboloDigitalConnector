package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

public class LatestValueField implements Field, SingleValueField {
    private String label;
    private AttributeStruct attribute;

    public LatestValueField(String label, AttributeStruct attribute) {
        this.label = label;
        this.attribute = attribute;
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

    @Override
    public String getLabel() {
        return label;
    }

    private Double getValue(Subject subject) {
        TimedValueUtils timedValueUtils = new TimedValueUtils();
        return timedValueUtils.getLatestBySubjectAndAttribute(subject, getAttribute())
                .map(timedValue -> timedValue.getValue())
                .orElse(null);
    }

    private Attribute getAttribute() {
        Attribute attr = AttributeUtils.getByProviderAndLabel(attribute.providerLabel, attribute.attributeLabel);
        if (null == attribute) {
            throw new IllegalArgumentException(String.format("No attribute found for provider %s and label %s", attribute.providerLabel, attribute.attributeLabel));
        } else {
            return attr;
        }
    }

    private static final class AttributeStruct {
        public final String providerLabel;
        public final String attributeLabel;

        AttributeStruct(String providerLabel, String attributeLabel) {
            this.providerLabel = providerLabel;
            this.attributeLabel = attributeLabel;
        }
    }
}
