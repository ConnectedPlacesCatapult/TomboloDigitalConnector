package uk.org.tombolo.importer.osm;

import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.DataSourceID;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Open street map importer for all land uses.
 *
 */
public class LandUseImporter extends OSMImporter {

    private static final Map<String, List<String>> CATEGORIES = Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("landuse", Arrays.asList("*")))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    public LandUseImporter(Config config) {
        super(config);

        dataSourceID = new DataSourceID(
                "OSMLandUse",
                "",
                "Open Street Map land use data",
                URL,
                null
        );
        categories = CATEGORIES;
        datasourceIds = Arrays.asList(dataSourceID.getLabel());
    }

}
