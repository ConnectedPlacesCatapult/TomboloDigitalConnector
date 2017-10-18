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
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class AttributeMatcherFieldTest extends AbstractTest {
    private static final String ATTRIBUTE_LABEL = "tobecounted";

    @Test
    public void valueForSubjectAttributeWithValues() throws Exception {
        // Create dummy subjects
        Subject subjectWithAttributeAndOneValueMatch = TestFactory.makeSubject(
                TestFactory.makeNamedSubjectType("lsoa"),"E01000001","",
                TestFactory.makeSquareGeometry(0.0, 0.0, 5d));
        Subject subjectWithAttribtueAndTwoValueMatches = TestFactory.makeSubject(
                TestFactory.makeNamedSubjectType("localAuthority"),"E09000019","",
                TestFactory.makeSquareGeometry(0.0, 0.0, 7d));
        Subject subjectWithAttributeButOtherValue = TestFactory.makeNamedSubject("E09000001");
        Subject subjectWithoutAttribute = TestFactory.makeNamedSubject("E01000002");

        // Crate dummy attribute
        Attribute testAttribute = new Attribute(TestFactory.DEFAULT_PROVIDER,ATTRIBUTE_LABEL, "");
        AttributeUtils.save(testAttribute);

        // Assign attribute values
        FixedValueUtils.save(new FixedValue(subjectWithAttributeAndOneValueMatch, testAttribute, "value1"));
        FixedValueUtils.save(new FixedValue(subjectWithAttribtueAndTwoValueMatches, testAttribute, "value1"));
        FixedValueUtils.save(new FixedValue(subjectWithAttribtueAndTwoValueMatches, testAttribute, "value2"));
        FixedValueUtils.save(new FixedValue(subjectWithAttributeButOtherValue, testAttribute, "value3"));

        // Create field
        AttributeMatcher attributeMatcher = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), ATTRIBUTE_LABEL,
                Arrays.asList("value1", "value2"));
        AttributeMatcherField field = new AttributeMatcherField("blafield",
                Collections.singletonList(attributeMatcher), makeAreaFieldSpec());

        // Test
        assertEquals("25.00",field.valueForSubject(subjectWithAttributeAndOneValueMatch, true));
        assertEquals("49.00", field.valueForSubject(subjectWithAttribtueAndTwoValueMatches, true));
        assertEquals("0", field.valueForSubject(subjectWithAttributeButOtherValue, true));
        assertEquals("0", field.valueForSubject(subjectWithoutAttribute, true));
    }

    @Test
    public void valueForSubjectAttributeNoValues() throws Exception {

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
        AttributeMatcher attributeMatcher1 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(),
                testAttribute1.getLabel(), Arrays.asList("value", "value"));
        AttributeMatcher attributeMatcher2 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(),
                testAttribute2.getLabel(), Arrays.asList("value"));
        AttributeMatcherField field = new AttributeMatcherField("blafield",
                Arrays.asList(attributeMatcher1, attributeMatcher2), makeFixedAnnotationFieldSpec());

        // Test
        assertEquals("3.0",field.valueForSubject(subjectWithOneAttributeMatch, true));
        assertEquals("3.0",field.valueForSubject(subjectWithTwoAttributeMatches, true));
        assertEquals("0",field.valueForSubject(subjectWithNoAttributeMatches, true));
        assertEquals("0",field.valueForSubject(subjectWithoutAttribute, true));
    }

    private FieldRecipe makeAreaFieldSpec() {
        return RecipeDeserializer.fromJson(
                FieldBuilder.areaField("default_label", Subject.SRID + "").toJSONString(),
                FieldRecipe.class);
    }

    private FieldRecipe makeFixedAnnotationFieldSpec() {
        return RecipeDeserializer.fromJson(
                FieldBuilder.fixedAnnotationField("default_provider_label", "3.0").toJSONString(),
                FieldRecipe.class);
    }
}