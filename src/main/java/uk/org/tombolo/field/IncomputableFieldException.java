package uk.org.tombolo.field;

/**
 * IncomputableFieldException.java
 * Signals that the Field could not be computed for the given subject
 *
 * Note that this should not be used for any errors in Field specification or construction.
 * It should only be used for situations where the value cannot be computed for the given
 * subject. For instance, if no TimedValue exists for the Attribute and Subject.
 */
public class IncomputableFieldException extends Exception {
    public IncomputableFieldException () { }

    public IncomputableFieldException (String message) {
        super (message);
    }

    public IncomputableFieldException (Throwable cause) {
        super (cause);
    }

    public IncomputableFieldException (String message, Throwable cause) {
        super (message, cause);
    }
}
