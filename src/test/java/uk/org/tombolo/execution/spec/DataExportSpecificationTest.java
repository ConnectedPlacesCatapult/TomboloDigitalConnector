package uk.org.tombolo.execution.spec;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.DataExportSpecificationBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import static org.junit.Assert.*;

public class DataExportSpecificationTest extends AbstractTest {
    DataExportSpecificationBuilder builder = DataExportSpecificationBuilder.withGeoJsonExporter();

    @Test
    public void testFromJsonWithBlankSpec() throws Exception {
        assertSame(DataExportSpecification.class, DataExportSpecification.fromJson(builder.toJSONString()).getClass());
    }

    @Test
    public void testFromJsonFileWithBlankSpec() throws Exception {
        File file = File.createTempFile("test", "spec");
        Writer writer = new FileWriter(file);
        writer.write(builder.toJSONString());
        writer.close();
        assertSame(DataExportSpecification.class, DataExportSpecification.fromJsonFile(file).getClass());
    }

    @Test
    public void testFromJsonFileWithExporter() throws Exception {
        builder.setExporterClass("a_cool_string");
        assertEquals("a_cool_string", DataExportSpecification.fromJson(builder.toJSONString()).exporterClass);
    }
}