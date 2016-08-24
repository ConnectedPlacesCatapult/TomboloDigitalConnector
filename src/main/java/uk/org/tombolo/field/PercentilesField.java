package uk.org.tombolo.field;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.json.simple.JSONObject;
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

    FieldSpecification valueField;
    List<SubjectSpecification> normalizationSubjects;
    Integer percentileCount;
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

            percentiles = new ArrayList<>();
            for (int i=0; i< percentileCount; i++){
                double percentage = Math.floor(100d/percentileCount)*(i+1);
                percentiles.add(percentile.evaluate(percentage));
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
