package uk.org.tombolo.field.transformation;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.execution.spec.SubjectSpecification;
import uk.org.tombolo.field.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Field that returns for a subject the percentile in which its value falls.
 * Percentiles can be calculated either over the output Subject or any other specified set of Subjects.
 */
public class PercentilesField extends AbstractField implements Field, SingleValueField, ParentField {
    private static Logger log = LoggerFactory.getLogger(PercentilesField.class);

    // The field over which to calculate the percentiles
    private final FieldSpecification valueField;
    // The subjects over which the percentiles are calculated
    private final List<SubjectSpecification> normalizationSubjects;
    // The number of percentiles
    private final Integer percentileCount;
    // True if the ordering of the percentiles is supposed to be inverse to the field
    private Boolean inverse;

    private SingleValueField field;
    private List<Double> percentiles;

    public PercentilesField(
            String label,
            String name,
            FieldSpecification valueField,
            List<SubjectSpecification> normalizationSubjects,
            Integer percentileCount, Boolean inverse) {
        super(label);
        this.valueField = valueField;
        this.normalizationSubjects = normalizationSubjects;
        this.percentileCount = percentileCount;
        this.inverse = inverse;
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        if (null == field) { initialize(); }
        JSONObject obj = new JSONObject();
        obj.put(this.label,
                calculateValueForSubject(subject));
        return obj;
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        return String.valueOf(calculateValueForSubject(subject));
    }

    private Double calculateValueForSubject(Subject subject) throws IncomputableFieldException {
        if (field == null)
            initialize();
        double fieldValue = Double.valueOf(field.valueForSubject(subject));
        for (int i=0; i< percentiles.size()+1; i++){
            if (fieldValue <= percentiles.get(i)){
                if (inverse){
                    return new Double(percentileCount-i);
                }else{
                    return new Double(i+1);
                }
            }
        }
        // This should never happen
        throw new IncomputableFieldException("Value outside percentiles");
    }

    private void initialize() {
        if (field == null) {
            try {
                field = (SingleValueField) valueField.toField();
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
                    values[i] = Double.valueOf(field.valueForSubject(subjects.get(i)));
                } catch (IncomputableFieldException e) {
                    throw new Error(String.format("Error calculating percentiles. Encountered when computing Field %s for Subject %s.", field.getLabel(), subjects.get(i).getLabel()), e);
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
            for (int i=0; i< percentileCount; i++){
                double percentage = Math.floor(100d/percentileCount)*(i+1);
                percentiles.add(percentile.evaluate(percentage));
                log.info("Percentile {} with percentage {} at value {}",i+1, percentage, percentiles.get(i));
            }
        }
    }

    @Override
    public List<Field> getChildFields() {
        if (field == null)
                initialize();

        return Collections.singletonList(field);
    }
}
