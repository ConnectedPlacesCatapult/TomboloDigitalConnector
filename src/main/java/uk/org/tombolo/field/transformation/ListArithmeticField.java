package uk.org.tombolo.field.transformation;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;
import uk.org.tombolo.recipe.FieldRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Takes as input an operation, and a list of fields. It returns for a given Subject the value resulting from applying
 * the operation on the list of fields' values.
 */
public class ListArithmeticField extends AbstractField implements SingleValueField {
    public enum Operation {mul, add}
    private final List<FieldRecipe> fields;
    private final Operation operation;

    private Map<Operation, Function<List<Double>, Double>> operators;
    private List<SingleValueField> singleValueFields;
    private Function<List<Double>, Double> operator;

    ListArithmeticField(String label, Operation operation, List<FieldRecipe> fields) {
            super(label);
            this.operation = operation;
            this.fields = fields;
        }

        public void initialize() {
            // Initialise operators
            operators = new HashMap<>();
            operators.put(Operation.mul, l -> l.stream().reduce(1.0, (a, b) -> a * b));
            operators.put(Operation.add, l -> l.stream().reduce(0.0, (a, b) -> a + b));

            try {
                this.operator = operators.get(this.operation);
                singleValueFields = new ArrayList<>();

                for (FieldRecipe fieldRecipe: fields) {
                    Field field = fieldRecipe.toField();
                    field.setFieldCache(fieldCache);
                    singleValueFields.add((SingleValueField) field);
                }
            } catch (Exception e) {
                throw new Error("Field not valid", e);
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

    private Double calculateValueForSubject(Subject subject) throws IncomputableFieldException {
        String cachedValue = getCachedValue(subject);
        if (cachedValue != null)
            return Double.parseDouble(cachedValue);

        if (null == singleValueFields || singleValueFields.isEmpty()) { initialize(); }

        List<Double> values = new ArrayList<>();
        for (SingleValueField singleValueField: singleValueFields) {
            values.add(Double.parseDouble(singleValueField.valueForSubject(subject, true)));
        }

        Double retVal = operator.apply(values);

        if (retVal.isNaN()) {
            throw new IncomputableFieldException(String.format("Arithmetic operation %s returned NaN (possible division by zero?)", operation));
        } else if (retVal.isInfinite()) {
            throw new IncomputableFieldException(String.format("Arithmetic operation %s returned Infinity (possible division by zero?)", operation));
        }

        setCachedValue(subject, retVal.toString());
        return retVal;
    }

}
