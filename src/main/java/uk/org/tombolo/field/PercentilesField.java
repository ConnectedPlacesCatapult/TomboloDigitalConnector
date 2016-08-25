package uk.org.tombolo.field;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.execution.spec.SubjectSpecification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Field that returns for a subject the percentile in which its value falls
 */
public class PercentilesField implements Field, SingleValueField, ParentField {
    Logger log = LoggerFactory.getLogger(PercentilesField.class);

    // The field over which to calculate the percentiles
    FieldSpecification valueField;
    // The subjects over which the percentiles are calculated
    List<SubjectSpecification> normalizationSubjects;
    // The number of percentiles
    Integer percentileCount;
    // True if the ordering of the percentiles is supposed to be inverse to the field
    Boolean inverse;

    String label;
    String name;
    Field field;
    List<Double> percentiles;

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

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getHumanReadableName() {
        return name;
    }

    private Double calculateValueForSubject(Subject subject) throws IncomputableFieldException {
        if (field == null)
            initialize();
        double fieldValue = Double.valueOf(((SingleValueField)field).valueForSubject(subject));
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

    private void initialize() throws IncomputableFieldException {
        if (field == null) {
            try {
                field = valueField.toField();
            } catch (ClassNotFoundException e) {
                throw new IncomputableFieldException("Field class not found.", e);
            }
            if (!(field instanceof SingleValueField)){
                throw new IncomputableFieldException("Field must be SingleValueFiedl");
            }
        }

        if (inverse == null)
            inverse = false;

        if (percentiles == null){
            List<Subject> subjects = SubjectUtils.getSubjectBySpecifications(normalizationSubjects);
            Percentile percentile = new Percentile(percentileCount);

            double[] values = new double[subjects.size()];

            for (int i = 0; i< subjects.size(); i++){
                values[i] = Double.valueOf(((SingleValueField)field).valueForSubject(subjects.get(i)));
            }
            percentile.setData(values);
            log.info("Normalising percentiles of {} over {} subjects", field.getHumanReadableName(), subjects.size());
            log.info("Min value: {}", StatUtils.min(values));
            log.info("Max value: {}", StatUtils.max(values));
            log.info("Median: {}", StatUtils.mean(values));
            log.info("Mean: {}", percentile.evaluate(50d));
            log.info("Variance: {}", StatUtils.variance(values));

            percentiles = new ArrayList<>();
            for (int i=0; i< percentileCount; i++){
                double percentage = Math.floor(100d/percentileCount)*(i+1);
                percentiles.add(percentile.evaluate(percentage));
                log.info("Percentile {} wiht percentage {} at value {}",i+1, percentage, percentiles.get(i));
            }
        }
    }

    @Override
    public List<Field> getChildFields() {
        if (field == null) {
            try {
                initialize();
            } catch (IncomputableFieldException e) {
                throw new Error("Field not valid",e);
            }
        }

        return Collections.singletonList(field);
    }
}
