package uk.org.tombolo.field.aggregation;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.field.*;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.recipe.SubjectRecipe;

import java.util.Collections;
import java.util.List;

/**
 * MapToNearestSubjectField.java
 * This field will find the nearest subject of a given SubjectType and then
 * evaluate the fieldSpec with that new subject. For example, if the
 * nearestSubjectType is 'Street' and it is given a subject representing a
 * building, it will evaluate the fieldSpec with a subject representing the
 * Street that building is on (notwithstanding oddities in the data)
 */
public class MapToNearestSubjectField extends AbstractField implements ParentField, SingleValueField {
    private static final Double DEFAULT_MAX_RADIUS = 0.01;

    private final SubjectRecipe subject;
    private final FieldRecipe field;
    private Double maxRadius;
    private SingleValueField singleValueField;
    private SubjectType nearestSubjectTypeObject;

    MapToNearestSubjectField(String label, SubjectRecipe subject, Double maxRadius, FieldRecipe field) {
        super(label);
        this.maxRadius = maxRadius;
        this.subject = subject;
        this.field = field;
    }

    public void initialize() {
        nearestSubjectTypeObject = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(subject.getProvider(), subject.getSubjectType());

        // Initialize maxRadius with a default value
        if (null == maxRadius) maxRadius = DEFAULT_MAX_RADIUS;
        try {
            this.singleValueField = (SingleValueField) field.toField();
            singleValueField.setFieldCache(fieldCache);
        } catch (ClassNotFoundException e) {
            throw new Error("Field not valid");
        }
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        if (null == singleValueField) { initialize(); }
        JSONObject obj = new JSONObject();
        obj.put(this.label, Double.valueOf(singleValueField.valueForSubject(getSubjectProximalToSubject(subject), timeStamp)));
        return obj;
    }

    private Subject getSubjectProximalToSubject(Subject outputSubject) throws IncomputableFieldException {
        Subject nearestSubject = SubjectUtils.subjectNearestSubject(nearestSubjectTypeObject, outputSubject, maxRadius);
        if (nearestSubject == null) {
            throw new IncomputableFieldException(String.format(
                    "Subject %s has no nearby subjects of type %s, but should have 1",
                    outputSubject.getName(),
                    subject.getSubjectType()));
        }

        return nearestSubject;
    }

    @Override
    public List<Field> getChildFields() {
        if (singleValueField == null)
            initialize();
        return Collections.singletonList(singleValueField);
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        if (null == singleValueField) { initialize(); }
        return singleValueField.valueForSubject(
                getSubjectProximalToSubject(subject), timeStamp);
    }
}
