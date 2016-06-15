package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValue;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class LatestValueFieldTest extends AbstractTest {
    private Subject subject;
    private Attribute attribute;
    private LatestValueField field;

    @Before
    public void setUp() {
        subject = TestFactory.makeNamedSubject("E01000001");
        attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr");
        field = new LatestValueField("aLabel", new ValuesByTimeField.AttributeStruct(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr_label"));
    }

    @Test
    public void testValueForSubject() throws Exception {
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-01T00:00:00", 100d);
        assertEquals("100.0", field.valueForSubject(subject));
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-01T00:00:00", 100d);
        String jsonString = field.jsonValueForSubject(subject).toJSONString();
        assertThat(jsonString, hasJsonPath("$.aLabel.provider", equalTo("default_provider_name")));
        assertThat(jsonString, hasJsonPath("$.aLabel.name", equalTo("attr_name")));
        assertThat(jsonString, hasJsonPath("$.aLabel.values.latest", equalTo(100.0)));
    }
}