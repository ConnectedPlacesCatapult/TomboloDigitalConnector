package uk.org.tombolo.field;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.DatasourceSpecification;
import uk.org.tombolo.execution.spec.FieldSpecification;

import java.util.ArrayList;
import java.util.List;

/**
 * Takes a list of fields as input and returns a field consisting of the sum of the other fields
 */
public class FieldValueSumField implements SingleValueField, ParentField {
    String label;
    String name;
    List<FieldSpecification> fieldSpecifications;
    List<Field> fields;

    public FieldValueSumField(String label, String name, List<FieldSpecification> fieldSpecifications) {
        this.label = label;
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

    @Override
    public String getLabel() {
        return label;
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
