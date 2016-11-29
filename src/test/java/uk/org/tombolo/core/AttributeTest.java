package uk.org.tombolo.core;

import com.google.gson.stream.JsonWriter;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;

import java.io.StringWriter;

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

    @Test
    public void testWriteJSON() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "timed_label").writeJSON(jsonWriter);

        JSONAssert.assertEquals("{" +
                "  label: 'timed_label'," +
                "  name: 'timed_label_name'," +
                "  description: 'timed_label_description'," +
                "  provider: {" +
                "    label: 'default_provider_label'," +
                "    name: 'default_provider_name'" +
                "  }" +
                "}", writer.toString(), false);

        jsonWriter.close();
    }
}