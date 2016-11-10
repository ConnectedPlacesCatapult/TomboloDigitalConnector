package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.execution.spec.FieldSpecification;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class GeographicProximityField implements Field, SingleValueField, ParentField {
    private final String label;
    private final String proximalSubjectType;
    private final FieldSpecification fieldSpecification;
    private SingleValueField field;

    GeographicProximityField(String label, String proximalSubjectType, FieldSpecification fieldSpecification) {
        this.label = label;
        this.proximalSubjectType = proximalSubjectType;
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
                        getSubjectProximalToSubject(subject)));
        return obj;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        if (null == field) { initialize(); }
        return field.valueForSubject(
                getSubjectProximalToSubject(subject));
    }

    private Subject getSubjectProximalToSubject(Subject subject) throws IncomputableFieldException {
        List<Subject> subjectsContainingSubject = SubjectUtils.subjectsNearSubject(proximalSubjectType, subject, 0.001f);
        if (subjectsContainingSubject.size() != 1) {
            throw new IncomputableFieldException(String.format(
                    "Subject %s is contained by %d subjects of type %s (%s), but should be near 1 only",
                    subject.getName(),
                    subjectsContainingSubject.size(),
                    proximalSubjectType,
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
