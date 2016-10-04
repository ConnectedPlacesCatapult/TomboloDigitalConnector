package uk.org.tombolo.importer.utils.extraction;

import uk.org.tombolo.importer.utils.extraction.ExtractorException;

public interface SingleValueExtractor {

    String extract() throws ExtractorException;
}
