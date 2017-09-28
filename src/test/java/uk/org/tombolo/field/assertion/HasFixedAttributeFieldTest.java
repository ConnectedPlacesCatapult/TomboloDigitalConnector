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
import uk.org.tombolo.recipe.AttributeMatcher;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.recipe.RecipeDeserializer;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class HasFixedAttributeFieldTest extends AbstractTest {
    @Test
    public void valueForSubject() throws Exception {

        // Create dummy subjects
        Subject subjectWithOneAttributeMatch = TestFactory.makeNamedSubject("E01000001");
        Subject subjectWithTwoAttributeMatches = TestFactory.makeNamedSubject("E09000019");
        Subject subjectWithNoAttributeMatches = TestFactory.makeNamedSubject("E09000001");
        Subject subjectWithoutAttribute = TestFactory.makeNamedSubject("E01000002");

        // Crate dummy attribute
        Attribute testAttribute1 = new Attribute(TestFactory.DEFAULT_PROVIDER,"testAttribute1", "");
        AttributeUtils.save(testAttribute1);
        Attribute testAttribute2 = new Attribute(TestFactory.DEFAULT_PROVIDER,"testAttribute2", "");
        AttributeUtils.save(testAttribute2);
        Attribute testAttribute3 = new Attribute(TestFactory.DEFAULT_PROVIDER,"testAttribute3", "");
        AttributeUtils.save(testAttribute3);


        // Assign attribute values
        FixedValueUtils.save(new FixedValue(subjectWithOneAttributeMatch, testAttribute1, "value"));
        FixedValueUtils.save(new FixedValue(subjectWithTwoAttributeMatches, testAttribute1, "value"));
        FixedValueUtils.save(new FixedValue(subjectWithTwoAttributeMatches, testAttribute2, "value"));
        FixedValueUtils.save(new FixedValue(subjectWithNoAttributeMatches, testAttribute3, "value"));

        // Create field
        AttributeMatcher attributeMatcher1 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), testAttribute1.getLabel());
        AttributeMatcher attributeMatcher2 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), testAttribute2.getLabel());
        HasFixedAttributeField field = new HasFixedAttributeField("blafield", Arrays.asList(attributeMatcher1, attributeMatcher2), makeFieldSpec());

        // Test
        assertEquals("3.0",field.valueForSubject(subjectWithOneAttributeMatch, true));
        assertEquals("3.0",field.valueForSubject(subjectWithTwoAttributeMatches, true));
        assertEquals("0",field.valueForSubject(subjectWithNoAttributeMatches, true));
        assertEquals("0",field.valueForSubject(subjectWithoutAttribute, true));
    }

    private FieldRecipe makeFieldSpec() {
        return RecipeDeserializer.fromJson(
                FieldBuilder.fixedAnnotationField("default_provider_label", "3.0").toJSONString(),
                FieldRecipe.class);
    }
}