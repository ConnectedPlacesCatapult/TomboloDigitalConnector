package uk.org.tombolo.importer.osm;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enumeration containing the built-in importers for Open Street Map
 */
public enum BuiltInImporters {
    OSMCycling("built_in_cycling", "Open Street Map cycling data", Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("highway", Arrays.asList("cycleway")),
            new AbstractMap.SimpleEntry<>("cycleway", Arrays.asList(
                    "lane", "opposite", "opposite_lane", "asl", "shoulder", "separate",
                    "shared_lane", "share_busway", "shared",
                    "track", "opposite_track")),
            new AbstractMap.SimpleEntry<>("amenity", Arrays.asList("bicycle_parking", "bicycle_rental")))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())))),
    OSMGreenspace("built_in_greenspace", "Open Street Map green space data", Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("leisure", Arrays.asList(
                    "park", "garden", "dog_park")),
            new AbstractMap.SimpleEntry<>("landuse", Arrays.asList(
                    "allotments", "forest", "grass", "greenfield", "meadow", "orchard", "recreation_ground", "village_green", "wood")),
            new AbstractMap.SimpleEntry<>("natural", Arrays.asList(
                    "fell", "grassland", "heath", "scrub", "wood")))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())))),
    OSMHighways("built_in_highways", "Open Street Map highways data", Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("highway",
                    Arrays.asList("motorway", "trunk", "primary", "secondary", "tertiary", "unclassified","residential", "service")))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())))),
    OSMLanduse("built_in_landuse", "Open Street Map land use data", Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("landuse", Arrays.asList("*")))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()))))
    ;

    private String label;
    private String description;
    private Map<String, List<String>> categories;

    BuiltInImporters(String label, String description, Map<String, List<String>> categories) {
        this.description = description;
        this.categories = categories;
        this.label = label;
    }

    public Map<String, List<String>> getCategories() {
        return this.categories;
    }
    String getDescription() {
        return this.description;
    }
    public String getLabel() { return this.label; }
}
