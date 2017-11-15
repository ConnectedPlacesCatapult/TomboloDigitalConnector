package uk.org.tombolo.field.value;

import com.google.gson.JsonSyntaxException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.recipe.AttributeMatcher;

import static org.junit.Assert.assertEquals;

public class TimeseriesFieldTest extends AbstractTest {
    TimeseriesField field;
    Subject subject;
    Attribute attribute;

    @Before
    public void setUp() throws Exception {
        subject = TestFactory.makeNamedSubject("E01000001");
        attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr_label");
        field = new TimeseriesField("aLabel", new AttributeMatcher(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr_label", null));
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        TestFactory.makeTimedValue(subject.getSubjectType(), "E01000001", attribute, "2011-01-01T00:00", 100d);
        String jsonString = field.jsonValueForSubject(subject, null).toJSONString();
        JSONAssert.assertEquals("{" +
                "  aLabel: [" +
                "    {" +
                "      value: 100," +
                "      timestamp: '2011-01-01T00:00:00'" +
                "    }" +
                "  ]" +
                "}", jsonString, false);
    }

    @Test
    public void testJsonValueForSubjectWithMultipleValues() throws Exception {
        TestFactory.makeTimedValue(subject.getSubjectType(), "E01000001", attribute, "2011-01-01T00:00", 100d);
        TestFactory.makeTimedValue(subject.getSubjectType(), "E01000001", attribute, "2011-01-02T00:00", 200d);
        String jsonString = field.jsonValueForSubject(subject, true).toJSONString();
        JSONAssert.assertEquals("{" +
                "  aLabel: [" +
                "    {" +
                "      value: 100," +
                "      timestamp: '2011-01-01T00:00:00'" +
                "    }," +
                "    {" +
                "      value: 200," +
                "      timestamp: '2011-01-02T00:00:00'" +
                "    }" +
                "  ]" +
                "}", jsonString, false);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testJsonValueForSubjectWithMultipleValuesWithNull() {
        TestFactory.makeTimedValue(subject.getSubjectType(), "E01000001", attribute, "2011-01-01T00:00", 100d);
        TestFactory.makeTimedValue(subject.getSubjectType(), "E01000001", attribute, "2011-01-02T00:00", 200d);
        try {
            field.jsonValueForSubject(subject, null).toJSONString();
        } catch (Exception e) {
            Assert.assertEquals(JsonSyntaxException.class, e.getClass());
        }
    }

    @Test
    public void testGetLabel() throws Exception {
        assertEquals("aLabel", field.getLabel());
    }
}