package uk.org.tombolo.field.transformation;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Takes as input an operation, and two fields. It returns for a given Subject the value resulting from applying
 * the operation on the two field values.
 */
public class ArithmeticField extends AbstractField implements SingleValueField {
    public static enum Operation {div, mul, add, sub}
    private final FieldRecipe fieldSpecification1;
    private final FieldRecipe fieldSpecification2;
    private final Operation operation;

    private Map<Operation, BiFunction<Double, Double, Double>> operators;
    private SingleValueField field1;
    private SingleValueField field2;
    private BiFunction<Double, Double, Double> operator;

    ArithmeticField(String label, Operation operation, FieldRecipe fieldSpecification1, FieldRecipe fieldSpecification2) {
        super(label);
        this.fieldSpecification1 = fieldSpecification1;
        this.operation = operation;
        this.fieldSpecification2 = fieldSpecification2;
    }

    public void initialize() {
        // Initialise operators
        operators = new HashMap<>();
        operators.put(Operation.div, (a, b) -> a / b);
        operators.put(Operation.mul, (a, b) -> a * b);
        operators.put(Operation.add, (a, b) -> a + b);
        operators.put(Operation.sub, (a, b) -> a - b);

        try {
            this.operator = operators.get(this.operation);
            this.field1 = (SingleValueField) fieldSpecification1.toField();
            field1.setFieldCache(fieldCache);
            this.field2 = (SingleValueField) fieldSpecification2.toField();
            field2.setFieldCache(fieldCache);
        } catch (Exception e) {
            throw new Error("Field not valid: " + e.getClass());
        }
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        return calculateValueForSubject(subject).toString();
    }

    private Double calculateValueForSubject(Subject subject) throws IncomputableFieldException {
        String cachedValue = getCachedValue(subject);
        if (cachedValue != null)
            return Double.parseDouble(cachedValue);

        if (null == field1) { initialize(); }
        Double retVal = operator.apply(
                Double.parseDouble(field1.valueForSubject(subject, true)),
                Double.parseDouble(field2.valueForSubject(subject, true)));

        if (retVal.isNaN()) {
            throw new IncomputableFieldException(String.format("Arithmetic operation %s returned NaN (possible division by zero?)", operation));
        } else if (retVal.isInfinite()) {
            throw new IncomputableFieldException(String.format("Arithmetic operation %s returned Infinity (possible division by zero?)", operation));
        }

        setCachedValue(subject, retVal.toString());
        return retVal;
    }

}
