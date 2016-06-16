package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Subject;

import static org.junit.Assert.assertEquals;

public class FixedAnnotationFieldTest extends AbstractTest {
    FixedAnnotationField field = new FixedAnnotationField("aLabel", "aValue");
    Subject subject = new Subject();

    @Test
    public void testValueForSubject() throws Exception {
        assertEquals("aValue", field.valueForSubject(subject));
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        JSONObject obj = field.jsonValueForSubject(subject);
        assertEquals(obj.size(), 1);
        assertEquals("aValue", obj.get("aLabel"));
    }

    @Test
    public void testGetLabel() throws Exception {
        assertEquals("aLabel", field.getLabel());
    }

    @Test
    public void testGetHumanReadableName() throws Exception {
        assertEquals("aLabel", field.getLabel());
    }
}