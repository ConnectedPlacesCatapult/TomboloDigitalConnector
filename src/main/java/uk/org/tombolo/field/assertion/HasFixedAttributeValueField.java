package uk.org.tombolo.field.assertion;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;
import uk.org.tombolo.recipe.AttributeMatcher;
import uk.org.tombolo.recipe.FieldRecipe;

import java.util.List;

/**
 * Returns 1 if subject has attribute with value
 */
public class HasFixedAttributeValueField extends AbstractField {

    private AttributeMatcher attribute;
    private final List<String> values;
    private FieldRecipe field;

    private Attribute cachedAttribute;
    private SingleValueField singleValueField;

    public HasFixedAttributeValueField(String label, AttributeMatcher attribute, List<String> values, FieldRecipe field){
        super(label);
        this.attribute = attribute;
        this.values = values;
        this.field = field;
    }

    public void initialize() {
        cachedAttribute = AttributeUtils.getByProviderAndLabel(attribute.provider, attribute.label);

        try {
            this.singleValueField = (SingleValueField) field.toField();
            singleValueField.setFieldCache(fieldCache);
        } catch (Exception e) {
            throw new Error("Field not valid", e);
        }
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
                    String fieldValue = singleValueField.valueForSubject(subject, timeStamp);
                    setCachedValue(subject, fieldValue);
                    return fieldValue;
                }
            }
        }
        setCachedValue(subject, "0");
        return "0";
    }
}
