package uk.org.tombolo.field.value;

import org.json.simple.JSONObject;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Subject;

import static org.junit.Assert.assertEquals;

public class SubjectNameFieldTest extends AbstractTest {
    SubjectNameField field = new SubjectNameField("aLabel");
    Subject subject = new Subject(null, null, "theName", null);

    @Test
    public void testValueForSubject() throws Exception {
        assertEquals("theName", field.valueForSubject(subject, true));
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        JSONObject obj = field.jsonValueForSubject(subject, true);
        assertEquals(obj.size(), 1);
        assertEquals("theName", obj.get("aLabel"));
    }

    @Test
    public void testGetLabel() throws Exception {
        assertEquals("aLabel", field.getLabel());
    }
}