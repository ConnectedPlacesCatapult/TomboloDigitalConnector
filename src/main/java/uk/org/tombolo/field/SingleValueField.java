package uk.org.tombolo.field;

import uk.org.tombolo.core.Subject;

/**
 * SingleValueField.java
 * A Field that can return a single String value.
 *
 * Note that you should only implement this if it makes sense for your
 * field to do this. Think about what the user is likely to expect from
 * the name of your field. It may be more readable if you create a subclass
 * of your generic field, or another field entirely.
 */
public interface SingleValueField extends Field {
    String valueForSubject(Subject subject);
}
