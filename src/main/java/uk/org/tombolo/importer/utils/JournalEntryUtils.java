package uk.org.tombolo.importer.utils;

import org.apache.commons.codec.digest.DigestUtils;
import uk.org.tombolo.core.DatabaseJournalEntry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utilities for handling journal entries for importers.
 */
public class JournalEntryUtils {

    public static DatabaseJournalEntry getJournalEntryForDatasourceId(
            String className, String datasourceId,
            List<String> geographyScope, List<String> temporalScope) {

        String geographyScopes = (geographyScope == null || geographyScope.isEmpty())
                ?""
                :geographyScope.stream().sorted().collect(Collectors.joining("\t"));
        String temporalScopes = (temporalScope == null || temporalScope.isEmpty())
                ?""
                :temporalScope.stream().sorted().collect(Collectors.joining("\t"));

        String scopeKey = ((geographyScope == null || geographyScope.isEmpty()) && (temporalScope == null || temporalScope.isEmpty()))
                ?""
                :":"+DigestUtils.md5Hex(geographyScopes+"|"+temporalScopes);
        String importKey = datasourceId+scopeKey;

        return new DatabaseJournalEntry(className, importKey);
    };

}
