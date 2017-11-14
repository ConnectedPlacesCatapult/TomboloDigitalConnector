package uk.org.tombolo.field.modelling;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;
import uk.org.tombolo.recipe.DatasourceRecipe;

import java.util.List;

/**
 * This is an extension of the {@link BasicModellingField} in order to support the use of modelling fields in
 * further calculations. E.g. in a field where we would like to algorithmically combine the values of two or more
 * modelling fields.
 */
public class SingleValueModellingField extends BasicModellingField implements SingleValueField {

    public SingleValueModellingField(String label, String recipe, List<DatasourceRecipe> datasources) {
        super(label, recipe, datasources);
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        if (field == null)
            initialize();
        if (field instanceof SingleValueField){
            String cachedValue = getCachedValue(subject);
            if (cachedValue == null) {
                cachedValue = ((SingleValueField) field).valueForSubject(subject, timeStamp);
                setCachedValue(subject, cachedValue);
            }
            return cachedValue;
        }
        throw new IncomputableFieldException("Modelling field must be Single Value Field");
    }
}
