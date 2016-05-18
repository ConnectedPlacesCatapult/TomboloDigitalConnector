package uk.org.tombolo;

import org.junit.Test;

import java.io.StringWriter;
import java.io.Writer;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class DataExportEngineTest {
    DataExportEngine engine = new DataExportEngine();
    DataExportSpecificationBuilder builder = DataExportSpecificationBuilder.fromBlankGeoJson();
    Writer writer = new StringWriter();

    @Test
    public void testReturnsEmptyOnBlankSpec() throws Exception {
        engine.execute(builder.build(), writer, true);
        
        assertThat(writer.toString(), hasJsonPath("$.features", hasSize(0)));
    }
}