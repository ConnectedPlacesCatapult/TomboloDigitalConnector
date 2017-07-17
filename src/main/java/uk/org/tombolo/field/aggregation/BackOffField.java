package uk.org.tombolo.field.aggregation;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;

import java.util.ArrayList;
import java.util.List;

/**
 * Field for providing backed off values when none exist.
 * An example back-off would be mapping to a value for a parent geography.
 */
public class BackOffField extends AbstractField implements SingleValueField {

    private List<FieldSpecification> fields;

    private List<Field> materialisedFields;

    public BackOffField(String label, List<FieldSpecification> fields){
        super(label);
        this.fields = fields;
    }

    public void initialize() {
        this.materialisedFields = new ArrayList<>();
        for (FieldSpecification field : fields) {
            try {
                materialisedFields.add(field.toField());
            } catch (ClassNotFoundException e) {
                throw new Error("Field not valid");
            }
        }
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        if (materialisedFields == null)
            initialize();
        for (Field field : materialisedFields) {
            String value = null;
            try {
                value = ((SingleValueField) field).valueForSubject(subject);
            } catch (IncomputableFieldException e){
                // Keep calm and continue processing ... we will back-off
            }
            if (value != null)
                return ((SingleValueField) field).valueForSubject(subject);
        }
        throw new IncomputableFieldException("No Backed-off value found");
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        JSONObject obj = new JSONObject();
        obj.put("value", valueForSubject(subject));
        return obj;
    }
}
