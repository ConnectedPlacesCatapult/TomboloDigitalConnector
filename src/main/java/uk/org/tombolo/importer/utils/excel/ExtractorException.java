package uk.org.tombolo.importer.utils.excel;

/**
 * Thrown if a value cannot be extracted.
 */
public class ExtractorException extends Exception {
    public ExtractorException() { }

    public ExtractorException(String message) {
        super (message);
    }

    public ExtractorException(Throwable cause) {
        super (cause);
    }

    public ExtractorException(String message, Throwable cause) {
        super (message, cause);
    }
}
