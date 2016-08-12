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

public class ValuesByTimeFieldTest extends AbstractTest {
    ValuesByTimeField field;
    Subject subject;
    Attribute attribute;

    @Before
    public void setUp() throws Exception {
        subject = TestFactory.makeNamedSubject("E01000001");
        attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr_label");
        field = new ValuesByTimeField("aLabel", new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr_label"));
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-01T00:00", 100d);
        String jsonString = field.jsonValueForSubject(subject).toJSONString();
        JSONAssert.assertEquals("{" +
                "  aLabel: {" +
                "    values: [" +
                "      {" +
                "        value: 100," +
                "        timestamp: '2011-01-01T00:00:00'" +
                "      }" +
                "    ]" +
                "  }" +
                "}", jsonString, false);
    }

    @Test
    public void testJsonValueForSubjectWithMultipleValues() throws Exception {
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-01T00:00", 100d);
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-02T00:00", 200d);
        String jsonString = field.jsonValueForSubject(subject).toJSONString();
        JSONAssert.assertEquals("{" +
                "  aLabel: {" +
                "    values: [" +
                "      {" +
                "        value: 100," +
                "        timestamp: '2011-01-01T00:00:00'" +
                "      }," +
                "      {" +
                "        value: 200," +
                "        timestamp: '2011-01-02T00:00:00'" +
                "      }" +
                "    ]" +
                "  }" +
                "}", jsonString, false);
    }

    @Test
    public void testGetLabel() throws Exception {
        assertEquals("aLabel", field.getLabel());
    }
}