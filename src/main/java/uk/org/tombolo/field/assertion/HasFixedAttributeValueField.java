package uk.org.tombolo.field.assertion;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.recipe.AttributeMatcher;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;

import java.util.List;

/**
 * Returns 1 if subject has attribute with value
 */
public class HasFixedAttributeValueField extends AbstractField implements SingleValueField {

    private AttributeMatcher attribute;
    private final List<String> values;

    private Attribute cachedAttribute;

    public HasFixedAttributeValueField(String label, AttributeMatcher attribute, List<String> values){
        super(label);
        this.attribute = attribute;
        this.values = values;
    }

    public void initialize() {
        cachedAttribute = AttributeUtils.getByProviderAndLabel(attribute.providerLabel, attribute.attributeLabel);
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        String cachedValue = getCachedValue(subject);
        if (cachedValue != null)
            return cachedValue;
        if (cachedAttribute == null)
            initialize();
        FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, cachedAttribute);
        if (fixedValue != null) {
            for (String value : values) {
                if (fixedValue.getValue().equals(value)) {
                    setCachedValue(subject, "1");
                    return "1";
                }
            }
        }
        setCachedValue(subject, "0");
        return "0";
    }
}
