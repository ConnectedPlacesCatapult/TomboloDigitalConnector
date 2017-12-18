package uk.org.tombolo.field.transformation;

import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.*;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.descriptive.summary.SumOfLogs;
import org.apache.commons.math3.stat.descriptive.summary.SumOfSquares;
import org.apache.commons.math3.util.ResizableDoubleArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.*;
import uk.org.tombolo.recipe.FieldRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates a descriptive statistic over the values of a list of input fields
 */
public class DescriptiveStatisticsField extends AbstractField implements SingleValueField, ParentField{
    private static Logger log = LoggerFactory.getLogger(DescriptiveStatisticsField.class);

    public enum Statistic {
        geometricmean(new GeometricMean()),
        kurtosis(new Kurtosis()),
        max(new Max()),
        mean(new Mean()),
        median(new Median()),
        min(new Min()),
        product(new Product()),
        stdev(new StandardDeviation()),
        sum(new Sum()),
        sumoflogs(new SumOfLogs()),
        sumofsquares(new SumOfSquares()),
        variance(new Variance())
        ;

        private UnivariateStatistic statistic;

        Statistic(UnivariateStatistic statistic){
            this.statistic = statistic;
        }

        protected UnivariateStatistic getStatistic(){
            return statistic;
        }
    }

    // Parameters in recipe
    private final Statistic statistic;
    private final List<FieldRecipe> fields;

    // Initialised parameters
    private List<Field> singleValueFields;

    public DescriptiveStatisticsField(String label, Statistic statistic, List<FieldRecipe> fields) {
        super(label);
        this.statistic = statistic;
        this.fields = fields;
    }

    protected void initialize(){
        singleValueFields = new ArrayList<>();
        for (FieldRecipe fieldRecipe : fields){
            try {
                Field field = fieldRecipe.toField();
                if (!(field instanceof SingleValueField))
                    throw new IncomputableFieldException("Parameters for DescriptiveStatisticsField must be of type SingleValueField");
                field.setFieldCache(fieldCache);
                singleValueFields.add(field);
            } catch (Exception e) {
                throw new Error(e.getMessage(), e);
            }
        }
    }

    @Override
    public List<Field> getChildFields() {
        if (singleValueFields == null)
            initialize();
        return singleValueFields;
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        return getDoubleValueForSubject(subject).toString();
    }

    private Double getDoubleValueForSubject(Subject subject) throws IncomputableFieldException {
        // Check for cached value
        String cachedValue = getCachedValue(subject);
        if (cachedValue != null)
            return Double.parseDouble(cachedValue);

        // Initialise
        if (null == singleValueFields)
            initialize();

        // Calculate statistic
        ResizableDoubleArray doubles = new ResizableDoubleArray();

        for (Field field : singleValueFields){
            try {
                doubles.addElement(Double.parseDouble(((SingleValueField) field).valueForSubject(subject, null)));
            }catch (IncomputableFieldException e){
                log.warn("Incomputable field not included in statistics for subject {} ({})",
                        subject.getName(),
                        subject.getId());
            }
        }

        Double descriptiveStaistic = doubles.compute(statistic.getStatistic());

        if (descriptiveStaistic.isNaN()) {
            throw new IncomputableFieldException(String.format(
                    "Descriptive statistics function %s returned NaN",
                    statistic.getStatistic().getClass().getSimpleName()));
        } else if (descriptiveStaistic.isInfinite()) {
            throw new IncomputableFieldException(String.format(
                    "Descriptive statistics function {} returned Infinity",
                    statistic.getStatistic().getClass().getSimpleName()));
        }

        return descriptiveStaistic;
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        JSONObject obj = new JSONObject();
        obj.put(this.label, getDoubleValueForSubject(subject));
        return obj;
    }
}
