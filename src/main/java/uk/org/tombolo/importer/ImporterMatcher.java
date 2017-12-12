package uk.org.tombolo.importer;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ImportMatcher.java
 * Parses a string indicating a list of classes, then matches on them.
 * The string can be in the following formats
 *   Just match one class:
 *     uk.org.tombolo.something.ImporterClassName
 *   Match either one class, or another:
 *     uk.org.tombolo.something.ImporterClassName, uk.org.tombolo.something.ImporterClassName2
 *   Match nothing:
 *     (an empty string)
 */
public class ImporterMatcher {
    private final List<String> classNames;

    public ImporterMatcher(String matchString) throws ParseException {
        classNames = parseMatchString(matchString);
    }

    private List<String> parseMatchString(String matchString) throws ParseException {
        if (null == matchString || "None".equals(matchString) || "".equals(matchString)) { return Collections.emptyList(); }
        try {
            return Arrays.asList(matchString.split("\\s*,\\s*"));
        } catch (Exception e) {
            throw new ParseException(String.format("Could not parse importer match string: '%s'. See ImporterMatcher.java for examples.", matchString), 0);
        }
    }

    /**
     * doesMatch
     * Returns true if the given cmpClassName matches
     * the classes that we've been told to look for.
     * @param cmpClassName The className to look for
     * @return True if matches, false if does not
     */
    public boolean doesMatch(String cmpClassName) {
        return classNames.stream().anyMatch(className -> className.equals(cmpClassName));
    }
}
