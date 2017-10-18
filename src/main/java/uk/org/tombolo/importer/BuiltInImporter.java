package uk.org.tombolo.importer;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.recipe.AttributeMatcher;

import java.util.List;
import java.util.Map;

/**
 * Interface representing static built in importers like the OSM importers.
 */
public interface BuiltInImporter {
    Map<Attribute, List<String>> checkBuiltIn(AttributeMatcher attributeMatcher);
}
