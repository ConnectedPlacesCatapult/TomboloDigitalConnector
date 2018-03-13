package uk.org.tombolo.importer.osm;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.recipe.AttributeMatcher;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enumeration containing the built-in importers for Open Street Map
 */
public enum OSMBuiltInImporters {
    OSMCycling("built-in-cycling", "Open Street Map cycling data", Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("highway", Arrays.asList("cycleway")),
            new AbstractMap.SimpleEntry<>("cycleway", Arrays.asList(
                    "lane", "opposite", "opposite_lane", "asl", "shoulder", "separate",
                    "shared_lane", "share_busway", "shared",
                    "track", "opposite_track")),
            new AbstractMap.SimpleEntry<>("amenity", Arrays.asList("bicycle_parking", "bicycle_rental")))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())))),
    OSMGreenspace("built-in-greenspace", "Open Street Map green space data", Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("leisure", Arrays.asList(
                    "park", "garden", "dog_park")),
            new AbstractMap.SimpleEntry<>("landuse", Arrays.asList(
                    "allotments", "forest", "grass", "greenfield", "meadow", "orchard", "recreation_ground", "village_green", "wood")),
            new AbstractMap.SimpleEntry<>("natural", Arrays.asList(
                    "fell", "grassland", "heath", "scrub", "wood")))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())))),
    OSMEducation("built-in-education-infrastructure", "Open Street Map education infrastructure data", Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("amenity", Arrays.asList(
                    "school", "library", "university", "college", "kindergarten")))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())))),
    OSMHealth("built-in-health-infrastructure", "Open Street Map health infrastructure data", Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("amenity", Arrays.asList(
                    "clinic", "dentist", "doctors", "hospital", "pharmacy")))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())))),
    OSMCivic("built-in-civic-infrastructure", "Open Street Map education infrastructure data", Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("amenity", Arrays.asList(
                    "courthouse", "coworking_space", "bench", "fire_station", "place_of_worship", "police", "post-box", "post_office", "public_bath", "recycling", "town_hall", "waste_disposal")))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())))),
    OSMHighways("built-in-highways", "Open Street Map highways data", Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("highway",
                    Arrays.asList("motorway", "trunk", "primary", "secondary", "tertiary", "unclassified","residential", "service")))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())))),
    OSMLanduse("built-in-landuse", "Open Street Map land use data", Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("landuse", Arrays.asList("*")))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())))),
    OSMGastronomy("built-in-gastronomy", "Open Street Map gastronomy data", Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>(
                    "amenity",
                    Arrays.asList("bar", "biergarten", "cafe", "fast_food", "pub", "restaurant")))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()))))
    ;

    private String label;
    private String description;
    private Map<String, List<String>> categories;

    OSMBuiltInImporters(String label, String description, Map<String, List<String>> categories) {
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

    /*
        Check if the attribute is an open street map built-in importer that identifies different categories and eventually
        add the attributes and values to the map.
     */
    public static Map<Attribute, List<String>> checkBuiltIn(AttributeMatcher attributeMatcher) {
        Map<Attribute, List<String>> attributeValueMatches = new HashMap<>();

        for(OSMBuiltInImporters bii: OSMBuiltInImporters.values()) {
            if (bii.getLabel().equals(attributeMatcher.label)) {
                for (String category: bii.getCategories().keySet()) {
                    attributeValueMatches.put(
                            AttributeUtils.getByProviderAndLabel(attributeMatcher.provider, category),
                            bii.getCategories().get(category));
                }
                return attributeValueMatches;
            }
        }
        throw new IllegalArgumentException("Built in attribute not supported: " + attributeMatcher.label);
    }
}
