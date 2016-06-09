package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;

public interface Field {
    void initialize(String label, JSONObject data);

    String valueForSubject(Subject subject);
    String getLabel();

}
