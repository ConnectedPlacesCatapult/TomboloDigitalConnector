package uk.org.tombolo.core;

import com.google.gson.stream.JsonWriter;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;

import java.io.StringWriter;

import static org.junit.Assert.*;

public class DatasourceTest extends AbstractTest {
    @Test
    public void testWriteJSON() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        Datasource datasource = new Datasource("id", TestFactory.DEFAULT_PROVIDER, "name", "description");
        datasource.setUrl("http://example.com/info-page");
        datasource.setRemoteDatafile("http://example.com/remote-data-file.json");

        datasource.writeJSON(jsonWriter);

        JSONAssert.assertEquals("{" +
                    "id: 'id'," +
                    "name: 'name'," +
                    "description: 'description'," +
                    "url: 'http://example.com/info-page'," +
                    "remoteDatafile: 'http://example.com/remote-data-file.json'" +
                "}", writer.toString(), false);
    }
}