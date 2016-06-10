package uk.org.tombolo.field;

import com.vividsolutions.jts.geom.Point;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;

public class SubjectLatitudeField implements SingleValueField {
    private final String label;

    public SubjectLatitudeField(String label) {
        this.label = label;
    }

    @Override
    public String valueForSubject(Subject subject) {
        Point centroid = subject.getShape().getCentroid();
        return String.valueOf(centroid.getY());
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) {
        JSONObject obj = new JSONObject();
        Point centroid = subject.getShape().getCentroid();
        obj.put(label, centroid.getY());
        return obj;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getHumanReadableName() {
        return label;
    }
}
