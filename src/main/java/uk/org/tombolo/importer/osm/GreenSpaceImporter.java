package uk.org.tombolo.importer.osm;

import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.DataSourceID;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Open street map importer for green spaces. Green spaces include:
 * Parks, gardens, god parks, woods, meadows, forests, orchards, grass
 *
 * File: 8f97288ba27a34e5c76ddfa3dfc2383b.osm
 */
public class GreenSpaceImporter extends OSMImporter {

    private static final String AREA = "Great Britain";
    private static final Map<String, List<String>> CATEGORIES = Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("leisure", Arrays.asList("park", "garden", "dog_park")),
            new AbstractMap.SimpleEntry<>("landuse", Arrays.asList("meadow", "wood", "forest", "orchard", "grass")))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    public GreenSpaceImporter(Config config) {
        super(config);

        dataSourceID = new DataSourceID(
                "OSMGreenSpace",
                "",
                "Open Street Map green space data",
                URL,
                compileURL(AREA, CATEGORIES)
        );

        datasourceIds = Arrays.asList(dataSourceID.getLabel());
    }


}
