package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.FieldSpecification;

import java.util.ArrayList;
import java.util.List;

/**
 * Takes a list of fields as input and returns a field consisting of the sum of the other fields
 */
public class FieldValueSumField implements SingleValueField {
    String label;
    String name;
    List<FieldSpecification> fieldSpecifications;
    List<Field> fields;

    public void initialize() {
        this.fields = new ArrayList<>();
        for (FieldSpecification fieldSpec : fieldSpecifications) {
            try {
                fields.add(fieldSpec.toField());
            } catch (ClassNotFoundException e) {
                throw new Error("Field not valid");
            }
        }
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        return sumFields(subject).toString();
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        JSONObject obj = new JSONObject();
        obj.put("value", sumFields(subject));
        return obj;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getHumanReadableName() {
        return name;
    }

    private Double sumFields(Subject subject) throws IncomputableFieldException {
        if (fields == null)
            initialize();
        Double sum = 0d;
        for (Field field : fields) {
            sum += Double.parseDouble(((SingleValueField)field).valueForSubject(subject));
        }
        return sum;
    }
}
