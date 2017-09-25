package uk.org.tombolo.recipe;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.DataExportSpecificationBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class RecipeDeserializerTest extends AbstractTest {
    DataExportSpecificationBuilder builder = DataExportSpecificationBuilder.withGeoJsonExporter();

    @Test
    public void testFromJsonWithBlankSpec() throws Exception {
        assertSame(DataExportRecipe.class, RecipeDeserializer.fromJson(builder.toJSONString(), DataExportRecipe.class).getClass());
    }

    @Test
    public void testFromJsonFileWithBlankSpec() throws Exception {
        File file = File.createTempFile("test", "spec");
        Writer writer = new FileWriter(file);
        writer.write(builder.toJSONString());
        writer.close();
        assertSame(DataExportRecipe.class, RecipeDeserializer.fromJsonFile(file, DataExportRecipe.class).getClass());
    }

    @Test
    public void testFromJsonFileWithExporter() throws Exception {
        builder.setExporterClass("a_cool_string");
        assertEquals("a_cool_string", RecipeDeserializer.fromJson(builder.toJSONString(), DataExportRecipe.class).exporter);
    }
}