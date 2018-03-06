package uk.org.tombolo.field.aggregation;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.ResizableDoubleArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.field.*;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.recipe.SubjectRecipe;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GeographicAggregationField.java
 * A field that calculates the value of a field for every other Subject within its geometry and performs some
 * aggregation function on the result.
 *
 * So far, `sum` and `mean` are implemented.
 */
public class GeographicAggregationField extends AbstractField implements SingleValueField, ParentField {
    private static Logger log = LoggerFactory.getLogger(GeographicAggregationField.class);

    public enum AggregationFunction {sum, mean}
    private final SubjectRecipe subject;
    private final FieldRecipe field;
    private final AggregationFunction function;

    private Map<AggregationFunction, MathArrays.Function> aggregators;
    private SingleValueField singleValueField;
    private MathArrays.Function aggregator;
    private SubjectType aggregatorSubjectType;

    GeographicAggregationField(String label, SubjectRecipe subject, AggregationFunction function, FieldRecipe field) {
        super(label);
        this.subject = subject;
        this.field = field;
        this.function = function;
    }

    public void initialize() {
        aggregatorSubjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(subject.getProvider(), subject.getSubjectType());

        // Initialise aggregators
        aggregators = new HashMap<>();
        aggregators.put(AggregationFunction.sum, new Sum());
        aggregators.put(AggregationFunction.mean, new Mean());

        try {
            this.aggregator = aggregators.get(this.function);
            this.singleValueField = (SingleValueField) field.toField();
            singleValueField.setFieldCache(fieldCache);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Function not supported. Supporting {sum, mean}");
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Field class not found.", e);
        } catch (ClassCastException e){
            throw new IllegalArgumentException("Field must be SingleValueField");
        }
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        JSONObject obj = new JSONObject();
        obj.put(this.label, getDoubleValueForSubject(subject));
        return obj;
    }

    private Double aggregateSubjects(MathArrays.Function aggregator, List<Subject> aggregationSubjects) throws IncomputableFieldException {
        ResizableDoubleArray doubles = new ResizableDoubleArray();

        for (Subject subject : aggregationSubjects) {
            String value = null;
            try {
                value = singleValueField.valueForSubject(subject, true);
                doubles.addElement(Double.parseDouble(value));
            } catch (IncomputableFieldException e) {
                log.warn("Incomputable field not included in aggregation for subject {} ({})",
                        subject.getName(),
                        subject.getId());
            } catch (NullPointerException | NumberFormatException e) {
                log.warn("Incomputable field not included in aggregation for subject {} ({}), value {} cannot be " +
                                "converted to numeric type.", subject.getName(), subject.getId(), value);
            }
        }

        Double retVal = doubles.compute(aggregator);

        if (retVal.isNaN()) {
            throw new IncomputableFieldException(String.format("Aggregation function %s returned NaN (possible division by zero?)", function));
        } else if (retVal.isInfinite()) {
            throw new IncomputableFieldException(String.format("Aggregation function %s returned Infinity (possible division by zero?)", function));
        }

        return retVal;
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        return getDoubleValueForSubject(subject).toString();
    }

    private Double getDoubleValueForSubject(Subject subject) throws IncomputableFieldException {
        if (null == singleValueField) { initialize(); }
        String cachedValue = getCachedValue(subject);
        if (cachedValue != null)
            return Double.parseDouble(cachedValue);
        Double value = aggregateSubjects(aggregator, getAggregationSubjects(subject));
        if (value != null)
            setCachedValue(subject, value.toString());
        return value;
    }

    private List<Subject> getAggregationSubjects(Subject subject) throws IncomputableFieldException {
        return SubjectUtils.subjectsWithinSubject(aggregatorSubjectType, subject);
    }

    @Override
    public List<Field> getChildFields() {
        if (singleValueField == null)
            initialize();
        return Collections.singletonList(singleValueField);
    }

}
