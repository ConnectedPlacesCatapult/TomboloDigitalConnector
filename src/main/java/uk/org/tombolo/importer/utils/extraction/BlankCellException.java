package uk.org.tombolo.importer.utils.extraction;

public class BlankCellException extends ExtractorException {
    public BlankCellException() { }

    public BlankCellException(String message) {
        super (message);
    }

    public BlankCellException(Throwable cause) {
        super (cause);
    }

    public BlankCellException(String message, Throwable cause) {
        super (message, cause);
    }
}
