package uk.org.tombolo.field;

import uk.org.tombolo.core.Subject;

/**
 *
 */
public class SingleValuePredefinedField extends BasicPredefinedField implements Field, SingleValueField, PredefinedField{

    public SingleValuePredefinedField(String label, String recipe) {
        super(label, recipe);
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        if (field == null)
            initialize();
        if (field instanceof SingleValueField){
            return ((SingleValueField) field).valueForSubject(subject);
        }
        throw new IncomputableFieldException("Predefined field must be Single Value Field");
    }
}
