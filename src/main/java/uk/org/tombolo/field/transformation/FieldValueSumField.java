package uk.org.tombolo.field.transformation;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.value.FixedValueField;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.field.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Takes a list of fields as input and returns a field consisting of the sum of the other fields
 */
public class FieldValueSumField extends FixedValueField implements SingleValueField, ParentField {
    String name;
    List<FieldRecipe> fieldSpecifications;
    List<Field> fields;

    public FieldValueSumField(String label, String name, List<FieldRecipe> fieldSpecifications) {
        super(label);
        this.name = name;
        this.fieldSpecifications = fieldSpecifications;
    }

    public void initialize() {
        this.fields = new ArrayList<>();
        for (FieldRecipe fieldSpec : fieldSpecifications) {
            try {
                Field field = fieldSpec.toField();
                field.setFieldCache(fieldCache);
                fields.add(field);
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
        if (fields == null)
            initialize();
        Double sum = 0d;
        for (Field field : fields) {
            if (!(field instanceof SingleValueField))
                throw new IncomputableFieldException("Field sum only valid for single value fields");
            sum += Double.parseDouble(((SingleValueField)field).valueForSubject(subject, true));
        }
        setCachedValue(subject, sum.toString());
        return sum;
    }

    @Override
    public List<Field> getChildFields() {
        if (null == fields) { initialize(); }
        return fields;
    }
}
