package uk.org.tombolo.core;

import com.google.gson.stream.JsonWriter;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.importer.Importer;

import java.io.StringWriter;

public class DatasourceTest extends AbstractTest {
    @Test
    public void testWriteJSON() throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        Datasource datasource = new Datasource(Importer.class, "id", TestFactory.DEFAULT_PROVIDER, "name", "description");
        datasource.setUrl("http://example.com/info-page");
        datasource.setRemoteDatafile("http://example.com/remote-data-file.json");
        datasource.addFixedValueAttribute(TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "fixed_label"));
        datasource.addTimedValueAttribute(TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "timed_label"));
        datasource.addSubjectType(TestFactory.makeSubjectType(TestFactory.DEFAULT_PROVIDER, "st_label", "st_name"));

        datasource.writeJSON(jsonWriter);

        JSONAssert.assertEquals("{" +
                "  id: 'id'," +
                "  name: 'name'," +
                "  description: 'description'," +
                "  url: 'http://example.com/info-page'," +
                "  remoteDatafile: 'http://example.com/remote-data-file.json'," +
                "  provider: {" +
                "    label: 'default_provider_label'," +
                "    name: 'default_provider_name'" +
                "  }," +
                "  timedValueAttributes: [{" +
                "    label: 'timed_label'," +
                "    name: 'timed_label_name'," +
                "    description: 'timed_label_description'," +
                "    provider: {" +
                "      label: 'default_provider_label'," +
                "      name: 'default_provider_name'" +
                "    }" +
                "  }]," +
                "  fixedValueAttributes: [{" +
                "    label: 'fixed_label'," +
                "    name: 'fixed_label_name'," +
                "    description: 'fixed_label_description'," +
                "    provider: {" +
                "      label: 'default_provider_label'," +
                "      name: 'default_provider_name'" +
                "    }" +
                "  }]," +
                "  subjectTypes: [{" +
                "    label: 'st_label'," +
                "    name: 'st_name'" +
                "  }]" +
                "}", writer.toString(), false);
    }
}