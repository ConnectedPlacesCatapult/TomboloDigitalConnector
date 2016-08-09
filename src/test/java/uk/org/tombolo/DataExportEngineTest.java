package uk.org.tombolo;

import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.DatabaseJournalEntry;
import uk.org.tombolo.core.utils.DatabaseJournal;
import uk.org.tombolo.importer.ImporterMatcher;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DataExportEngineTest extends AbstractTest {
    DataExportEngine engine;
    DataExportSpecificationBuilder builder = DataExportSpecificationBuilder.withGeoJsonExporter();
    Writer writer = new StringWriter();

    @Before
    public void addSubjectFixtures() throws Exception {
        engine =  new DataExportEngine(makeApiKeyProperties(), makeTestDownloadUtils());

        TestFactory.makeNamedSubject("E01000001");
        TestFactory.makeNamedSubject("E09000001");
        TestFactory.makeNamedSubject("E01002766");
        TestFactory.makeNamedSubject("E08000035");
    }

    @Test
    public void testReturnsEmptyOnBlankSpec() throws Exception {
        engine.execute(builder.build(), writer);

        assertThat(writer.toString(), hasJsonPath("$.features", hasSize(0)));
    }

    @Test
    public void testReturnsSubject() throws Exception {
        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("lsoa").addMatcher("label", "E01000001")
        );

        engine.execute(builder.build(), writer);

        assertThat(writer.toString(), hasJsonPath("$.features", hasSize(1)));
        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.label", equalTo("E01000001")));
    }

    @Test
    public void testObeysCache() throws Exception {
        // If we mark localAuthorities as imported...
        DatabaseJournal.addJournalEntry(new DatabaseJournalEntry("uk.org.tombolo.importer.ons.LocalAuthorityImporter", "localAuthority"));

        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("localAuthority").addMatcher("label", "E10000006")
        ).addDatasourceSpecification("uk.org.tombolo.importer.ons.LocalAuthorityImporter", "localAuthority");
        engine.execute(builder.build(), writer);

        // ...we expect the importer not to have imported them, so we should have zero features
        assertThat(writer.toString(), hasJsonPath("$.features", hasSize(0)));
    }

    @Test
    public void testReimportsWhenForced() throws Exception {
        // If we mark localAuthorities as imported...
        DatabaseJournal.addJournalEntry(new DatabaseJournalEntry("uk.org.tombolo.importer.ons.LocalAuthorityImporter", "localAuthority"));

        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("localAuthority").addMatcher("label", "E10000006")
        ).addDatasourceSpecification("uk.org.tombolo.importer.ons.LocalAuthorityImporter", "localAuthority");

        // And we set the clear-database flag
        engine.execute(builder.build(), writer, new ImporterMatcher("uk.org.tombolo.importer.ons.LocalAuthorityImporter"));

        // ...we expect the importer to ignore our fake journal and import them anyway
        assertThat(writer.toString(), hasJsonPath("$.features", hasSize(1)));
    }

    @Test
    public void testReturnsSubjectAndLatestTimedValueForAttribute() throws Exception {
        Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr");
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-01T00:00:00", 100d);

        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("lsoa").addMatcher("label", "E01000001")
        ).addFieldSpecification(
                FieldSpecificationBuilder.wrapperField("attributes", Arrays.asList(
                        FieldSpecificationBuilder.latestValue("default_provider_label", "attr_label")
                ))
        );

        engine.execute(builder.build(), writer);

        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.attr_label.name", equalTo("attr_name")));
        assertHasOnlyTimedValues(writer.toString(),
                new TimedValueMatcher("E01000001", "attr_label", "value", "100.0"));
    }

    @Test
    public void testReturnsSubjectAndValuesByTimeForAttribute() throws Exception {
        Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr");
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-01T00:00", 100d);

        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("lsoa").addMatcher("label", "E01000001")
        ).addFieldSpecification(
                FieldSpecificationBuilder.wrapperField("attributes", Arrays.asList(
                        FieldSpecificationBuilder.valuesByTime("default_provider_label", "attr_label")
                ))
        );

        engine.execute(builder.build(), writer);
        System.out.println(writer.toString());

        assertHasOnlyTimedValues(writer.toString(),
                new TimedValueMatcher("E01000001", "attr_label", "2011-01-01T00:00", "100.0"));
    }

    @Test
    public void testImportsFromLondonDataStore() throws Exception {
        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("localAuthority").addMatcher("label", "E09000001"))
                .addDatasourceSpecification("uk.org.tombolo.importer.londondatastore.LondonDatastoreImporter", "london-borough-profiles")
                .addFieldSpecification(
                        FieldSpecificationBuilder.wrapperField("attributes", Arrays.asList(
                                FieldSpecificationBuilder.valuesByTime("uk.gov.london", "populationDensity")
                        ))
                );

        engine.execute(builder.build(), writer);

        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.populationDensity.name", equalTo("Population density (per hectare) 2015")));
        assertHasOnlyTimedValues(writer.toString(),
                new TimedValueMatcher("E09000001", "populationDensity", "2015-12-31T23:59:59", "28.237556363195576"));
    }

    @Test
    public void testTransforms() throws Exception {
        builder .addSubjectSpecification(
                        new SubjectSpecificationBuilder("lsoa").addMatcher("label", "E01002766"))
                .addDatasourceSpecification("uk.org.tombolo.importer.ons.ONSCensusImporter", "QS103EW")
                .addFieldSpecification(
                        FieldSpecificationBuilder.wrapperField("attributes", Arrays.asList(
                                FieldSpecificationBuilder.fractionOfTotal("percentage_under_1_years_old_label")
                                        .addDividendAttribute("uk.gov.ons", "CL_0000053_2") // number under one year old
                                        .setDivisorAttribute("uk.gov.ons", "CL_0000053_1") // total population
                        ))
                );

        engine.execute(builder.build(), writer);

        assertHasOnlyTimedValues(writer.toString(),
                new TimedValueMatcher("E01002766", "percentage_under_1_years_old_label", "2011-12-31T23:59:59", "0.012263099219620958"));
    }

    @Test
    public void testRunsOnNewSubjects() throws Exception {
        builder .addSubjectSpecification(
                new SubjectSpecificationBuilder("TfLStation").addMatcher("name", "Aldgate Station"))
                .addDatasourceSpecification("uk.org.tombolo.importer.tfl.TfLStationsImporter", "StationList")
                .addFieldSpecification(
                        FieldSpecificationBuilder.wrapperField("attributes", Arrays.asList(
                                FieldSpecificationBuilder.valuesByTime("uk.gov.tfl", "ServingLineCount")
                        ))
                );

        engine.execute(builder.build(), writer);

        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.name", equalTo("Aldgate Station")));
        assertHasOnlyTimedValues(writer.toString(),
                new TimedValueMatcher("tfl:station:tube:1000003", "ServingLineCount", "2010-02-04T11:54:08", "3.0"));

    }

    @Test
    public void testExportsCSV() throws Exception {
        DataExportSpecificationBuilder csvBuilder = DataExportSpecificationBuilder.withCSVExporter();
        csvBuilder
                .addSubjectSpecification(
                        new SubjectSpecificationBuilder("lsoa").addMatcher("label", "E01002766"))
                .addDatasourceSpecification("uk.org.tombolo.importer.ons.ONSCensusImporter", "QS103EW")
                .addFieldSpecification(
                        FieldSpecificationBuilder.fractionOfTotal("percentage_under_1_years_old_label")
                                .addDividendAttribute("uk.gov.ons", "CL_0000053_2") // number under one year old
                                .setDivisorAttribute("uk.gov.ons", "CL_0000053_1") // total population
                );

        engine.execute(csvBuilder.build(), writer);

        List<CSVRecord> records = CSVParser.parse(writer.toString(), CSVFormat.DEFAULT.withHeader()).getRecords();

        assertEquals(1, records.size());
        assertEquals("E01002766", records.get(0).get("label"));
        assertEquals("0.012263099219620958", records.get(0).get("percentage_under_1_years_old_label_latest_value"));
    }

    @Test
    public void testExportsMultipleSubjectTypes() throws Exception {
        builder .addSubjectSpecification(
                new SubjectSpecificationBuilder("lsoa").addMatcher("label", "E01002766"))
                .addSubjectSpecification(
                        new SubjectSpecificationBuilder("localAuthority").addMatcher("label", "E08000035"))
                .addDatasourceSpecification("uk.org.tombolo.importer.ons.ONSCensusImporter", "QS103EW")
                .addFieldSpecification(
                        FieldSpecificationBuilder.wrapperField("attributes", Arrays.asList(
                                FieldSpecificationBuilder.fractionOfTotal("percentage_under_1_years_old_label")
                                        .addDividendAttribute("uk.gov.ons", "CL_0000053_2") // number under one year old
                                        .setDivisorAttribute("uk.gov.ons", "CL_0000053_1") // total population
                        ))
                );

        engine.execute(builder.build(), writer);

        assertHasOnlyTimedValues(writer.toString(),
                new TimedValueMatcher("E01002766", "percentage_under_1_years_old_label", "2011-12-31T23:59:59", "0.012263099219620958"),
                new TimedValueMatcher("E08000035", "percentage_under_1_years_old_label", "2011-12-31T23:59:59", "0.013229804986127467"));
    }

    private void assertHasOnlyTimedValues(String json, TimedValueMatcher ...matchers) {
        List<Integer> allTimedAttributes = JsonPath.parse(json).read("$.features..properties.attributes..values");
        assertEquals("Number of matchers does not match number of values", matchers.length, allTimedAttributes.size());
        for (TimedValueMatcher matcher : matchers) {
            assertHasTimedValue(json, matcher);
        }
    }

    private void assertHasTimedValue(String json, TimedValueMatcher matcher) {
        ArrayList<Map<String, Object>> features = JsonPath.parse(json).read("$.features[?]",
                Filter.filter(Criteria.where("properties.label").is(matcher.subjectLabel)));
        assertEquals(String.format("Wrong number of features found for label %s", matcher.subjectLabel), 1, features.size());
        assertEquals(matcher.value, JsonPath.parse(features.get(0)).read("$.properties.attributes." + matcher.attributeName + ".values['" + matcher.timestamp + "']").toString());
    }

    private static class TimedValueMatcher {
        String subjectLabel;
        String attributeName;
        String timestamp;
        String value;

        TimedValueMatcher(String subjectLabel, String attributeName, String timestamp, String value) {
            this.subjectLabel = subjectLabel;
            this.attributeName = attributeName;
            this.timestamp = timestamp;
            this.value = value;
        }
    }
}