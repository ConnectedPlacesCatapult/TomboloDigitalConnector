package uk.org.tombolo.core;

import com.google.gson.stream.JsonWriter;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;

import java.io.StringWriter;

public class ProviderTest extends AbstractTest {

    @Test
    public void testWriteJSON() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        TestFactory.DEFAULT_PROVIDER.writeJSON(jsonWriter);

        JSONAssert.assertEquals("{" +
                "  label: 'default_provider_label'," +
                "  name: 'default_provider_name'" +
                "}", writer.toString(), false);

        jsonWriter.close();
    }
}