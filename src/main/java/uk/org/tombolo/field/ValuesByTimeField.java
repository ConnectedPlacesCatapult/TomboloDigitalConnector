package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

public class ValuesByTimeField implements Field, FieldWithProvider {
    protected String label;
    private AttributeStruct attribute;

    public ValuesByTimeField(String label, AttributeStruct attribute) {
        this.label = label;
        this.attribute = attribute;
    }

    public JSONObject jsonValueForSubject(Subject subject) {
        TimedValueUtils timedValueUtils = new TimedValueUtils();
        JSONObject obj = new JSONObject();
        timedValueUtils.getBySubjectAndAttribute(subject, getAttribute()).forEach(timedValue -> {
            obj.put(timedValue.getId().getTimestamp().toString(), timedValue.getValue());
        });
        return withinMetadata(obj);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getHumanReadableName() {
        return getAttribute().getName();
    }

    protected Attribute getAttribute() {
        Attribute attr = AttributeUtils.getByProviderAndLabel(attribute.providerLabel, attribute.attributeLabel);
        if (null == attr) {
            throw new IllegalArgumentException(String.format("No attribute found for provider %s and label %s", attribute.providerLabel, attribute.attributeLabel));
        } else {
            return attr;
        }
    }

    protected JSONObject withinMetadata(JSONObject contents) {
        JSONObject attr = new JSONObject();
        attr.put("name", getHumanReadableName());
        attr.put("provider", getProvider().getName());
        attr.put("values", contents);
        JSONObject obj = new JSONObject();
        obj.put(label, attr);
        return obj;
    }

    @Override
    public Provider getProvider() {
        return getAttribute().getProvider();
    }

    public static final class AttributeStruct {
        public final String providerLabel;
        public final String attributeLabel;

        public AttributeStruct(String providerLabel, String attributeLabel) {
            this.providerLabel = providerLabel;
            this.attributeLabel = attributeLabel;
        }
    }
}
