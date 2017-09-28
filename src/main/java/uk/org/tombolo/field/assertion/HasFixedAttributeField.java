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

import java.util.ArrayList;
import java.util.List;

/**
 * Returns true if a subject has any fixed value for the listed attributes.
 */
public class HasFixedAttributeField extends AbstractField {

    private List<AttributeMatcher> attributes;
    private FieldRecipe field;

    private List<Attribute> cachedAttributes;
    private SingleValueField singleValueField;

    public HasFixedAttributeField(String label, List<AttributeMatcher> attributes, FieldRecipe field){
        super(label);
        this.attributes = attributes;
        this.field = field;
    }

    public void initialise(){
        cachedAttributes = new ArrayList<>();
        attributes.forEach(attribute -> cachedAttributes
                .add(AttributeUtils.getByProviderAndLabel(attribute.provider, attribute.label)));

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
        if (cachedAttributes == null)
            initialise();
        for (Attribute cachedAttribute : cachedAttributes) {
            FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, cachedAttribute);
            if (fixedValue != null) {
                String fieldValue = singleValueField.valueForSubject(subject, timeStamp);
                setCachedValue(subject, fieldValue);
                return fieldValue;
            }
        }
        setCachedValue(subject, "0");
        return "0";
    }

}
