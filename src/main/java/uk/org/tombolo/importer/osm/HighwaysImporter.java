package uk.org.tombolo.importer.osm;

import uk.org.tombolo.core.DatasourceSpec;
import uk.org.tombolo.importer.Config;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class HighwaysImporter extends OSMImporter {

    private static final String AREA = "Great Britain";
    private static final Map<String, List<String>> CATEGORIES = Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("highway", Arrays.asList("motorway", "trunk", "primary", "secondary", "tertiary", "unclassified","residential", "service")))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    public HighwaysImporter(Config config) {
        super(config);

        datasourceSpec = new DatasourceSpec(
                HighwaysImporter.class,
                "OSMHighways",
                "",
                "Open Street Map highways data",
                URL
        );
        categories = CATEGORIES;

        datasourceIds = Arrays.asList(datasourceSpec.getId());
    }
}