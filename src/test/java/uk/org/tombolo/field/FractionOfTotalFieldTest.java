package uk.org.tombolo.field;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.AttributeMatcher;

import java.util.Arrays;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class FractionOfTotalFieldTest extends AbstractTest {
    private Subject subject;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        subject = TestFactory.makeNamedSubject("E01000001");
    }

    @Test
    public void testValueForSubject() throws Exception {
        assertEquals("0.5", makeField().valueForSubject(subject));
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        String jsonString = makeField().jsonValueForSubject(subject).toJSONString();
        assertThat(jsonString, hasJsonPath("$.aLabel.values['2011-01-03T00:00']", equalTo(0.5)));
    }

    @Test
    public void testJsonValueForSubjectWithPartiallyAbsentDividendValue() throws Exception {
        // This is controversial
        // We could decide to return no value since one of the numerator values was missing
        String jsonString = makeFieldWithPartiallyAbsentDividendValue().jsonValueForSubject(subject).toJSONString();
        assertThat(jsonString, hasJsonPath("$.aLabel.values['2011-01-03T00:00']", equalTo(0.25)));
    }

    @Test
    public void testJsonValueForSubjectWithFullyAbsentDivisorValue() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Dividend cannot be zero or absent");
        makeFieldWithFullyAbsentDividendValue().jsonValueForSubject(subject);
    }

    @Test
    public void testJsonValueForSubjectWithAbsentDividendValue() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Divisor cannot be zero or absent");
        makeFieldWithAbsentDivisorValue().jsonValueForSubject(subject);
    }

    @Test
    public void testGetLabel() throws Exception {
        Field field = new FractionOfTotalField("aLabel", null, null);
        assertEquals("aLabel", field.getLabel());
    }

    @Test
    public void testGetHumanReadableName() throws Exception {
        Field field = new FractionOfTotalField("aLabel", null, null);
        assertEquals("aLabel", field.getHumanReadableName());
    }

    private FractionOfTotalField makeField() {
        Attribute attribute1 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr1");
        Attribute attribute2 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr2");
        Attribute attribute3 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr3");
        AttributeMatcher attributeMatcher1 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr1_label");
        AttributeMatcher attributeMatcher2 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr2_label");
        AttributeMatcher attributeMatcher3 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr3_label");
        TestFactory.makeTimedValue("E01000001", attribute1, "2011-01-03T00:00", 100d);
        TestFactory.makeTimedValue("E01000001", attribute2, "2011-01-02T00:00", 100d);
        TestFactory.makeTimedValue("E01000001", attribute3, "2011-01-01T00:00", 400d);
        return new FractionOfTotalField("aLabel", Arrays.asList(attributeMatcher1, attributeMatcher2), attributeMatcher3);
    }

    private FractionOfTotalField makeFieldWithPartiallyAbsentDividendValue() {
        Attribute attribute1 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr1");
        Attribute attribute2 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr2");
        Attribute attribute3 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr3");
        AttributeMatcher attributeMatcher1 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr1_label");
        AttributeMatcher attributeMatcher2 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr2_label");
        AttributeMatcher attributeMatcher3 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr3_label");
        TestFactory.makeTimedValue("E01000001", attribute1, "2011-01-03T00:00", 100d);
        TestFactory.makeTimedValue("E01000001", attribute3, "2011-01-01T00:00", 400d);
        return new FractionOfTotalField("aLabel", Arrays.asList(attributeMatcher1, attributeMatcher2), attributeMatcher3);
    }

    private FractionOfTotalField makeFieldWithFullyAbsentDividendValue() {
        Attribute attribute1 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr1");
        Attribute attribute2 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr2");
        Attribute attribute3 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr3");
        AttributeMatcher attributeMatcher1 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr1_label");
        AttributeMatcher attributeMatcher2 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr2_label");
        AttributeMatcher attributeMatcher3 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr3_label");
        TestFactory.makeTimedValue("E01000001", attribute3, "2011-01-01T00:00", 400d);
        return new FractionOfTotalField("aLabel", Arrays.asList(attributeMatcher1, attributeMatcher2), attributeMatcher3);
    }
    private FractionOfTotalField makeFieldWithAbsentDivisorValue() {
        Attribute attribute1 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr1");
        Attribute attribute2 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr2");
        Attribute attribute3 = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr3");
        AttributeMatcher attributeMatcher1 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr1_label");
        AttributeMatcher attributeMatcher2 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr2_label");
        AttributeMatcher attributeMatcher3 = new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr3_label");
        TestFactory.makeTimedValue("E01000001", attribute1, "2011-01-03T00:00", 100d);
        TestFactory.makeTimedValue("E01000001", attribute2, "2011-01-02T00:00", 100d);
        return new FractionOfTotalField("aLabel", Arrays.asList(attributeMatcher1, attributeMatcher2), attributeMatcher3);
    }
}