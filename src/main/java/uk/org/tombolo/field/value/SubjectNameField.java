package uk.org.tombolo.field.value;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.IncomputableFieldException;

/**
 * SubjectNameField.java
 * Returns the name of the given subject
 */
public class SubjectNameField extends AbstractField {

    public SubjectNameField(String label) {
        super(label);
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        return subject.getName();
    }

}
