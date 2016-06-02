package uk.org.tombolo;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.core.Attribute;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class DataExportEngineTest extends AbstractTest {
    DataExportEngine engine = new DataExportEngine(makeTestDownloadUtils());
    DataExportSpecificationBuilder builder = DataExportSpecificationBuilder.withGeoJsonExporter();
    Writer writer = new StringWriter();

    @Before
    public void addGeography() {
        TestFactory.makeNamedGeography("E01000001");
        TestFactory.makeNamedGeography("E09000001");
        TestFactory.makeNamedGeography("E01002766");
    }

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
        assertHasOneTimedValue(writer.toString(), "attr_label");
        assertTimedValueEquals(writer.toString(), "2011-01-01T00:00", "100.0", "attr_label");
    }

    @Test
    public void testImportsFromLondonDataStore() throws Exception {
        builder.addGeographySpecification(
                new GeographySpecificationBuilder("localAuthority").addMatcher("label", "E09000001")
        ).addDatasourceSpecification("uk.org.tombolo.importer.londondatastore.LondonDatastoreImporter", "london-borough-profiles")
         .addAttributeSpecification("uk.gov.london", "populationDensity");

        engine.execute(builder.build(), writer, true);

        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.populationDensity.name", equalTo("Population density (per hectare) 2015")));
        assertHasOneTimedValue(writer.toString(), "populationDensity");
        assertTimedValueEquals(writer.toString(), "2015-12-31T23:59:59", "28.237556363195576", "populationDensity");

    }

    @Test
    public void testTransforms() throws Exception {
        builder .addGeographySpecification(
                    new GeographySpecificationBuilder("lsoa").addMatcher("label", "E01002766"))
                .addDatasourceSpecification("uk.org.tombolo.importer.ons.ONSCensusImporter", "QS103EW")
                .addTransformSpecification(
                    new TransformSpecificationBuilder("uk.org.tombolo.transformer.SumFractionTransformer")
                            .setOutputAttribute("provider", "percentage_under_1_years_old")
                            .addInputAttribute("uk.gov.ons", "CL_0000053_2") // number under one year old
                            .addInputAttribute("uk.gov.ons", "CL_0000053_1")) // total population
                .addAttributeSpecification("provider_label", "percentage_under_1_years_old_label");

        engine.execute(builder.build(), writer, true);

        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.percentage_under_1_years_old_label.name", equalTo("percentage_under_1_years_old_name")));
        assertHasOneTimedValue(writer.toString(), "percentage_under_1_years_old_label");
        assertTimedValueEquals(writer.toString(), "2011-12-31T23:59:59", "0.012263099219620958", "percentage_under_1_years_old_label");
    }

    @Test
    public void testRunsOnNewGeographies() throws Exception {
        builder .addGeographySpecification(
                    new GeographySpecificationBuilder("TfLStation").addMatcher("name", "Aldgate Station"))
                .addDatasourceSpecification("uk.org.tombolo.importer.tfl.TfLStationsImporter", "StationList")
                .addAttributeSpecification("uk.gov.tfl", "ServingLineCount");

        engine.execute(builder.build(), writer, true);

        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.name", equalTo("Aldgate Station")));
        assertHasOneTimedValue(writer.toString(), "ServingLineCount");
        assertTimedValueEquals(writer.toString(), "2010-02-04T11:54:08", "3.0", "ServingLineCount");
    }

    @Test
    public void testExportsCSV() throws Exception {
        DataExportSpecificationBuilder csvBuilder = DataExportSpecificationBuilder.withCSVExporter();
        csvBuilder
                .addGeographySpecification(
                        new GeographySpecificationBuilder("lsoa").addMatcher("label", "E01002766"))
                .addDatasourceSpecification("uk.org.tombolo.importer.ons.ONSCensusImporter", "QS103EW")
                .addTransformSpecification(
                        new TransformSpecificationBuilder("uk.org.tombolo.transformer.SumFractionTransformer")
                                .setOutputAttribute("provider", "percentage_under_1_years_old")
                                .addInputAttribute("uk.gov.ons", "CL_0000053_2") // number under one year old
                                .addInputAttribute("uk.gov.ons", "CL_0000053_1")) // total population
                .addAttributeSpecification("provider_label", "percentage_under_1_years_old_label");

        engine.execute(csvBuilder.build(), writer, true);

        List<CSVRecord> records = CSVParser.parse(writer.toString(), CSVFormat.DEFAULT.withHeader()).getRecords();

        assertEquals(1, records.size());
        assertEquals("E01002766", records.get(0).get("label"));
        assertEquals("0.012263099219620958", records.get(0).get("provider_label_percentage_under_1_years_old_label_latest_value"));
    }

    private void assertTimedValueEquals(String json, String time, String matches, String attributeLabel) {
        assertEquals(
                matches,
                JsonPath.parse(writer.toString())
                        .read("$.features[0].properties.attributes." + attributeLabel + ".values['" + time + "']")
                        .toString());
    }

    private void assertHasOneTimedValue(String json, String attributeLabel) {
        assertThat(json,
                hasJsonPath("$.features[0].properties.attributes." + attributeLabel + ".values[*]", hasSize(1)));
    }
}