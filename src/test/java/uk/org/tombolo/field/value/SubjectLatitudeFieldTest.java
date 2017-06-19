package uk.org.tombolo.field.value;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Subject;

import static org.junit.Assert.assertEquals;

public class SubjectLatitudeFieldTest extends AbstractTest {
    Subject subject;
    SubjectLatitudeField field = new SubjectLatitudeField("aLabel");

    @Before
    public void setUp() throws Exception {
        subject = TestFactory.makeNamedSubject("E01000001");
    }

    @Test
    public void testValueForSubject() throws Exception {
        assertEquals("0.0", field.valueForSubject(subject));
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        JSONObject obj = field.jsonValueForSubject(subject);
        assertEquals(0.0, obj.get("aLabel"));
    }

    @Test
    public void testGetLabel() throws Exception {
        assertEquals("aLabel", field.getLabel());
    }
}