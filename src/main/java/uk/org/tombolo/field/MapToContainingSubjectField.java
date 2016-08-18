package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.execution.spec.FieldSpecification;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MapToContainingSubjectField.java
 * This field will find a subject of the given type (containingSubjectType) that contains the provided subject,
 * and then evaluate the fieldSpec with that new subject. For example, if the containingSubjectType is 'City' and
 * it is given a subject representing a building, it will evaluate the fieldSpec with a subject representing the
 * city that building is in.
 */
public class MapToContainingSubjectField implements Field, SingleValueField, ParentField {
    private final String label;
    private final String containingSubjectType;
    private final FieldSpecification fieldSpecification;
    private SingleValueField field;

    MapToContainingSubjectField(String label, String containingSubjectType, FieldSpecification fieldSpecification) {
        this.label = label;
        this.containingSubjectType = containingSubjectType;
        this.fieldSpecification = fieldSpecification;
    }

    public void initialize() {
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
                field.jsonValueForSubject(
                        getSubjectContainingSubject(subject)));
        return obj;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public String getHumanReadableName() {
        return this.label;
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        if (null == field) { initialize(); }
        return field.valueForSubject(
                getSubjectContainingSubject(subject));
    }

    private Subject getSubjectContainingSubject(Subject subject) throws IncomputableFieldException {
        List<Subject> subjectsContainingSubject = SubjectUtils.subjectsContainingSubject(containingSubjectType, subject);
        if (subjectsContainingSubject.size() != 1) {
            throw new IncomputableFieldException(String.format(
                    "Subject %s is contained by %d subjects of type %s (%s), but should be contained by 1 only",
                    subject.getName(),
                    subjectsContainingSubject.size(),
                    containingSubjectType,
                    subjectsContainingSubject.stream().map(Subject::getName).collect(Collectors.joining(", "))));
        }

        return subjectsContainingSubject.get(0);
    }

    @Override
    public List<Field> getChildFields() {
        if (null == field) { initialize(); }
        return Collections.singletonList(field);
    }
}
