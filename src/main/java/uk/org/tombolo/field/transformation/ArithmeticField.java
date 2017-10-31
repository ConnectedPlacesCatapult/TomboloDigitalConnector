package uk.org.tombolo.field.transformation;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.*;
import uk.org.tombolo.recipe.FieldRecipe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Takes as input an operation, and two fields. It returns for a given Subject the value resulting from applying
 * the operation on the two field values.
 */
public class ArithmeticField extends AbstractField implements ParentField {

    public enum Operation {div, mul, add, sub}
    private final FieldRecipe field1;
    private final FieldRecipe field2;
    private final Operation operation;

    private Map<Operation, BiFunction<Double, Double, Double>> operators;
    private SingleValueField singleValueField1;
    private SingleValueField singleValueField2;
    private BiFunction<Double, Double, Double> operator;

    ArithmeticField(String label, Operation operation, FieldRecipe field1, FieldRecipe field2) {
        super(label);
        this.operation = operation;
        this.field1 = field1;
        this.field2 = field2;
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
            this.singleValueField1 = (SingleValueField) field1.toField();
            singleValueField1.setFieldCache(fieldCache);
            this.singleValueField2 = (SingleValueField) field2.toField();
            singleValueField2.setFieldCache(fieldCache);
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

        if (null == singleValueField1) { initialize(); }
        Double retVal = operator.apply(
                Double.parseDouble(singleValueField1.valueForSubject(subject, true)),
                Double.parseDouble(singleValueField2.valueForSubject(subject, true)));

        if (retVal.isNaN()) {
            throw new IncomputableFieldException(String.format("Arithmetic operation %s returned NaN (possible division by zero?)", operation));
        } else if (retVal.isInfinite()) {
            throw new IncomputableFieldException(String.format("Arithmetic operation %s returned Infinity (possible division by zero?)", operation));
        }

        setCachedValue(subject, retVal.toString());
        return retVal;
    }

    @Override
    public List<Field> getChildFields() {
        if (singleValueField1 == null)
            initialize();
        return Arrays.asList(singleValueField1, singleValueField2);
    }

}
