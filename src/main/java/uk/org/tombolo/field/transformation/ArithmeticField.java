package uk.org.tombolo.field.transformation;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Takes as input an operation, and two fields. It returns for a given Subject the value resulting from applying
 * the operation on the two field values.
 */
public class ArithmeticField implements SingleValueField {
    public static enum Operation {div, mul, add, sub}
    private final String label;
    private final FieldSpecification fieldSpecification1;
    private final FieldSpecification fieldSpecification2;
    private final Operation operation;

    private Map<Operation, BiFunction<Double, Double, Double>> operators;
    private SingleValueField field1;
    private SingleValueField field2;
    private BiFunction<Double, Double, Double> operator;

    ArithmeticField(String label, Operation operation, FieldSpecification fieldSpecification1, FieldSpecification fieldSpecification2) {
        this.label = label;
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
            this.field2 = (SingleValueField) fieldSpecification2.toField();
        } catch (Exception e) {
            throw new Error("Field not valid: " + e.getClass());
        }
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        if (null == field1) { initialize(); }
        JSONObject obj = new JSONObject();
        obj.put(this.label,
                calculateValueForSubject(subject));
        return obj;
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        if (null == field1) { initialize(); }
        return calculateValueForSubject(subject).toString();
    }

    private Double calculateValueForSubject(Subject subject) throws IncomputableFieldException {
        Double retVal = operator.apply(
                Double.parseDouble(field1.valueForSubject(subject)),
                Double.parseDouble(field2.valueForSubject(subject)));

        if (retVal.isNaN()) {
            throw new IncomputableFieldException(String.format("Arithmetic operation %s returned NaN (possible division by zero?)", operation));
        } else if (retVal.isInfinite()) {
            throw new IncomputableFieldException(String.format("Arithmetic operation %s returned Infinity (possible division by zero?)", operation));
        }

        return retVal;
    }

    @Override
    public String getLabel() {
        return this.label;
    }
}
