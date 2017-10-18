package uk.org.tombolo.field.assertion;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldBuilder;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.importer.BuiltInImporter;
import uk.org.tombolo.recipe.AttributeMatcher;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.recipe.RecipeDeserializer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;


public class BuiltInAttributeMatcherFieldTest extends AbstractTest {
    private Attribute testAttribute = new Attribute(TestFactory.DEFAULT_PROVIDER,"testAttribute", "");

    private class TestBuiltInImporter implements BuiltInImporter {

        @Override
        public Map<Attribute, List<String>> checkBuiltIn(AttributeMatcher attributeMatcher) {
            Map map = Collections.unmodifiableMap(Stream.of(
                    new AbstractMap.SimpleEntry<>(testAttribute, Arrays.asList("builtInValue")))
                    .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

            return map;
        }
    }

    @Test
    public void valueForSubjectBuiltInAttribute() throws Exception {

        // Create dummy subjects
        Subject subjectWithAttributeMatch = TestFactory.makeNamedSubject("E01000001");
        // Save dummy attribute
        AttributeUtils.save(testAttribute);
        // Assign attribute values
        FixedValueUtils.save(new FixedValue(subjectWithAttributeMatch, testAttribute, "builtInValue"));

        // Create field
        AttributeMatcher attributeMatcher = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(),
                testAttribute.getLabel(), Arrays.asList("built-in-value"));
        BuiltInAttributeMatcherField field = new BuiltInAttributeMatcherField("builtInField", new TestBuiltInImporter(),
                Arrays.asList(attributeMatcher), makeFixedAnnotationFieldSpec());

        // Test
        assertEquals("5.0",field.valueForSubject(subjectWithAttributeMatch, true));
    }

    private FieldRecipe makeFixedAnnotationFieldSpec() {
        return RecipeDeserializer.fromJson(
                FieldBuilder.fixedAnnotationField("default_provider_label", "5.0").toJSONString(),
                FieldRecipe.class);
    }
}
