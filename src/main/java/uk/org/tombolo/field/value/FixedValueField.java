package uk.org.tombolo.field.value;

import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.recipe.AttributeMatcher;

/**
 * For a given subject, returns the {@link FixedValue} of a specified attribute.
 */
public class FixedValueField extends ValuesByTimeField {

    public FixedValueField(String label) {
        super(label);
    }

    public FixedValueField(String label, AttributeMatcher attribute) {
        super(label, attribute);
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        return getFixedValue(subject).getValue().toString();
    }

    private FixedValue getFixedValue(Subject subject) throws IncomputableFieldException {
        FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, getAttribute());
        if (fixedValue == null) {
            throw new IncomputableFieldException(String.format("No FixedValue found for attribute %s", getAttribute().getLabel()));
        }
        return fixedValue;
    }
}
