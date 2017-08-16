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
            List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) {

        String geographyScopes = "";
        if (geographyScope != null && !geographyScope.isEmpty())
            geographyScopes = geographyScope.stream().sorted().collect(Collectors.joining("\t"));

        String temporalScopes = "";
        if (temporalScope != null && !temporalScope.isEmpty())
            temporalScopes = temporalScope.stream().sorted().collect(Collectors.joining("\t"));

        String localdata = "";
        if (datasourceLocation != null && !datasourceLocation.isEmpty()) {
            localdata = datasourceLocation.stream().sorted().collect(Collectors.joining("\t"));
        }

        String scopeKey = "";
        if (!"".equals(geographyScopes) || !"".equals(temporalScopes) || !"".equals(localdata))
            scopeKey = ":"+DigestUtils.md5Hex(geographyScopes+"|"+temporalScopes + "|" + localdata);

        String importKey = datasourceId+scopeKey;

        return new DatabaseJournalEntry(className, importKey);
    };

}
