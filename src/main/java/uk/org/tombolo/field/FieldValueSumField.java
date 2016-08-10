package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;

import java.util.List;

/**
 * Takes a list of fields as input and returns a field consisting of the sum of the other fields
 */
public class FieldValueSumField implements SingleValueField {
    String label;
    String name;
    List<SingleValueField> fields;

    FieldValueSumField(String label, String name, List<SingleValueField> fields) {
        this.label = label;
        this.fields = fields;
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
        Double sum = 0d;
        for (SingleValueField field : fields) {
            sum += Double.parseDouble(field.valueForSubject(subject));
        }
        return sum;
    }
}
