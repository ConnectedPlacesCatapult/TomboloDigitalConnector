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

import java.util.ArrayList;
import java.util.List;

/**
 * Returns true if a subject has any fixed value for the listed attributes.
 */
public class HasFixedAttributeField extends AbstractField implements SingleValueField {

    private List<AttributeMatcher> attributes;

    private List<Attribute> cachedAttributes;

    public HasFixedAttributeField(String label, List<AttributeMatcher> attributes){
        super(label);
        this.attributes = attributes;
    }

    public void initialise(){
        cachedAttributes = new ArrayList<>();
        attributes.forEach(attribute -> cachedAttributes
                .add(AttributeUtils.getByProviderAndLabel(attribute.providerLabel, attribute.attributeLabel)));
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
                setCachedValue(subject, "1");
                return "1";
            }
        }
        setCachedValue(subject, "0");
        return "0";
    }

}
