package uk.org.tombolo.field.assertion;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldBuilder;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.recipe.AttributeMatcher;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.recipe.RecipeDeserializer;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;


public class OSMBuiltInAttributeMatcherFieldTest extends AbstractTest {
    @Test
    public void valueForSubjectBuiltInAttribute() throws Exception {
        Provider osmProvider = new Provider("org.openstreetmap", "Open Street Map");
        ProviderUtils.save(osmProvider);
        // Create dummy subjects
        Subject subjectWithAttributeMatch = TestFactory.makeNamedSubject("E01000001");
        // Save dummy attribute
        Attribute testAttribute = new Attribute(osmProvider,"landuse", "");
        AttributeUtils.save(testAttribute);
        // Assign attribute values
        FixedValueUtils.save(new FixedValue(subjectWithAttributeMatch, testAttribute, "park"));

        // Create field
        AttributeMatcher attributeMatcher = new AttributeMatcher(osmProvider.getLabel(),
                "built-in-landuse", null);
        OSMBuiltInAttributeMatcherField field = new OSMBuiltInAttributeMatcherField("builtInField",
                Arrays.asList(attributeMatcher), makeConstantFieldSpec());

        // Test
        assertEquals("5.0",field.valueForSubject(subjectWithAttributeMatch, true));
    }

    private FieldRecipe makeConstantFieldSpec() {
        return RecipeDeserializer.fromJson(
                FieldBuilder.constantField("default_provider_label", "5.0").toJSONString(),
                FieldRecipe.class);
    }
}
