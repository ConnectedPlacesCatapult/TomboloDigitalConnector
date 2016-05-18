package uk.org.tombolo;

import org.junit.Test;
import uk.org.tombolo.execution.spec.DataExportSpecification;

import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.*;

public class DataExportEngineTest {
    DataExportEngine engine = new DataExportEngine();
    String blankSpec = "{" +
                "\"datasetSpecification\": {" +
                    "\"geographySpecification\": []," +
                    "\"datasourceSpecification\": [], " +
                    "\"transformSpecification\": [], " +
                    "\"attributeSpecification\": [] " +
                "}, " +
                "exporterClass: \"uk.org.tombolo.exporter.GeoJsonExporter\"" +
            "}";

    @Test
    public void testReturnsEmptyOnBlankSpec() throws Exception {
        Writer writer = new StringWriter();
        DataExportSpecification spec = DataExportSpecification.fromJson(blankSpec);

        engine.execute(spec, writer, true);
        assertEquals("{\"type\":\"FeatureCollection\",\"features\":[]}", writer.toString());
    }
}