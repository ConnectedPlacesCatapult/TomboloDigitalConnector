package uk.org.tombolo;

import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.GeographyUtils;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDateTime;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class DataExportEngineTest extends AbstractTest {
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
        Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr");
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-01T00:00:00", 100d);

        builder.addGeographySpecification(
                new GeographySpecificationBuilder("lsoa").addMatcher("label", "E01000001")
        ).addAttributeSpecification("default_provider_label", "attr_label");

        engine.execute(builder.build(), writer, true);

        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.attr_label.name", equalTo("attr_name")));
        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.attr_label.values[*]", hasSize(1)));
        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.attr_label.values['2011-01-01T00:00']", equalTo(100d)));
    }

    @Test
    public void testImportsFromLondonDataStore() throws Exception {
        builder.addGeographySpecification(
                new GeographySpecificationBuilder("localAuthority").addMatcher("label", "E09000001")
        ).addDatasourceSpecification("uk.org.tombolo.importer.londondatastore.LondonDatastoreImporter", "london-borough-profiles")
         .addAttributeSpecification("uk.gov.london", "populationDensity");

        engine.execute(builder.build(), writer, true);

        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.populationDensity.name", equalTo("Population density (per hectare) 2015")));
        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.populationDensity.values[*]", hasSize(1)));
        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.populationDensity.values['2015-12-31T23:59:59']", equalTo(28.237556363195576)));
    }

    @Test
    public void testTransforms() throws Exception {
        builder .addGeographySpecification(
                    new GeographySpecificationBuilder("localAuthority").addMatcher("label", "E09000001"))
                .addDatasourceSpecification("uk.org.tombolo.importer.londondatastore.LondonDatastoreImporter", "london-borough-profiles")
                .addTransformSpecification(
                    new TransformSpecificationBuilder("uk.org.tombolo.transformer.SumFractionTransformer")
                            .setOutputAttribute("provider", "attribute")
                            .addInputAttribute("uk.gov.london", "populationDensity")
                            .addInputAttribute("uk.gov.london", "carsPerHousehold"))
                .addAttributeSpecification("provider_label", "attribute_label");

        engine.execute(builder.build(), writer, true);

        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.attribute_label.name", equalTo("attribute_name")));
        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.attribute_label.values[*]", hasSize(1)));
        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.attribute_label.values['2015-12-31T23:59:59']", equalTo(73.18066468830533)));
    }
}