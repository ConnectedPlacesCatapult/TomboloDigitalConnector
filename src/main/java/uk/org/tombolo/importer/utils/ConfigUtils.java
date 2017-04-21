package uk.org.tombolo.importer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.importer.Config;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 * Utility class to operate with the config file that the user specifies for the importer.
 */

public class ConfigUtils {
    static Logger log = LoggerFactory.getLogger(ConfigUtils.class);

    private enum PROPERTIES {
        FILE_LOCATION("file_location"),
        SUBJECT_ID_INDEX("subjectID"),
        EXISTING_SUBJECT("existingSubject"),

        PROVIDER("provider"),
        SUBJECT_TYPE_PROVIDER("subject_type_provider"),
        SUBJECT_TYPE_LABEL("subject_type_label"),

        GEO_PROJECTION("geography_projection"),
        GEO_X_INDEX("geography_x"),
        GEO_Y_INDEX("geography_y");

        String name;

        PROPERTIES(String name) {
            this.name = name;
        }

    }

    private static boolean checkConfig(Properties properties) {
        // check subjectID is not empty
        if (Arrays.asList(PROPERTIES.SUBJECT_ID_INDEX, PROPERTIES.EXISTING_SUBJECT, PROPERTIES.FILE_LOCATION).stream()
                .anyMatch(p -> properties.getProperty(p.name).equals(""))) {
            log.error("Specifying {}, {}, and {} is mandatory.",
                    PROPERTIES.SUBJECT_ID_INDEX,
                    PROPERTIES.EXISTING_SUBJECT,
                    PROPERTIES.FILE_LOCATION);

            return false;
        }

        // check provider, subject_type_provider, and subject_type_label are not empty if the subjectedID did not exist before
        if (properties.getProperty("existingSubject").equalsIgnoreCase("no")) {
            if (Arrays.asList(PROPERTIES.PROVIDER, PROPERTIES.SUBJECT_TYPE_PROVIDER, PROPERTIES.SUBJECT_TYPE_LABEL).stream()
                    .anyMatch(p -> properties.getProperty(p.name).equals(""))) {
                log.error("{}, {}, and {} cannot be empty for a new subject.",
                        PROPERTIES.PROVIDER,
                        PROPERTIES.SUBJECT_TYPE_PROVIDER,
                        PROPERTIES.SUBJECT_TYPE_LABEL);

                return false;
            }
        }

        return true;
    }

    public static Config loadConfig(String configFilePath) {
        Properties properties = new Properties();
        FileInputStream stream;
        try {
            stream = new FileInputStream(configFilePath);
            properties.load(stream);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }

        if (!checkConfig(properties)) {
            return null;
        }

        Config.Builder configBuilder = new Config.Builder(
                Integer.parseInt(properties.getProperty(PROPERTIES.SUBJECT_ID_INDEX.name)),
                properties.getProperty(PROPERTIES.EXISTING_SUBJECT.name),
                properties.getProperty(PROPERTIES.FILE_LOCATION.name));

        if (!properties.getProperty(PROPERTIES.PROVIDER.name).equals("")) {
            configBuilder.newSubject(
                    properties.getProperty(PROPERTIES.PROVIDER.name),
                    properties.getProperty(PROPERTIES.SUBJECT_TYPE_PROVIDER.name),
                    properties.getProperty(PROPERTIES.SUBJECT_TYPE_LABEL.name)
            );
        }

        if (!properties.getProperty(PROPERTIES.GEO_PROJECTION.name).equals("")) {
            configBuilder.geography(
                    properties.getProperty(PROPERTIES.GEO_PROJECTION.name),
                    Integer.parseInt(properties.getProperty(PROPERTIES.GEO_X_INDEX.name)),
                    Integer.parseInt(properties.getProperty(PROPERTIES.GEO_Y_INDEX.name))
            );
        }

        return configBuilder.build();
    }

//    public static List<Integer> parseRange(String ranges) {
//        List<Integer> attributesToConsider = new ArrayList<>();
//        String[] splits = ranges.split(",");
//
//        for (String range : splits) {
//            if(range.matches("^(\\d+)-(\\d+)$")) {
//                String[] rangeLimits = range.split("(\\s*)-(\\s*)");
//                int start = Integer.parseInt(rangeLimits[0]);
//                int end = Integer.parseInt(rangeLimits[1]);
//                if (rangeLimits.length != 2 || start > end) {
//                    log.warn("Invalid attribute range {}, these attribute index configuration will be skipped.", ranges);
//                    continue;
//                }
//                IntStream attributeColumns = IntStream.rangeClosed(start, end);
//                attributesToConsider.addAll(attributeColumns.boxed().collect(Collectors.toList()));
//            } else if(range.matches("^\\d+$")) {
//                int index = Integer.parseInt(range);
//                if (index < 0) {
//                    log.warn("Attriute index cannot be < 0. This attribute index configuration will be skipped.", range);
//                    continue;
//                }
//                attributesToConsider.add(index);
//            }
//        }
//        return attributesToConsider;
//    }
}
