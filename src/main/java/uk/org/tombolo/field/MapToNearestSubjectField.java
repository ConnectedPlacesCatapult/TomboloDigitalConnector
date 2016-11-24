package uk.org.tombolo.field;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValueId;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.execution.spec.FieldSpecification;

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
public class MapToNearestSubjectField implements Field, SingleValueField, ParentField {
    private static final Double DEFAULT_MAX_RADIUS = 0.01;

    private final String label;
    private final String nearestSubjectType;
    private final FieldSpecification fieldSpecification;
    private Double maxRadius;
    private SingleValueField field;

    MapToNearestSubjectField(String label, String nearestSubjectType, Double maxRadius, FieldSpecification fieldSpecification) {
        this.label = label;
        this.maxRadius = maxRadius;
        this.nearestSubjectType = nearestSubjectType;
        this.fieldSpecification = fieldSpecification;
    }

    public void initialize() {
        // Initialize maxRadius with a default value
        if (null == maxRadius) maxRadius = DEFAULT_MAX_RADIUS;
        try {
            this.field = (SingleValueField) fieldSpecification.toField();
        } catch (ClassNotFoundException e) {
            throw new Error("Field not valid");
        }
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        if (null == field) { initialize(); }
        JSONObject obj = new JSONObject();
        obj.put(this.label,
                Double.valueOf(field.valueForSubject(getSubjectProximalToSubject(subject))));
        return obj;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    private Subject getSubjectProximalToSubject(Subject subject) throws IncomputableFieldException {
        Subject nearestSubject = SubjectUtils.subjectNearestSubject(nearestSubjectType, subject, maxRadius);
        if (nearestSubject == null) {
            throw new IncomputableFieldException(String.format(
                    "Subject %s has no nearby subjects of type %s, but should have 1",
                    subject.getName(),
                    nearestSubjectType));
        }

        return nearestSubject;
    }

    @Override
    public List<Field> getChildFields() {
        if (null == field) { initialize(); }
        return Collections.singletonList(field);
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        return field.valueForSubject(
                getSubjectProximalToSubject(subject));
    }
}
