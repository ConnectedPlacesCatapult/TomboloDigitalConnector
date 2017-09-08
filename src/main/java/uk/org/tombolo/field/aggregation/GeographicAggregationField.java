package uk.org.tombolo.field.aggregation;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.ResizableDoubleArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;
import uk.org.tombolo.recipe.FieldRecipe;

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
public class GeographicAggregationField extends AbstractField {
    private static Logger log = LoggerFactory.getLogger(GeographicAggregationField.class);

    public static enum AggregationFunction {sum, mean}
    private final String aggregationSubjectProvider;
    private final String aggregationSubjectType;
    private final FieldRecipe field;
    private final AggregationFunction aggregationFunction;

    private Map<AggregationFunction, MathArrays.Function> aggregators;
    private SingleValueField singleValueField;
    private MathArrays.Function aggregator;
    private SubjectType aggregatorSubjectType;

    GeographicAggregationField(String label, String aggregationSubjectProvider, String aggregationSubjectType, AggregationFunction aggregationFunction, FieldRecipe field) {
        super(label);
        this.aggregationSubjectProvider = aggregationSubjectProvider;
        this.aggregationSubjectType = aggregationSubjectType;
        this.field = field;
        this.aggregationFunction = aggregationFunction;
    }

    public void initialize() {
        aggregatorSubjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(aggregationSubjectProvider, aggregationSubjectType);

        // Initialise aggregators
        aggregators = new HashMap<>();
        aggregators.put(AggregationFunction.sum, new Sum());
        aggregators.put(AggregationFunction.mean, new Mean());

        try {
            this.aggregator = aggregators.get(this.aggregationFunction);
            this.singleValueField = (SingleValueField) field.toField();
            singleValueField.setFieldCache(fieldCache);
        } catch (Exception e) {
            throw new Error("Field not valid");
        }
    }

    private Double aggregateSubjects(MathArrays.Function aggregator, List<Subject> aggregationSubjects) throws IncomputableFieldException {
        ResizableDoubleArray doubles = new ResizableDoubleArray();

        for (Subject subject : aggregationSubjects) {
            try {
                doubles.addElement(Double.parseDouble(singleValueField.valueForSubject(subject, true)));
            } catch (IncomputableFieldException e) {
                log.warn("Incomputable field not included in aggregation for subject {} ({})",
                        subject.getName(),
                        subject.getId());
            }
        }

        Double retVal = doubles.compute(aggregator);

        if (retVal.isNaN()) {
            throw new IncomputableFieldException(String.format("Aggregation function %s returned NaN (possible division by zero?)", aggregationFunction));
        } else if (retVal.isInfinite()) {
            throw new IncomputableFieldException(String.format("Aggregation function %s returned Infinity (possible division by zero?)", aggregationFunction));
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
}
