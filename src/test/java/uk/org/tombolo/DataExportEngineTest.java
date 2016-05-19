package uk.org.tombolo;

import com.jayway.jsonpath.JsonPath;
import org.junit.Test;

import java.io.StringWriter;
import java.io.Writer;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class DataExportEngineTest {
    DataExportEngine engine = new DataExportEngine();
    DataExportSpecificationBuilder builder = DataExportSpecificationBuilder.withGeoJsonExporter();
    Writer writer = new StringWriter();

    @Test
    public void testReturnsEmptyOnBlankSpec() throws Exception {
        engine.execute(builder.build(), writer, true);

        assertThat(writer.toString(), hasJsonPath("$.features", hasSize(0)));
    }

    @Test
    public void testReturnsGeography() throws Exception {
        builder.addGeographySpecification(
                new GeographySpecificationBuilder("lsoa").addMatcher("label", "E01000001")
        );

        engine.execute(builder.build(), writer, true);

        assertThat(writer.toString(), hasJsonPath("$.features", hasSize(1)));
        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.label", equalTo("E01000001")));
    }

    @Test
    public void testReturnsGeographyAndAttribute() throws Exception {

        builder.addGeographySpecification(
                new GeographySpecificationBuilder("lsoa").addMatcher("label", "E01000001")
        ).addAttributeSpecification("uk.gov.ons", "CL_0000053_1");

        engine.execute(builder.build(), writer, true);

        // FIXME: This will only work for me. We need a test DB and test setup.
        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.CL_0000053_1.name", equalTo("Age (T102A) - Total: All categories: Age")));
        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.CL_0000053_1.values[*]", hasSize(1)));
        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.CL_0000053_1.values['2011-12-31T23:59:59']", equalTo(1465.0)));
    }
}