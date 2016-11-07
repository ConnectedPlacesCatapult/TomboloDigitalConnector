package uk.org.tombolo.importer.utils.extraction;

public class UnknownSubjectLabelException extends ExtractorException {

    public UnknownSubjectLabelException() { }

    public UnknownSubjectLabelException(String message) {
        super (message);
    }

    public UnknownSubjectLabelException(Throwable cause) {
        super (cause);
    }

    public UnknownSubjectLabelException(String message, Throwable cause) {
        super (message, cause);
    }

}
