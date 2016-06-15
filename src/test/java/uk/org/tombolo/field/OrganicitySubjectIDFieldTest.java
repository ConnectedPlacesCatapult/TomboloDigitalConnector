package uk.org.tombolo.field;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.organicity.OrganicitySubjectIDField;

import static org.junit.Assert.*;

public class OrganicitySubjectIDFieldTest extends AbstractTest {
    Subject subject;

    @Before
    public void setUp() {
        subject = TestFactory.makeNamedSubject("E01000001");
    }

    @Test
    public void testValueForSubject() throws Exception {
        OrganicitySubjectIDField field = new OrganicitySubjectIDField("aLabel", "aSite", "aService", "aProvider", "aGroup");
        assertEquals("urn:oc:entity:aSite:aService:aProvider:aGroup:E01000001", field.valueForSubject(subject));
    }

    @Test
    public void testValueForSubjectWithMissingFields() throws Exception {
        OrganicitySubjectIDField field = new OrganicitySubjectIDField("aLabel", "aSite", null, "aProvider", "aGroup");
        assertEquals("urn:oc:entity:aSite:aProvider:aGroup:E01000001", field.valueForSubject(subject));
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        OrganicitySubjectIDField field = new OrganicitySubjectIDField("aLabel", "aSite", "aService", "aProvider", "aGroup");
        JSONObject obj = field.jsonValueForSubject(subject);
        assertEquals("urn:oc:entity:aSite:aService:aProvider:aGroup:E01000001", obj.get("aLabel"));
        assertEquals(obj.size(), 1);
    }

    @Test
    public void testGetLabel() throws Exception {
        OrganicitySubjectIDField field = new OrganicitySubjectIDField("aLabel", "aSite", "aService", "aProvider", "aGroup");
        assertEquals("aLabel", field.getLabel());
    }

    @Test
    public void testGetHumanReadableName() throws Exception {
        OrganicitySubjectIDField field = new OrganicitySubjectIDField("aLabel", "aSite", "aService", "aProvider", "aGroup");
        assertEquals("aLabel", field.getHumanReadableName());
    }
}