package uk.org.tombolo.field;

import com.vividsolutions.jts.geom.Point;
import uk.org.tombolo.core.Subject;

public class SubjectLongitudeField extends SubjectLatitudeField {
    public SubjectLongitudeField(String label) {
        super(label);
    }

    @Override
    public String valueForSubject(Subject subject) {
        Point centroid = subject.getShape().getCentroid();
        return String.valueOf(centroid.getX());
    }
}
