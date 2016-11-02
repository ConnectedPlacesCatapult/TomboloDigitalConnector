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
public class GeographicProximityField implements Field, ParentField {
    private final String label;
    private final String proximalSubjectType;
    private final FieldSpecification fieldSpecification;
    private final Double radius;
    private Field field;

    GeographicProximityField(String label, String proximalSubjectType, Double radius, FieldSpecification fieldSpecification) {
        this.label = label;
        this.proximalSubjectType = proximalSubjectType;
        this.radius = radius;
        this.fieldSpecification = fieldSpecification;
    }

    public void initialize() {
        try {
            this.field = fieldSpecification.toField();
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

    private Subject getSubjectProximalToSubject(Subject subject) throws IncomputableFieldException {
        Subject nearestSubject = SubjectUtils.subjectNearestSubject(proximalSubjectType, subject, radius);
        if (nearestSubject == null) {
            throw new IncomputableFieldException(String.format(
                    "Subject %s has no nearby subjects of type %s, but should have 1",
                    subject.getName(),
                    proximalSubjectType));
        }

        return nearestSubject;
    }

    @Override
    public List<Field> getChildFields() {
        if (null == field) { initialize(); }
        return Collections.singletonList(field);
    }
}
