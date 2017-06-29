package uk.org.tombolo.field.assertion;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.execution.spec.AttributeMatcher;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;

import java.util.List;

/**
 * Returns 1 if subjcet has attribute with value
 */
public class HasFixedAttributeField implements Field, SingleValueField {

    private final String label;
    private AttributeMatcher attribute;
    private final List<String> values;

    Attribute cachedAttribute;

    public HasFixedAttributeField(String label, AttributeMatcher attribute, List<String> values){
        this.label = label;
        this.attribute = attribute;
        this.values = values;
    }

    public void initialize() {
        cachedAttribute = AttributeUtils.getByProviderAndLabel(attribute.providerLabel, attribute.attributeLabel);
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        if (cachedAttribute == null)
            initialize();
        FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, cachedAttribute);
        if (fixedValue != null) {
            for (String value : values) {
                if (fixedValue.getValue().equals(value))
                    return "1";
            }
        }
        return "0";
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        JSONObject obj = new JSONObject();
        obj.put("value", valueForSubject(subject));
        return obj;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
