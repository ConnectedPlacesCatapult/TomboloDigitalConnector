package uk.org.tombolo.execution.spec;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import static org.junit.Assert.*;

public class DataExportSpecificationTest {
    String blankSpec = "{" +
                "\"datasetSpecification\": {" +
                    "\"geographySpecification\": []," +
                    "\"datasourceSpecification\": [], " +
                    "\"transformSpecification\": [], " +
                    "\"attributeSpecification\": [] " +
                "}, " +
                "exporterClass: \"uk.org.tombolo.exporter.CSVExporter\"" +
            "}";

    @Test
    public void testFromJsonWithBlankSpec() throws Exception {
        assertSame(DataExportSpecification.class, DataExportSpecification.fromJson(blankSpec).getClass());
    }

    @Test
    public void testFromJsonFileWithBlankSpec() throws Exception {
        File file = File.createTempFile("test", "spec");
        Writer writer = new FileWriter(file);
        writer.write(blankSpec);
        writer.close();
        assertSame(DataExportSpecification.class, DataExportSpecification.fromJsonFile(file).getClass());
    }
}