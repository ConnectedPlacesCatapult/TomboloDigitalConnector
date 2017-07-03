package uk.org.tombolo.field.transformation;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.field.SingleValueField;

/**
 * Field that represents the area of the subject's geography.
 * It will give the area only for polygons and multi-polygons,
 * the other shapes(e.g. Points) will return 0.0.
 */
public class AreaField implements SingleValueField {

    private final String label;

    public AreaField(String label) {
        this.label = label;
    }

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        return subject.getShape().getArea() + "";
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        JSONObject obj = new JSONObject();
        obj.put(this.label, subject.getShape().getArea());
        return obj;
    }

    @Override
    public String getLabel() {
        return this.getLabel();
    }
}
