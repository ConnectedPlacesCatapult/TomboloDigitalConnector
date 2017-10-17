package uk.org.tombolo.core;

import com.google.gson.stream.JsonWriter;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.lac.LAQNImporter;

import java.io.StringWriter;

public class DatasourceTest extends AbstractTest {
    @Test
    public void testWriteJSON() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        Datasource datasource = new Datasource(new DatasourceSpec(LAQNImporter.class, "id", "name", "description", "http://example.com/info-page"));
        datasource.writeJSON(jsonWriter);

        JSONAssert.assertEquals("{" +
                "  id: 'id'," +
                "  name: 'name'," +
                "  description: 'description'," +
                "  url: 'http://example.com/info-page'" +
                "}", writer.toString(), false);
    }
}