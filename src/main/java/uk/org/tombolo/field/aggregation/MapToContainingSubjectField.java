package uk.org.tombolo.field.aggregation;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.field.*;
import uk.org.tombolo.recipe.SubjectRecipe;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MapToContainingSubjectField.java
 * This field will find a subject of the given type (subjectType) that contains the provided subject,
 * and then evaluate the fieldSpec with that new subject. For example, if the subjectType is 'City' and
 * it is given a subject representing a building, it will evaluate the fieldSpec with a subject representing the
 * city that building is in.
 */
public class MapToContainingSubjectField extends AbstractField implements ParentField {
    private final SubjectRecipe subject;
    private final FieldRecipe field;
    private SingleValueField singleValueField;
    private SubjectType containerSubjectType;

    MapToContainingSubjectField(String label, SubjectRecipe subject, FieldRecipe fieldRecipe) {
        super(label);
        this.subject = subject;
        this.field = fieldRecipe;
    }

    public void initialize() {
        containerSubjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(subject.getProvider(), subject.getSubjectType());

        try {
            this.singleValueField = (SingleValueField) field.toField();
            singleValueField.setFieldCache(fieldCache);
        } catch (ClassNotFoundException e) {
            throw new Error("Field not valid");
        }
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        if (null == singleValueField) { initialize(); }
        return singleValueField.valueForSubject(getSubjectContainingSubject(subject), timeStamp);
    }

    private Subject getSubjectContainingSubject(Subject subject) throws IncomputableFieldException {
        List<Subject> subjectsContainingSubject = SubjectUtils.subjectsContainingSubject(containerSubjectType, subject);
        if (subjectsContainingSubject.size() != 1) {
            throw new IncomputableFieldException(String.format(
                    "Subject %s is contained by %d subjects of type %s (%s), but should be contained by 1 only",
                    subject.getName(),
                    subjectsContainingSubject.size(),
                    containerSubjectType.getLabel(),
                    subjectsContainingSubject.stream().map(Subject::getName).collect(Collectors.joining(", "))));
        }

        return subjectsContainingSubject.get(0);
    }

    @Override
    public List<Field> getChildFields() {
        if (null == singleValueField) { initialize(); }
        return Collections.singletonList(singleValueField);
    }
}
