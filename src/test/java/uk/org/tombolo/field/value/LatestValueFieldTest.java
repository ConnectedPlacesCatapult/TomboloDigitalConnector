package uk.org.tombolo.field.value;

import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.recipe.AttributeMatcher;

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
        TestFactory.makeTimedValue(subject.getSubjectType(), "E01000001", attribute, "2011-01-01T00:00:00", 100d);
        assertEquals("100.0", field.valueForSubject(subject, true));
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        TestFactory.makeTimedValue(subject.getSubjectType(), "E01000001", attribute, "2011-01-01T00:00:00", 100d);
        String jsonString = field.jsonValueForSubject(subject, true).toJSONString();
        JSONAssert.assertEquals("{" +
                "  aLabel: [" +
                "    {" +
                "      value: '100.0'," +
                "      timestamp: '2011-01-01T00:00:00'" +
                "    }" +
                "  ]" +
                "}", jsonString, false);
    }
}