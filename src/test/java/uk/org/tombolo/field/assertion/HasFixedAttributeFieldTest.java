package uk.org.tombolo.field.assertion;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.recipe.AttributeMatcher;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class HasFixedAttributeFieldTest extends AbstractTest {
    @Test
    public void valueForSubject() throws Exception {

        // Create dummy subjects
        Subject subjectWithOneAttributeMatch = TestFactory.makeNamedSubject("E01000001");
        Subject subjectWithTwoAttribtueMatches = TestFactory.makeNamedSubject("E09000019");
        Subject subjectWithNoAttributeMatches = TestFactory.makeNamedSubject("E09000001");
        Subject subjectWithoutAttribute = TestFactory.makeNamedSubject("E01000002");

        // Crate dummy attribute
        Attribute testAttribute1 = new Attribute(TestFactory.DEFAULT_PROVIDER,"testAttribute1", "", "", Attribute.DataType.string);
        AttributeUtils.save(testAttribute1);
        Attribute testAttribute2 = new Attribute(TestFactory.DEFAULT_PROVIDER,"testAttribute2", "", "", Attribute.DataType.string);
        AttributeUtils.save(testAttribute2);
        Attribute testAttribute3 = new Attribute(TestFactory.DEFAULT_PROVIDER,"testAttribute3", "", "", Attribute.DataType.string);
        AttributeUtils.save(testAttribute3);


        // Assign attribute values
        FixedValueUtils.save(new FixedValue(subjectWithOneAttributeMatch, testAttribute1, "value"));
        FixedValueUtils.save(new FixedValue(subjectWithTwoAttribtueMatches, testAttribute1, "value"));
        FixedValueUtils.save(new FixedValue(subjectWithTwoAttribtueMatches, testAttribute2, "value"));
        FixedValueUtils.save(new FixedValue(subjectWithNoAttributeMatches, testAttribute3, "value"));

        // Create field
        AttributeMatcher attributeMatcher1 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), testAttribute1.getLabel());
        AttributeMatcher attributeMatcher2 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), testAttribute2.getLabel());
        List<String> testValues = Arrays.asList("value1", "value2");
        HasFixedAttributeField field = new HasFixedAttributeField("blafield", Arrays.asList(attributeMatcher1, attributeMatcher2));

        // Test
        assertEquals("1",field.valueForSubject(subjectWithOneAttributeMatch));
        assertEquals("1",field.valueForSubject(subjectWithTwoAttribtueMatches));
        assertEquals("0",field.valueForSubject(subjectWithNoAttributeMatches));
        assertEquals("0",field.valueForSubject(subjectWithoutAttribute));
    }

}