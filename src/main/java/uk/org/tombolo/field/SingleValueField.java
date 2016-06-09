package uk.org.tombolo.field;

import uk.org.tombolo.core.Subject;

public interface SingleValueField extends Field {
    String valueForSubject(Subject subject);
}
