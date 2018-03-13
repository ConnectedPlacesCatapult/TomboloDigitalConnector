package uk.org.tombolo.field.transformation;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.*;
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
public class ListArithmeticField extends AbstractField implements SingleValueField, ParentField {
    private static Logger log = LoggerFactory.getLogger(ListArithmeticField.class);

    // Syntactic sugar to accept both add and sum
    public enum Operation {mul, add, sum}
    private final List<FieldRecipe> fields;
    private final Operation operation;

    private Map<Operation, Function<List<Double>, Double>> operators;
    private List<Field> singleValueFields;
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
        operators.put(Operation.sum, l -> l.stream().reduce(0.0, (a, b) -> a + b));

        try {
            this.operator = operators.get(this.operation);
            singleValueFields = new ArrayList<>();

            for (FieldRecipe fieldRecipe: fields) {
                SingleValueField field = (SingleValueField) fieldRecipe.toField();
                field.setFieldCache(fieldCache);
                singleValueFields.add(field);
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Field class not found.", e);
        } catch (ClassCastException e){
            throw new IllegalArgumentException("Field must be SingleValueField", e);
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
        String value = null;
        for (Field singleValueField: singleValueFields) {
            try {
                value = ((SingleValueField) singleValueField).valueForSubject(subject, true);
                values.add(Double.parseDouble(value));
            } catch (NullPointerException | NumberFormatException e) {
                log.warn("Value {} not included in operation for subject {} ({}), cannot be converted to numeric type.",
                        subject.getName(), subject.getId(), value);
            }
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

    @Override
    public List<Field> getChildFields() {
        if (singleValueFields == null)
            initialize();
        return singleValueFields;
    }
}
