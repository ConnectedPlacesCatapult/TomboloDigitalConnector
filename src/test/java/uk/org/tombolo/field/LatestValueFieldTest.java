package uk.org.tombolo.field;

import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.AttributeMatcher;

import static org.junit.Assert.assertEquals;

public class LatestValueFieldTest extends AbstractTest {
    private Subject subject;
    private Attribute attribute;
    private LatestValueField field;

    @Before
    public void setUp() {
        subject = TestFactory.makeNamedSubject("E01000001");
        attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr_label");
        field = new LatestValueField("aLabel", new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr_label"));
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
        JSONAssert.assertEquals("{" +
                "  aLabel: {" +
                "    provider: 'default_provider_name'," +
                "    values: [" +
                "      {" +
                "        value: 100," +
                "        timestamp: '2011-01-01T00:00:00'" +
                "      }" +
                "    ]," +
                "    name: 'attr_label_name'" +
                "  }" +
                "}", jsonString, false);
    }
}