package uk.org.tombolo.importer;

/**
 * Thrown if the importer contains data that cannot be parsed - e.g. if regex not provided for timestamp
 */
public class ParsingException extends Exception {
    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}