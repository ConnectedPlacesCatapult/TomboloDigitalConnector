package uk.org.tombolo.field.value;

import com.vividsolutions.jts.geom.Point;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.SingleValueField;

/**
 * SubjectLongitudeField.java
 * Returns the longitude of the centroid of the subject.
 */
public class SubjectLongitudeField implements SingleValueField {
    private final String label;

    public SubjectLongitudeField(String label) {
        this.label = label;
    }

    @Override
    public String valueForSubject(Subject subject) {
        Point centroid = subject.getShape().getCentroid();
        return String.valueOf(centroid.getX());
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) {
        JSONObject obj = new JSONObject();
        Point centroid = subject.getShape().getCentroid();
        obj.put(label, centroid.getX());
        return obj;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
