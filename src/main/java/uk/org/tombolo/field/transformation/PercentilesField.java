package uk.org.tombolo.field.transformation;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.recipe.SubjectRecipe;
import uk.org.tombolo.field.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Field that returns for a subject the percentile in which its value falls.
 * Percentiles can be calculated either over the output Subject or any other specified set of Subjects.
 */
public class PercentilesField extends AbstractField implements ParentField {
    private static Logger log = LoggerFactory.getLogger(PercentilesField.class);

    // The field over which to calculate the percentiles
    private final FieldRecipe valueField;
    // The subjects over which the percentiles are calculated
    private final List<SubjectRecipe> normalizationSubjects;
    // The number of percentiles
    private final Integer percentileCount;
    // True if the ordering of the percentiles is supposed to be inverse to the field
    private Boolean inverse;

    private SingleValueField field;
    private List<Double> percentiles;

    public PercentilesField(
            String label,
            String name,
            FieldRecipe valueField,
            List<SubjectRecipe> normalizationSubjects,
            Integer percentileCount, Boolean inverse) {
        super(label);
        this.valueField = valueField;
        this.normalizationSubjects = normalizationSubjects;
        this.percentileCount = percentileCount;
        this.inverse = inverse;
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        return String.valueOf(calculateValueForSubject(subject));
    }

    private Double calculateValueForSubject(Subject subject) throws IncomputableFieldException {
        String cachedValue = getCachedValue(subject);
        if (cachedValue != null)
            return Double.parseDouble(cachedValue);

        if (field == null)
            initialize();
        double fieldValue = Double.valueOf(field.valueForSubject(subject, true));
        for (int i=0; i< percentiles.size()+1; i++){
            if (fieldValue <= percentiles.get(i)){
                Double value;
                if (inverse){
                    value = new Double(percentileCount-i);
                }else{
                    value =  new Double(i+1);
                }
                setCachedValue(subject, value.toString());
                return value;
            }
        }
        // This should never happen
        throw new IncomputableFieldException("Value outside percentiles");
    }

    private void initialize() {
        if (field == null) {
            try {
                field = (SingleValueField) valueField.toField();
                field.setFieldCache(fieldCache);
            } catch (ClassNotFoundException e) {
                throw new Error("Field class not found.", e);
            } catch (ClassCastException e){
                throw new Error("Field must be SingleValueField", e);
            }
        }

        if (inverse == null)
            inverse = false;

        if (percentiles == null){
            List<Subject> subjects = SubjectUtils.getSubjectBySpecifications(normalizationSubjects);
            Percentile percentile = new Percentile(percentileCount);

            double[] values = new double[subjects.size()];

            for (int i = 0; i< subjects.size(); i++){
                try {
                    values[i] = Double.valueOf(field.valueForSubject(subjects.get(i), true));
                } catch (IncomputableFieldException e) {
                    throw new Error(String.format("Error calculating percentiles. Encountered when computing Field %1$s for Subject %2$s.\n" +
                            "Check that Field %1$s exists for Subject %2$s \n" +
                            "If not, you may have to calculate percentiles over a different range of subjects", field.getLabel(), subjects.get(i).getLabel()), e);
                }
            }
            percentile.setData(values);
            log.info("Normalising percentiles of {} over {} subjects", field.getLabel(), subjects.size());
            log.info("Min value: {}", StatUtils.min(values));
            log.info("Max value: {}", StatUtils.max(values));
            log.info("Median: {}", StatUtils.mean(values));
            log.info("Mean: {}", percentile.evaluate(50d));
            log.info("Variance: {}", StatUtils.variance(values));

            percentiles = new ArrayList<>();
            IntStream.range(0, percentileCount).forEach(i -> {
                double percentage = Math.floor(100d / percentileCount) * (i + 1);
                percentiles.add(percentile.evaluate(percentage));
                log.info("Percentile {} with percentage {} at value {}", i + 1, percentage, percentiles.get(i));
            });
        }
    }

    @Override
    public List<Field> getChildFields() {
        if (field == null)
                initialize();

        return Collections.singletonList(field);
    }
}
