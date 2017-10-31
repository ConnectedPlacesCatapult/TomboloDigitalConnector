package uk.org.tombolo.field.transformation;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.ParentField;
import uk.org.tombolo.field.SingleValueField;
import uk.org.tombolo.field.value.FixedValueField;
import uk.org.tombolo.recipe.FieldRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Takes a list of fields as input and returns a field consisting of the sum of the other fields
 */
public class FieldValueSumField extends FixedValueField implements ParentField {
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

    private Double sumFields(Subject subject) throws IncomputableFieldException {
        String cachedValue = getCachedValue(subject);
        if (cachedValue != null)
            return Double.parseDouble(cachedValue);
        if (sumFields == null)
            initialize();
        Double sum = 0d;
        for (Field field : sumFields) {
            if (!(field instanceof SingleValueField))
                throw new IncomputableFieldException("Field sum only valid for single value fields");
            sum += Double.parseDouble(((SingleValueField)field).valueForSubject(subject, true));
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
