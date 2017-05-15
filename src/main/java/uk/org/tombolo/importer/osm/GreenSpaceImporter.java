package uk.org.tombolo.importer.osm;

import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.DataSourceID;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Open street map importer for green spaces. Green spaces include:
 * Parks, gardens, god parks, woods, meadows, forests, orchards
 */
public class GreenSpaceImporter extends OSMImporter {

    public GreenSpaceImporter(Config config) {
        super(config);
        area = "Great Britain";

        categories = Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>("leisure", Arrays.asList("park", "garden", "dog_park")),
                new AbstractMap.SimpleEntry<>("landuse", Arrays.asList("meadow", "wood", "forest", "orchard")))
                .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

        dataSourceID = new DataSourceID(
                "OSMGreenSpace",
                "",
                "Open Street Map green space data",
                "http://overpass-api.de/",
                compileURL()
        );

        datasourceIds = Arrays.asList(dataSourceID.getLabel());
    }


}
