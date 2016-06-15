package uk.org.tombolo.field;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.organicity.ValuesByTimeWithUrnField;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class ValuesByTimeWithUrnFieldTest extends AbstractTest {
    ValuesByTimeWithUrnField field;
    Subject subject;
    Attribute attribute;

    @Before
    public void setUp() throws Exception {
        subject = TestFactory.makeNamedSubject("E01000001");
        attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr");
        field = new ValuesByTimeWithUrnField("aLabel", new ValuesByTimeField.AttributeStruct(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr_label"), "an:urn");
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-01T00:00", 100d);
        String jsonString = field.jsonValueForSubject(subject).toJSONString();
        assertThat(jsonString, hasJsonPath("$.aLabel.provider", equalTo("default_provider_name")));
        assertThat(jsonString, hasJsonPath("$.aLabel.name", equalTo("attr_name")));
        assertThat(jsonString, hasJsonPath("$.aLabel.values.length()", equalTo(1)));
        assertThat(jsonString, hasJsonPath("$.aLabel.values['2011-01-01T00:00']", equalTo(100.0)));
        // The class-specific one:
        assertThat(jsonString, hasJsonPath("$.aLabel.attributes.urn", equalTo("an:urn")));
    }
}