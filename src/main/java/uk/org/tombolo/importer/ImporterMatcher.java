package uk.org.tombolo.importer;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ImportMatcher.java
 * Parses a string indicating a list of class and datasource ID pairs, then matches on them.
 * The string can be in the following formats
 *   Just match one class & ID pair:
 *     uk.org.tombolo.something.ImporterClassName:datasource-id
 *   Match either one class & ID pair, or another:
 *     uk.org.tombolo.something.ImporterClassName:datasource-id,uk.org.tombolo.something.ImporterClassName2:datasource-id2
 *   Match any pair with a given class:
 *     uk.org.tombolo.something.ImporterClassName:
 *   Match any pair with a given ID:
 *     :datasource-id
 *   Match anything:
 *     :
 *   Match nothing:
 *     (an empty string)
 */
public class ImporterMatcher {
    private final List<ImporterMatcherPair> pairs;

    public ImporterMatcher(String matchString) throws ParseException {
        pairs = parseMatchString(matchString);
    }

    private List<ImporterMatcherPair> parseMatchString(String matchString) throws ParseException {
        if (null == matchString || "".equals(matchString)) { return Collections.emptyList(); }
        try {
            return Arrays.asList(matchString.split("\\s*,\\s*")).stream().map(string -> {
                String[] pair = string.split("\\s*:\\s*", -1); // Give us empty strings for stuff like "ClassName:"
                return new ImporterMatcherPair(pair[0], pair[1]);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new ParseException(String.format("Could not parse importer match string: '%s'. See ImporterMatcher.java for examples.", matchString), 0);
        }
    }

    /**
     * doesMatch
     * Returns true if the given cmpClassName and cmpDatasourceId match
     * the classes that we've been told to look for.
     * @param cmpClassName The className to look for
     * @param cmpDatasourceId The datasourceId to look for
     * @return True if matches, false if does not
     */
    public boolean doesMatch(String cmpClassName, String cmpDatasourceId) {
        return pairs.stream().anyMatch(pair -> pair.doesMatch(cmpClassName, cmpDatasourceId));
    }

    private static class ImporterMatcherPair {
        private final String datasourceId;
        private final String className;

        public ImporterMatcherPair(String className, String datasourceId) {
            this.className = className;
            this.datasourceId = datasourceId;
        }

        public boolean doesMatch(String cmpClassName, String cmpDatasourceId) {
            return
                    (className.length() == 0 || className.equals(cmpClassName)) &&
                            (datasourceId.length() == 0 || datasourceId.equals(cmpDatasourceId));
        }
    }
}
