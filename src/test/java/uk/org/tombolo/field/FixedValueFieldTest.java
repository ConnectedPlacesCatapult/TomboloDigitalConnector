package uk.org.tombolo.field;

import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.AttributeMatcher;

import static org.junit.Assert.*;

public class FixedValueFieldTest extends AbstractTest {
    private Subject subject;
    private Attribute attribute;
    private FixedValueField field;

    @Before
    public void setUp() {
        subject = TestFactory.makeNamedSubject("E01000001");
        attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr_label");
        field = new FixedValueField("aLabel", new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr_label"));
    }

    @Test
    public void testValueForSubject() throws Exception {
        TestFactory.makeFixedValue("E01000001", attribute, "one-hundred");
        assertEquals("one-hundred", field.valueForSubject(subject));
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        TestFactory.makeFixedValue("E01000001", attribute, "one-hundred");
        String jsonString = field.jsonValueForSubject(subject).toJSONString();
        JSONAssert.assertEquals("{" +
                "  aLabel: [" +
                "    {" +
                "      value: 'one-hundred'" +
                "    }" +
                "  ]" +
                "}", jsonString, false);
    }
}