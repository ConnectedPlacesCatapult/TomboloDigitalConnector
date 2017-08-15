package uk.org.tombolo.field.value;

import com.vividsolutions.jts.geom.Point;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.AbstractField;
import uk.org.tombolo.field.SingleValueField;

/**
 * SubjectLongitudeField.java
 * Returns the longitude of the centroid of the subject.
 */
public class SubjectLongitudeField extends AbstractField implements SingleValueField {

    public SubjectLongitudeField(String label) {
        super(label);
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) {
        Point centroid = subject.getShape().getCentroid();
        return String.valueOf(centroid.getX());
    }

}
