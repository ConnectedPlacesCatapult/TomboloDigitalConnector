package uk.org.tombolo.field.assertion;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.importer.BuiltInImporter;
import uk.org.tombolo.recipe.AttributeMatcher;
import uk.org.tombolo.recipe.FieldRecipe;

import java.util.List;
import java.util.Map;

/**
 *  Returns the value of the 'field' if the subject has an attribute matching one of the built-in attributes in the
 *  built-in importer class specified
 */
public class BuiltInAttributeMatcherField extends AttributeMatcherField {
    private BuiltInImporter builtInClass;

    public BuiltInAttributeMatcherField(String label, BuiltInImporter builtInClass, List<AttributeMatcher> attributes, FieldRecipe field) {
        super(label, attributes, field);
        this.builtInClass = builtInClass;
    }

    @Override
    protected Map<Attribute, List<String>> getAttributeValueMatches(AttributeMatcher attributeMatcher) {
        return builtInClass.checkBuiltIn(attributeMatcher);
    }
}
