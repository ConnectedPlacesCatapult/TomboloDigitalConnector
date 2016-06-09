package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

public class LatestValueField implements Field {
    private String label;
    private TimedValueUtils timedValueUtils = new TimedValueUtils();
    private JSONObject data;

    @Override
    public void initialize(String label, JSONObject data) {
        this.label = label;
        this.data = data;
    }

    @Override
    public String valueForSubject(Subject subject) {
        return timedValueUtils.getLatestBySubjectAndAttribute(subject, getAttribute())
                .map(timedValue -> timedValue.getValue().toString())
                .orElse(null);
    }

    @Override
    public String getLabel() {
        return label;
    }

    private Attribute getAttribute() {
        JSONObject attrObject = (JSONObject) data.get("attribute");
        String providerLabel = (String) attrObject.get("providerLabel");
        String attributeLabel = (String) attrObject.get("attributeLabel");
        Attribute attribute = AttributeUtils.getByProviderAndLabel(providerLabel, attributeLabel);
        if (null == attribute) {
            throw new IllegalArgumentException(String.format("No attribute found for provider %s and label %s", attributeLabel, providerLabel));
        } else {
            return attribute;
        }
    }
}
