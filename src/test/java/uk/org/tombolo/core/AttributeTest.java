package uk.org.tombolo.core;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;

import static org.junit.Assert.assertEquals;

public class AttributeTest extends AbstractTest {
    Attribute subject;
    Provider provider;

    @Before
    public void setUp() throws Exception {
        this.provider = new Provider("providerLabel", "providerName");
        this.subject = new Attribute(this.provider, "attributeLabel", "attributeName", "attributeDescription", null);
    }

    @Test
    public void testUniqueLabel() throws Exception {
        assertEquals("providerLabel_attributeLabel", this.subject.uniqueLabel());
    }
}