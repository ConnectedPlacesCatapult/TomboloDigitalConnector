package uk.org.tombolo;

import org.junit.Test;
import uk.org.tombolo.execution.spec.DataExportSpecification;

import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.*;

public class DataExportEngineTest {
    DataExportEngine engine = new DataExportEngine();
    DataExportSpecificationBuilder builder = DataExportSpecificationBuilder.fromBlankGeoJson();

    @Test
    public void testReturnsEmptyOnBlankSpec() throws Exception {
        Writer writer = new StringWriter();

        engine.execute(builder.build(), writer, true);

        assertEquals("{\"type\":\"FeatureCollection\",\"features\":[]}", writer.toString());
    }
}