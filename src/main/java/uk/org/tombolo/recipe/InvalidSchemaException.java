package uk.org.tombolo.recipe;
/**
 * Thrown if JSON schema is invalid.
 */
public class InvalidSchemaException extends Exception {
    public InvalidSchemaException() { }

    public InvalidSchemaException(String message) {
        super (message);
    }

    public InvalidSchemaException(Throwable cause) {
        super (cause);
    }

    public InvalidSchemaException(String message, Throwable cause) {
        super (message, cause);
    }
}
