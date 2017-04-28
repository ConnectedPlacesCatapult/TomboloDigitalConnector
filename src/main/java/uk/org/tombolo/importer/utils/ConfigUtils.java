package uk.org.tombolo.importer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.importer.Config;

import java.util.Arrays;
import java.util.Properties;

/**
 * Utility class to operate with the config file that the user specifies for the importer.
 */

public class ConfigUtils {
    static Logger log = LoggerFactory.getLogger(ConfigUtils.class);

    private enum PROPERTIES {
        FILE_LOCATION("file_location"),
        SUBJECT_ID_INDEX("subjectIDIndex"),
        EXISTING_SUBJECT("existingSubject"),
        PROVIDER("provider"),

        SUBJECT_TYPE_PROVIDER("subject_type_provider"),
        SUBJECT_TYPE_LABEL("subject_type_label"),

        GEO_PROJECTION("geography_projection"),
        GEO_X_INDEX("longitude(easting)Index"),
        GEO_Y_INDEX("latitude(northing)Index");

        String name;

        PROPERTIES(String name) {
            this.name = name;
        }

    }

    private static boolean checkConfig(Properties properties) {
        // check subjectID is not empty
        if (Arrays.asList(PROPERTIES.SUBJECT_ID_INDEX, PROPERTIES.EXISTING_SUBJECT, PROPERTIES.FILE_LOCATION, PROPERTIES.PROVIDER).stream()
                .anyMatch(p -> properties.getProperty(p.name).equals(""))) {
            log.error("Specifying {}, {}, {}, and {} is mandatory.",
                    PROPERTIES.SUBJECT_ID_INDEX,
                    PROPERTIES.EXISTING_SUBJECT,
                    PROPERTIES.FILE_LOCATION,
                    PROPERTIES.PROVIDER);

            return false;
        }

        // check provider, subject_type_provider, and subject_type_label are not empty if the subjectedID did not exist before
        if (properties.getProperty("existingSubject").equalsIgnoreCase("no")) {
            if (Arrays.asList(PROPERTIES.SUBJECT_TYPE_PROVIDER, PROPERTIES.SUBJECT_TYPE_LABEL).stream()
                    .anyMatch(p -> properties.getProperty(p.name).equals(""))) {
                log.error("{} and {} cannot be empty for a new subject.",
                        PROPERTIES.SUBJECT_TYPE_PROVIDER,
                        PROPERTIES.SUBJECT_TYPE_LABEL);

                return false;
            }
        }

        return true;
    }

    public static Config loadConfig(Properties properties) {

        if (!checkConfig(properties)) {
            return null;
        }

        Config.Builder configBuilder = new Config.Builder(
                Integer.parseInt(properties.getProperty(PROPERTIES.SUBJECT_ID_INDEX.name)),
                properties.getProperty(PROPERTIES.EXISTING_SUBJECT.name),
                properties.getProperty(PROPERTIES.FILE_LOCATION.name),
                properties.getProperty(PROPERTIES.PROVIDER.name),
                new SubjectType(
                        new Provider(properties.getProperty(PROPERTIES.SUBJECT_TYPE_PROVIDER.name), ""),
                        properties.getProperty(PROPERTIES.SUBJECT_TYPE_LABEL.name),
                        ""
                )
        );

        if (!properties.getProperty(PROPERTIES.GEO_PROJECTION.name).equals("")) {
            configBuilder.geography(
                    properties.getProperty(PROPERTIES.GEO_PROJECTION.name),
                    Integer.parseInt(properties.getProperty(PROPERTIES.GEO_X_INDEX.name)),
                    Integer.parseInt(properties.getProperty(PROPERTIES.GEO_Y_INDEX.name))
            );
        }

        return configBuilder.build();
    }
}
