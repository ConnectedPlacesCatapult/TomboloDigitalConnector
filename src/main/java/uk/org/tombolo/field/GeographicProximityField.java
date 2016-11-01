package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;

/**
 *
 */
public class GeographicProximityField implements Field, SingleValueField{

    @Override
    public String valueForSubject(Subject subject) throws IncomputableFieldException {
        return null;
    }

    @Override
    public JSONObject jsonValueForSubject(Subject subject) throws IncomputableFieldException {
        return null;
    }

    @Override
    public String getLabel() {
        return null;
    }
}
