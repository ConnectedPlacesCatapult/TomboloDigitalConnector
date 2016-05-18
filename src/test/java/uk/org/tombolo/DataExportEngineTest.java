package uk.org.tombolo;

import org.junit.Test;

import java.io.StringWriter;
import java.io.Writer;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.*;
import static org.hamcrest.CoreMatchers.*;
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

    @Test
    public void testReturnsGeographyWithMatchingSpec() throws Exception {
        builder.addGeographySpecification(
                new GeographySpecificationBuilder("lsoa").addMatcher("label", "E01000001")
        );

        engine.execute(builder.build(), writer, true);

        assertThat(writer.toString(), hasJsonPath("$.features", hasSize(1)));
        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.label", equalTo("E01000001")));
    }
}