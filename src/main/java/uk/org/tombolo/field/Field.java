package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;

public interface Field {
    JSONObject jsonValueForSubject(Subject subject);
    String getLabel();
}
