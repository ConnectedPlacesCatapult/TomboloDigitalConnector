package uk.org.tombolo.field.aggregation;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.*;
import uk.org.tombolo.recipe.FieldRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Field for providing backed off values when none exist.
 * An example back-off would be mapping to a value for a parent geography.
 */
public class BackOffField extends AbstractField implements ParentField {

    private List<FieldRecipe> fields;

    private List<Field> materialisedFields;

    public BackOffField(String label, List<FieldRecipe> fields){
        super(label);
        this.fields = fields;
    }

    public void initialize() {
        this.materialisedFields = new ArrayList<>();
        for (FieldRecipe field : fields) {
            try {
                materialisedFields.add(field.toField());
            } catch (ClassNotFoundException e) {
                throw new Error("Field not valid");
            }
        }
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {

        if (materialisedFields == null)
            initialize();
        for (Field field : materialisedFields) {
            String value = null;
            try {
                value = ((SingleValueField) field).valueForSubject(subject, timeStamp);
            } catch (IncomputableFieldException e){
                // Keep calm and continue processing ... we will back-off
                continue;
            }
            if (value != null)
                return value;
        }
        throw new IncomputableFieldException("No Backed-off value found");
    }

    @Override
    public List<Field> getChildFields() {
        if (materialisedFields == null)
            initialize();
        return materialisedFields;
    }
}
