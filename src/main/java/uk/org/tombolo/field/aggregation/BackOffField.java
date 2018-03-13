package uk.org.tombolo.field.aggregation;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;
import uk.org.tombolo.field.*;
import uk.org.tombolo.recipe.FieldRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Field for providing backed off values when none exist.
 * An example back-off would be mapping to a value for a parent geography.
 *
 * It can have many levels to back off to as long as they make sense for the specific task.
 */
public class BackOffField extends AbstractField implements SingleValueField, ParentField {

    private List<FieldRecipe> fields;

    private List<Field> singleValueFields;

    public BackOffField(String label, List<FieldRecipe> fields){
        super(label);
        this.fields = fields;
    }

    public void initialize() {
        this.singleValueFields = new ArrayList<>();
        for (FieldRecipe field : fields) {
            try {
                singleValueFields.add(field.toField());
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Field class not found.");
            }
        }
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {

        if (singleValueFields == null)
            initialize();
        for (Field field : singleValueFields) {
            String value;
            try {
                value = ((SingleValueField) field).valueForSubject(subject, timeStamp);
            } catch (IncomputableFieldException e){
                // Keep calm and continue processing ... we will back-off
                continue;
            } catch (ClassCastException cce) {
                throw new IllegalArgumentException("Parameters for BackOffField must be of type SingleValueField.");
            }
            if (value != null)
                return value;
        }
        throw new IncomputableFieldException("No Backed-off value found");
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        JSONObject obj = new JSONObject();
        obj.put(null != this.label ? this.label : "value" , valueForSubject(subject, timeStamp));
        return obj;
    }
  
    public List<Field> getChildFields() {
        if (singleValueFields == null)
            initialize();
        return singleValueFields;
    }
}
