package uk.org.tombolo.importer.osm;

import uk.org.tombolo.core.DatasourceSpec;
import uk.org.tombolo.importer.Config;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Open street map importer for cycling data.
 */
public class CyclingImporter extends OSMImporter {

    private static final String AREA = "Great Britain";
    private static final Map<String, List<String>> CATEGORIES = Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("highway", Arrays.asList("cycleway")),
            new AbstractMap.SimpleEntry<>("cycleway", Arrays.asList(
                    "lane", "opposite", "opposite_lane", "asl", "shoulder", "separate",
                    "shared_lane", "share_busway", "shared",
                    "track", "opposite_track")),
            new AbstractMap.SimpleEntry<>("amenity", Arrays.asList("bicycle_parking", "bicycle_rental")))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    public CyclingImporter(Config config) {
        super(config);

        datasourceSpec = new DatasourceSpec(
                CyclingImporter.class,
                "OSMCycling",
                "",
                "Open Street Map cycling data",
                URL
        );
        categories = CATEGORIES;
        datasourceIds = Arrays.asList(datasourceSpec.getId());
    }


}