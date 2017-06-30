package uk.org.tombolo.field.aggregation;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;

import java.util.List;

/**
 * Field for providing backed off values when none exist.
 */
public class BackOffField implements Field, SingleValueField {

    private String label;
    private List<FieldSpecification> fields;

    private List<Field> materialisedFields;

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        return null;
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        return null;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
