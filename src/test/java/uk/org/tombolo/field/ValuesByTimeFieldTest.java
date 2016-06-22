package uk.org.tombolo.field;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ValuesByTimeFieldTest extends AbstractTest {
    ValuesByTimeField field;
    Subject subject;
    Attribute attribute;

    @Before
    public void setUp() throws Exception {
        subject = TestFactory.makeNamedSubject("E01000001");
        attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr");
        field = new ValuesByTimeField("aLabel", new ValuesByTimeField.AttributeStruct(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr_label"));
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-01T00:00", 100d);
        String jsonString = field.jsonValueForSubject(subject).toJSONString();
        assertThat(jsonString, hasJsonPath("$.aLabel.provider", equalTo("default_provider_name")));
        assertThat(jsonString, hasJsonPath("$.aLabel.name", equalTo("attr_name")));
        assertThat(jsonString, hasJsonPath("$.aLabel.values.length()", equalTo(1)));
        assertThat(jsonString, hasJsonPath("$.aLabel.values['2011-01-01T00:00']", equalTo(100.0)));
    }

    @Test
    public void testJsonValueForSubjectWithMultipleValues() throws Exception {
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-01T00:00", 100d);
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-02T00:00", 200d);
        String jsonString = field.jsonValueForSubject(subject).toJSONString();
        assertThat(jsonString, hasJsonPath("$.aLabel.provider", equalTo("default_provider_name")));
        assertThat(jsonString, hasJsonPath("$.aLabel.name", equalTo("attr_name")));
        assertThat(jsonString, hasJsonPath("$.aLabel.values.length()", equalTo(2)));
        assertThat(jsonString, hasJsonPath("$.aLabel.values['2011-01-01T00:00']", equalTo(100.0)));
        assertThat(jsonString, hasJsonPath("$.aLabel.values['2011-01-02T00:00']", equalTo(200.0)));
    }

    @Test
    public void testGetLabel() throws Exception {
        assertEquals("aLabel", field.getLabel());
    }

    @Test
    public void testGetHumanReadableName() throws Exception {
        assertEquals("attr_name", field.getHumanReadableName());
    }

    @Test
    public void testGetProvider() throws Exception {
        assertEquals(TestFactory.DEFAULT_PROVIDER, field.getProvider());
    }
}