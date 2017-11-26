package uk.org.tombolo.field.transformation;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.*;
import uk.org.tombolo.recipe.FieldRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Field for calculating a linear combination of sub-fields:
 *
 * linear-combination := scalar[1] * field[1] + scalar[2] * field[2] + ...
 *
 * See further: https://en.wikipedia.org/wiki/Linear_combination
 */
public class LinearCombinationField extends AbstractField implements SingleValueField, ParentField {

    private final List<FieldRecipe> fields;
    private final List<Float> scalars;

    private List<Field> singleValueFields;

    LinearCombinationField(String label, List<Float> scalars, List<FieldRecipe> fields) {
        super(label);
        this.scalars =scalars;
        this.fields = fields;
    }

    public void initialize() {
        if (scalars.size() != fields.size())
            throw new Error("For LinearCombinationField, scalars and fields must have same length");

        try {
            singleValueFields = new ArrayList<>();

            for (FieldRecipe fieldRecipe: fields) {
                Field field = fieldRecipe.toField();
                if (!(field instanceof SingleValueField))
                    throw new IncomputableFieldException("Parameters for LinearCombinationField must be of type SingleValueField");
                field.setFieldCache(fieldCache);
                singleValueFields.add(field);
            }
        } catch (Exception e) {
            throw new Error(e.getMessage(), e);
        }
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        JSONObject obj = new JSONObject();
        obj.put(this.label, calculateValueForSubject(subject));
        return obj;
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        return calculateValueForSubject(subject).toString();
    }

    protected Double calculateValueForSubject(Subject subject) throws IncomputableFieldException {
        String cachedValue = getCachedValue(subject);
        if (cachedValue != null)
            return Double.parseDouble(cachedValue);

        if (null == singleValueFields || singleValueFields.isEmpty()) { initialize(); }

        Double linearCombination = 0.0d;
        for (int i=0; i<singleValueFields.size(); i++) {
            String value;
            try {
                value = ((SingleValueField) singleValueFields.get(i)).valueForSubject(subject, null);
                linearCombination +=
                        scalars.get(i)
                                * ((value == null)? 0.0d : Double.parseDouble(value));
            }catch (IncomputableFieldException e){
                // Sub-field was not computable
                // Nothing added to the linear combination
            }
        }
        return linearCombination;
    }

    @Override
    public List<Field> getChildFields() {
        if (singleValueFields == null)
            initialize();
        return singleValueFields;
    }
}
