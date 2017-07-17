package uk.org.tombolo.field.transformation;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.field.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Takes a list of fields as input and returns a field consisting of the sum of the other fields
 */
public class FieldValueSumField extends AbstractField implements SingleValueField, ParentField {
    String name;
    List<FieldSpecification> fieldSpecifications;
    List<Field> fields;

    public FieldValueSumField(String label, String name, List<FieldSpecification> fieldSpecifications) {
        super(label);
        this.name = name;
        this.fieldSpecifications = fieldSpecifications;
    }

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
        JSONArray array = new JSONArray();
        array.add(obj);
        return withinMetadata(array);
    }

    protected JSONObject withinMetadata(JSONArray contents) {
        JSONObject obj = new JSONObject();
        obj.put(label, contents);
        return obj;
    }

    private Double sumFields(Subject subject) throws IncomputableFieldException {
        if (fields == null)
            initialize();
        Double sum = 0d;
        for (Field field : fields) {
            if (!(field instanceof SingleValueField))
                throw new IncomputableFieldException("Field sum only valid for single value fields");
            sum += Double.parseDouble(((SingleValueField)field).valueForSubject(subject));
        }
        return sum;
    }

    @Override
    public List<Field> getChildFields() {
        if (null == fields) { initialize(); }
        return fields;
    }
}
