package uk.org.tombolo.field.value;

import org.json.simple.JSONObject;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Subject;

import static org.junit.Assert.assertEquals;

public class ConstantFieldTest extends AbstractTest {
    ConstantField field = new ConstantField("aLabel", "aValue");
    Subject subject = new Subject();

    @Test
    public void testValueForSubject() throws Exception {
        assertEquals("aValue", field.valueForSubject(subject, true));
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        JSONObject obj = field.jsonValueForSubject(subject, null);
        assertEquals(obj.size(), 1);
        assertEquals("aValue", obj.get("aLabel"));
    }

    @Test
    public void testGetLabel() throws Exception {
        assertEquals("aLabel", field.getLabel());
    }
}