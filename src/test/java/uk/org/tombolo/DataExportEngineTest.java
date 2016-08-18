package uk.org.tombolo;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.DatabaseJournalEntry;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.DatabaseJournal;
import uk.org.tombolo.importer.ImporterMatcher;
import uk.org.tombolo.core.utils.SubjectUtils;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

        JSONAssert.assertEquals(writer.toString(), "{type:'FeatureCollection', features:[]}", false);
    }

    @Test
    public void testReturnsSubject() throws Exception {
        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("lsoa").setMatcher("label", "E01000001")
        );

        engine.execute(builder.build(), writer);
        JSONAssert.assertEquals("{features: [{properties: {name: 'City of London 001A', label: 'E01000001'}}]}", writer.toString(), false);
    }

    @Test
    public void testObeysCache() throws Exception {
        // If we mark localAuthorities as imported...
        DatabaseJournal.addJournalEntry(new DatabaseJournalEntry("uk.org.tombolo.importer.ons.OaImporter", "localAuthority"));

        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("localAuthority").setMatcher("label", "E10000006")
        ).addDatasourceSpecification("uk.org.tombolo.importer.ons.OaImporter", "localAuthority");
        engine.execute(builder.build(), writer);

        // ...we expect the importer not to have imported them, so we should have no features
        JSONAssert.assertEquals("{type:'FeatureCollection', features:[]}", writer.toString(), false);
    }

    @Test
    public void testReimportsWhenForced() throws Exception {
        // If we mark localAuthorities as imported...
        DatabaseJournal.addJournalEntry(new DatabaseJournalEntry("uk.org.tombolo.importer.ons.OaImporter", "localAuthority"));

        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("localAuthority").setMatcher("label", "E06000001")
        ).addDatasourceSpecification("uk.org.tombolo.importer.ons.OaImporter", "localAuthority");

        // And we set the clear-database flag
        engine.execute(builder.build(), writer, new ImporterMatcher("uk.org.tombolo.importer.ons.OaImporter"));

        // ...we expect the importer to ignore our fake journal and import them anyway
        JSONAssert.assertEquals("{" +
                "  features: [{" +
                "    properties: {" +
                "      name: 'Hartlepool'," +
                "      label: 'E06000001'" +
                "    }" +
                "  }]" +
                "}", writer.toString(), false);
    }

    @Test
    public void testReturnsSubjectAndLatestTimedValueForAttribute() throws Exception {
        Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr_label");
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-01T00:00:00", 100d);

        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("lsoa").setMatcher("label", "E01000001")
        ).addFieldSpecification(
                FieldSpecificationBuilder.wrapperField("attributes", Arrays.asList(
                        FieldSpecificationBuilder.latestValue("default_provider_label", "attr_label")
                ))
        );

        engine.execute(builder.build(), writer);
        JSONAssert.assertEquals("{" +
                "  features: [" +
                "    {" +
                "      properties: {" +
                "        name: 'City of London 001A'," +
                "        attributes: {" +
                "          attr_label: {" +
                "            provider: 'default_provider_name'," +
                "            values: [" +
                "              {" +
                "                value: 100," +
                "                timestamp: '2011-01-01T00:00:00'" +
                "              }" +
                "            ]," +
                "            name: 'attr_label_name'" +
                "          }" +
                "        }," +
                "        label: 'E01000001'" +
                "      }" +
                "    }" +
                "  ]" +
                "}", writer.toString(), false);
    }

    @Test
    public void testReturnsSubjectAndValuesByTimeForAttribute() throws Exception {
        Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr_label");
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-01T00:00", 100d);

        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("lsoa").setMatcher("label", "E01000001")
        ).addFieldSpecification(
                FieldSpecificationBuilder.wrapperField("attributes", Arrays.asList(
                        FieldSpecificationBuilder.valuesByTime("default_provider_label", "attr_label")
                ))
        );

        engine.execute(builder.build(), writer);

        JSONAssert.assertEquals("{" +
                "  features: [" +
                "    {" +
                "      properties: {" +
                "        name: 'City of London 001A'," +
                "        attributes: {" +
                "          attr_label: {" +
                "            provider: 'default_provider_name'," +
                "            values: [" +
                "              {" +
                "                value: 100," +
                "                timestamp: '2011-01-01T00:00:00'" +
                "              }" +
                "            ]," +
                "            name: 'attr_label_name'" +
                "          }" +
                "        }," +
                "        label: 'E01000001'" +
                "      }" +
                "    }" +
                "  ]" +
                "}", writer.toString(), false);
    }

    @Test
    public void testImportsFromLondonDataStore() throws Exception {
        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("localAuthority").setMatcher("label", "E09000001"))
                .addDatasourceSpecification("uk.org.tombolo.importer.londondatastore.LondonDatastoreImporter", "london-borough-profiles")
                .addFieldSpecification(
                        FieldSpecificationBuilder.wrapperField("attributes", Arrays.asList(
                                FieldSpecificationBuilder.valuesByTime("uk.gov.london", "populationDensity")
                        ))
                );

        engine.execute(builder.build(), writer);
        
        JSONAssert.assertEquals("{" +
                "  features: [" +
                "    {" +
                "      properties: {" +
                "        name: 'City of London'," +
                "        attributes: {" +
                "          populationDensity: {" +
                "            provider: 'London Datastore - Greater London Authority'," +
                "            values: [" +
                "              {" +
                "                value: 28.237556363195576," +
                "                timestamp: '2015-12-31T23:59:59'" +
                "              }" +
                "            ]," +
                "            name: 'Population density (per hectare) 2015'" +
                "          }" +
                "        }," +
                "        label: 'E09000001'" +
                "      }" +
                "    }" +
                "  ]" +
                "}", writer.toString(), false);
    }

    @Test
    public void testTransforms() throws Exception {
        builder .addSubjectSpecification(
                        new SubjectSpecificationBuilder("lsoa").setMatcher("label", "E01002766"))
                .addDatasourceSpecification("uk.org.tombolo.importer.ons.ONSCensusImporter", "QS103EW")
                .addFieldSpecification(
                        FieldSpecificationBuilder.wrapperField("attributes", Arrays.asList(
                                FieldSpecificationBuilder.fractionOfTotal("percentage_under_1_years_old_label")
                                        .addDividendAttribute("uk.gov.ons", "CL_0000053_2") // number under one year old
                                        .setDivisorAttribute("uk.gov.ons", "CL_0000053_1") // total population
                        ))
                );

        engine.execute(builder.build(), writer);

        JSONAssert.assertEquals("{" +
                "  features: [" +
                "    {" +
                "      properties: {" +
                "        name: 'Islington 015E'," +
                "        attributes: {" +
                "          percentage_under_1_years_old_label: {" +
                "            values: [" +
                "              {" +
                "                value: 0.012263099219620958," +
                "                timestamp: '2011-12-31T23:59:59'" +
                "              }" +
                "            ]" +
                "          }" +
                "        }," +
                "        label: 'E01002766'" +
                "      }" +
                "    }" +
                "  ]" +
                "}", writer.toString(), false);
    }

    @Test
    public void testRunsOnNewSubjects() throws Exception {
        builder
            .addSubjectSpecification(
                new SubjectSpecificationBuilder("localAuthority").setMatcher("label", "E06000001"))
            .addDatasourceSpecification("uk.org.tombolo.importer.ons.OaImporter", "localAuthority")
            .addDatasourceSpecification("uk.org.tombolo.importer.londondatastore.LondonDatastoreImporter", "london-borough-profiles")
            .addFieldSpecification(
                    FieldSpecificationBuilder.wrapperField("attributes", Arrays.asList(
                            FieldSpecificationBuilder.valuesByTime("uk.gov.london", "populationDensity")
                    ))
            );

        engine.execute(builder.build(), writer);

        JSONAssert.assertEquals("{" +
                "  type: 'FeatureCollection'," +
                "  features: [{" +
                "    type: 'Feature'," +
                "    properties: {" +
                "      name: 'Hartlepool'," +
                "      attributes: {" +
                "        populationDensity: {" +
                "          provider: 'London Datastore - Greater London Authority'," +
                "          values: []," +
                "          name: 'Population density (per hectare) 2015'" +
                "        }" +
                "      }," +
                "      label: 'E06000001'" +
                "    }" +
                "  }]" +
                "}", writer.toString(), false);
    }

    @Test
    public void testMapsBetweenSubjectTypes() throws Exception {
        Subject cityOfLondon = SubjectUtils.getSubjectByLabel("E09000001");
        Subject cityOfLondonLsoa = TestFactory.makeNamedSubject("E01000001"); // Subject contained by 'City of London'
        cityOfLondon.setShape(TestFactory.makePointGeometry(1d, 1d));
        cityOfLondonLsoa.setShape(TestFactory.makePointGeometry(1d, 1d));
        SubjectUtils.save(Arrays.asList(cityOfLondon, cityOfLondonLsoa));

        Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr_label");
        TestFactory.makeTimedValue("E09000001", attribute, "2011-01-01T00:00:00", 100d);

        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("lsoa").setMatcher("label", "E01000001")
        ).addFieldSpecification(
                FieldSpecificationBuilder.mapToContainingSubjectField(
                        "local_authority",
                        "localAuthority",
                        FieldSpecificationBuilder.latestValue("default_provider_label", "attr_label")
                )
        );

        engine.execute(builder.build(), writer);
        String jsonString = writer.toString();
        JSONAssert.assertEquals("{" +
                        "  features: [" +
                        "    {" +
                        "      properties: {" +
                        "        local_authority: {" +
                        "          attr_label: {" +
                        "            values: [" +
                        "              {" +
                        "                value: 100d" +
                        "              }" +
                        "            ]" +
                        "          }" +
                        "        }" +
                        "      }" +
                        "    }" +
                        "  ]"+
                        "}", jsonString, false);
    }

    @Test
    public void testExportsCSV() throws Exception {
        DataExportSpecificationBuilder csvBuilder = DataExportSpecificationBuilder.withCSVExporter();
        csvBuilder
                .addSubjectSpecification(
                        new SubjectSpecificationBuilder("lsoa").setMatcher("label", "E01002766"))
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
                new SubjectSpecificationBuilder("lsoa").setMatcher("label", "E01002766"))
                .addSubjectSpecification(
                        new SubjectSpecificationBuilder("localAuthority").setMatcher("label", "E08000035"))
                .addDatasourceSpecification("uk.org.tombolo.importer.ons.ONSCensusImporter", "QS103EW")
                .addFieldSpecification(
                        FieldSpecificationBuilder.wrapperField("attributes", Arrays.asList(
                                FieldSpecificationBuilder.fractionOfTotal("percentage_under_1_years_old_label")
                                        .addDividendAttribute("uk.gov.ons", "CL_0000053_2") // number under one year old
                                        .setDivisorAttribute("uk.gov.ons", "CL_0000053_1") // total population
                        ))
                );

        engine.execute(builder.build(), writer);

        JSONAssert.assertEquals("{" +
                "  features: [" +
                "    {" +
                "      properties: {" +
                "        name: 'Islington 015E'," +
                "        attributes: {" +
                "          percentage_under_1_years_old_label: {" +
                "            values: [" +
                "              {" +
                "                value: 0.012263099219620958," +
                "                timestamp: '2011-12-31T23:59:59'" +
                "              }" +
                "            ]" +
                "          }" +
                "        }," +
                "        label: 'E01002766'" +
                "      }" +
                "    }," +
                "    {" +
                "      properties: {" +
                "        name: 'Leeds'," +
                "        attributes: {" +
                "          percentage_under_1_years_old_label: {" +
                "            values: [" +
                "              {" +
                "                value: 0.013229804986127467," +
                "                timestamp: '2011-12-31T23:59:59'" +
                "              }" +
                "            ]" +
                "          }" +
                "        }," +
                "        label: 'E08000035'" +
                "      }" +
                "    }" +
                "  ]" +
                "}", writer.toString(), false);
    }

    @Test
    public void testGeneratesPredefinedField() throws Exception {
        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("lsoa").setMatcher("label", "E01002766")
        ).addFieldSpecification(
                FieldSpecificationBuilder.predefinedField("aLabel", "PredefinedFieldTest")
        );

        engine.execute(builder.build(), writer);

        JSONAssert.assertEquals("{" +
                "  features: [{" +
                "    properties: {" +
                "      name: 'Islington 015E'," +
                "      label: 'E01002766'," +
                "      Fraction_of_80: {" +
                "        values: [{" +
                "          value: 0.005016722408026756," +
                "          timestamp: '2011-12-31T23:59:59'" +
                "        }]" +
                "      }" +
                "    }" +
                "  }]" +
                "}", writer.toString(), false);
    }

    @Test
    public void testGeneratesPredefinedFieldWhenNested() throws Exception {
        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("lsoa").setMatcher("label", "E01002766")
        ).addFieldSpecification(
                FieldSpecificationBuilder.wrapperField("aWrapper", Collections.singletonList(
                    FieldSpecificationBuilder.predefinedField("aLabel", "PredefinedFieldTest")))
        );

        engine.execute(builder.build(), writer);

        JSONAssert.assertEquals("{" +
                "  features: [{" +
                "    properties: {" +
                "      name: 'Islington 015E'," +
                "      label: 'E01002766'," +
                "      aWrapper: {" +
                "        Fraction_of_80: {" +
                "          values: [{" +
                "            value: 0.005016722408026756," +
                "            timestamp: '2011-12-31T23:59:59'" +
                "          }]" +
                "        }" +
                "      }" +
                "    }" +
                "  }]" +
                "}", writer.toString(), false);
    }
}