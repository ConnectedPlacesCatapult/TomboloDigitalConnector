package uk.org.tombolo.field;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.execution.spec.FieldSpecification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * GeographicAggregationField.java
 * A field that calculates the value of a field for every other Subject within its geometry and performs some
 * aggregation function on the result.
 *
 * So far, `sum` and `mean` are implemented.
 */
public class GeographicAggregationField implements Field, SingleValueField {
    public static enum AggregationFunction {sum, mean}
    private final String label;
    private final String aggregationSubjectType;
    private final FieldSpecification fieldSpecification;
    private final AggregationFunction aggregationFunction;
    private Map<AggregationFunction, Function<List<Double>, Double>> aggregators;
    private SingleValueField field;
    private Function<List<Double>, Double> aggregator;

    GeographicAggregationField(String label, String aggregationSubjectType, AggregationFunction aggregationFunction, FieldSpecification fieldSpecification) {
        this.label = label;
        this.aggregationSubjectType = aggregationSubjectType;
        this.fieldSpecification = fieldSpecification;
        this.aggregationFunction = aggregationFunction;
    }

    public void initialize() {
        // Initialise aggregators
        aggregators = new HashMap<>();
        aggregators.put(AggregationFunction.sum, runWithPrimitiveDoubleArray(new Sum()::evaluate));
        aggregators.put(AggregationFunction.mean, runWithPrimitiveDoubleArray(new Mean()::evaluate));

        try {
            this.aggregator = aggregators.get(this.aggregationFunction);
            this.field = (SingleValueField) fieldSpecification.toField();
        } catch (Exception e) {
            throw new Error("Field not valid");
        }
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        if (null == field) { initialize(); }
        JSONObject obj = new JSONObject();
        obj.put(this.label,
                aggregateSubjects(aggregator,
                    getAggregationSubjects(subject)));
        return obj;
    }

    private Double aggregateSubjects(Function<List<Double>, Double> aggregator, List<Subject> aggregationSubjects) throws IncomputableFieldException {
        ArrayList<Double> values = new ArrayList<>();

        for (Subject subject : aggregationSubjects) {
            try {
                values.add(Double.parseDouble(field.valueForSubject(subject)));
            } catch (IncomputableFieldException e) {
                throw new IncomputableFieldException("Aggregator item failed to compute: " + e.getMessage(), e);
            }
        }

        Double retVal = aggregator.apply(values);

        if (retVal.isNaN()) {
            throw new IncomputableFieldException(String.format("Aggregation function %s returned NaN (possible division by zero?)", aggregationFunction));
        } else if (retVal.isInfinite()) {
            throw new IncomputableFieldException(String.format("Aggregation function %s returned Infinity (possible division by zero?)", aggregationFunction));
        }

        return retVal;
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        if (null == field) { initialize(); }
        return aggregateSubjects(aggregator,
                getAggregationSubjects(subject)).toString();
    }

    private List<Subject> getAggregationSubjects(Subject subject) throws IncomputableFieldException {
        return SubjectUtils.subjectsWithinSubject(aggregationSubjectType, subject);
    }

    private Function<List<Double>, Double> runWithPrimitiveDoubleArray(Function<double[], Double> fn) {
        return fn.compose((xs) -> {
            double[] doubles = new double[xs.size()];
            for (int i=0; i < xs.size(); i++) {
                doubles[i] = xs.get(i);
            }
            return doubles;
        });
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public String getHumanReadableName() {
        return this.label;
    }
}
