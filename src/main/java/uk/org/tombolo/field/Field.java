package uk.org.tombolo.field;

import uk.org.tombolo.core.Subject;

public interface Field {
    String valueForSubject(Subject subject);

    String getLabel();
}
