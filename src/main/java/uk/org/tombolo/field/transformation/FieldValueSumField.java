package uk.org.tombolo.field.transformation;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.*;
import uk.org.tombolo.recipe.FieldRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Takes a list of fields as input and returns a field consisting of the sum of the other fields
 */
public class FieldValueSumField extends AbstractField implements ParentField, SingleValueField {
    private static Logger log = LoggerFactory.getLogger(FieldValueSumField.class);

    String name;
    List<FieldRecipe> fields;
    List<Field> sumFields;

    public FieldValueSumField(String label, String name, List<FieldRecipe> fields) {
        super(label);
        this.name = name;
        this.fields = fields;
    }

    public void initialize() {
        this.sumFields = new ArrayList<>();
        for (FieldRecipe recipe : fields) {
            try {
                Field field = recipe.toField();
                field.setFieldCache(fieldCache);
                sumFields.add(field);
            } catch (ClassNotFoundException e) {
                throw new Error("Field not valid");
            }
        }
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {

        return sumFields(subject).toString();
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        JSONObject obj = new JSONObject();
        obj.put(null != this.label ? this.label : "value", sumFields(subject));
        return obj;
    }

    private Double sumFields(Subject subject) throws IncomputableFieldException {
        String cachedValue = getCachedValue(subject);
        if (cachedValue != null)
            return Double.parseDouble(cachedValue);
        if (sumFields == null)
            initialize();
        Double sum = 0d;
        for (Field field : sumFields) {
            String value = null;
            try {
                value = ((SingleValueField)field).valueForSubject(subject, true);
                sum += Double.parseDouble(value);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Field sum only valid for single value fields");
            } catch (NullPointerException | NumberFormatException e) {
                log.warn("Incomputable field not included in operation for subject {} ({}), value {} cannot be " +
                        "converted to numeric type.", subject.getName(), subject.getId(), value);
            }
        }
        setCachedValue(subject, sum.toString());
        return sum;
    }

    @Override
    public List<Field> getChildFields() {
        if (sumFields == null)
            initialize();
        return sumFields;
    }
}
