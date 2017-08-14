package uk.org.tombolo.field.value;

import com.vividsolutions.jts.geom.Point;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.SingleValueField;

/**
 * SubjectLatitudeField.java
 * Returns the latitude of the centroid of the subject.
 */
public class SubjectLatitudeField extends AbstractField implements SingleValueField {

    public SubjectLatitudeField(String label) {
        super(label);
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) {
        Point centroid = subject.getShape().getCentroid();
        return String.valueOf(centroid.getY());
    }

}
