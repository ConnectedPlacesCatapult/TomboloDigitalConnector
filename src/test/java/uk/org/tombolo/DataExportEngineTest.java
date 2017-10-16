package uk.org.tombolo;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.DatabaseJournalEntry;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.DatabaseJournal;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.execution.DataExportEngine;
import uk.org.tombolo.importer.ImporterMatcher;
import uk.org.tombolo.importer.ons.AbstractONSImporter;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DataExportEngineTest extends AbstractTest {
    private SubjectType lsoa;
    private SubjectType localAuthority;

    DataExportEngine engine;
    DataExportSpecificationBuilder builder = DataExportSpecificationBuilder.withGeoJsonExporter();
    Writer writer = new StringWriter();

    @Before
    public void addSubjectFixtures() throws Exception {
        engine =  new DataExportEngine(makeApiKeyProperties(), makeTestDownloadUtils());

        lsoa = TestFactory.makeNamedSubjectType("lsoa");
        localAuthority = TestFactory.makeNamedSubjectType("localAuthority");

        TestFactory.makeNamedSubject("E01000001");
        TestFactory.makeNamedSubject("E09000001");
        TestFactory.makeNamedSubject("E01002766");
        TestFactory.makeNamedSubject("E01002767");
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
                new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(),"lsoa").setMatcher("label", "E01000001")
        );

        engine.execute(builder.build(), writer);
        JSONAssert.assertEquals("{features: [{properties: {name: 'City of London 001A', label: 'E01000001'}}]}", writer.toString(), false);
    }

    @Test
    public void testObeysCache() throws Exception {
        // If we mark localAuthorities as imported...
        DatabaseJournal.addJournalEntry(new DatabaseJournalEntry("uk.org.tombolo.importer.ons.OaImporter", "localAuthority"));

        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "localAuthority").setMatcher("label", "E10000006")
        ).addDatasourceSpecification("uk.org.tombolo.importer.ons.OaImporter", "localAuthority", null);
        engine.execute(builder.build(), writer);

        // ...we expect the importer not to have imported them, so we should have no features
        JSONAssert.assertEquals("{type:'FeatureCollection', features:[]}", writer.toString(), false);
    }

    @Test
    public void testReimportsWhenForced() throws Exception {
        // If we mark localAuthorities as imported...
        DatabaseJournal.addJournalEntry(new DatabaseJournalEntry("uk.org.tombolo.importer.ons.OaImporter", "localAuthority"));

        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "localAuthority").setMatcher("label", "E06000001")
        ).addDatasourceSpecification("uk.org.tombolo.importer.ons.OaImporter", "localAuthority", "");

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
        TestFactory.makeTimedValue(lsoa, "E01000001", attribute, "2011-01-01T00:00:00", 100d);

        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "lsoa").setMatcher("label", "E01000001")
        ).addFieldSpecification(
                FieldBuilder.wrapperField("attributes", Arrays.asList(
                        FieldBuilder.latestValue("default_provider_label", "attr_label")
                ))
        );

        engine.execute(builder.build(), writer);
        JSONAssert.assertEquals("{" +
                "  features: [" +
                "    {" +
                "      properties: {" +
                "        name: 'City of London 001A'," +
                "        attributes: {" +
                "          attr_label: [" +
                "            {" +
                "              value: '100.0'," +
                "              timestamp: '2011-01-01T00:00:00'" +
                "            }" +
                "          ]" +
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
        TestFactory.makeTimedValue(lsoa, "E01000001", attribute, "2011-01-01T00:00", 100d);

        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "lsoa").setMatcher("label", "E01000001")
        ).addFieldSpecification(
                FieldBuilder.wrapperField("attributes", Arrays.asList(
                        FieldBuilder.valuesByTime("default_provider_label", "attr_label")
                ))
        );

        engine.execute(builder.build(), writer);
        JSONAssert.assertEquals("{" +
                "  features: [" +
                "    {" +
                "      properties: {" +
                "        name: 'City of London 001A'," +
                "        attributes: {" +
                "          attr_label: [" +
                "            {" +
                "              value: 100.0," +
                "              timestamp: '2011-01-01T00:00:00'" +
                "            }" +
                "          ]" +
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
                new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "localAuthority").setMatcher("label", "E09000001"))
                .addDatasourceSpecification("uk.org.tombolo.importer.londondatastore.LondonBoroughProfileImporter", "londonBoroughProfiles", "")
                .addFieldSpecification(
                        FieldBuilder.wrapperField("attributes", Arrays.asList(
                                FieldBuilder.valuesByTime("uk.gov.london", "populationDensity")
                        ))
                );

        engine.execute(builder.build(), writer);
        JSONAssert.assertEquals("{" +
                "  features: [" +
                "    {" +
                "      properties: {" +
                "        name: 'City of London'," +
                "        attributes: {" +
                "          populationDensity: [" +
                "            {" +
                "              value: 28.2," +
                "              timestamp: '2015-12-31T23:59:59'" +
                "            }" +
                "          ]" +
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
                        new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "lsoa").setMatcher("label", "E01002766"))
                .addDatasourceSpecification("uk.org.tombolo.importer.ons.CensusImporter", "qs103ew", "")
                .addFieldSpecification(
                        FieldBuilder.wrapperField("attributes", Arrays.asList(
                                FieldBuilder.fractionOfTotal("percentage_under_1_years_old_label")
                                        .addDividendAttribute("uk.gov.ons", "Age: Age under 1") // number under one year old
                                        .setDivisorAttribute("uk.gov.ons", "Age: All categories: Age") // total population
                        ))
                );

        engine.execute(builder.build(), writer);

        JSONAssert.assertEquals("{" +
                "  features: [" +
                "    {" +
                "      properties: {" +
                "        name: 'Islington 015E'," +
                "        attributes: {" +
                "          percentage_under_1_years_old_label: [" +
                "            {" +
                "              value: 0.012263099219620958," +
                "              timestamp: '2011-12-31T23:59:59'" +
                "            }" +
                "          ]" +
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
                new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "localAuthority").setMatcher("label", "E06000001"))
            .addDatasourceSpecification("uk.org.tombolo.importer.ons.OaImporter", "localAuthority", "")
            .addDatasourceSpecification("uk.org.tombolo.importer.londondatastore.LondonBoroughProfileImporter", "londonBoroughProfiles", "")
            .addFieldSpecification(
                    FieldBuilder.wrapperField("attributes", Collections.singletonList(
                            FieldBuilder.valuesByTime("uk.gov.london", "populationDensity")
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
                "        populationDensity: []" +
                "      }," +
                "      label: 'E06000001'" +
                "    }" +
                "  }]" +
                "}", writer.toString(), false);
    }

    @Test
    public void testMapsBetweenSubjectTypes() throws Exception {
        Subject cityOfLondon = TestFactory.makeNamedSubject("E09000001");
        Subject cityOfLondonLsoa = TestFactory.makeNamedSubject("E01000001"); // Subject contained by 'City of London'
        cityOfLondon.setShape(TestFactory.makePointGeometry(1d, 1d));
        cityOfLondonLsoa.setShape(TestFactory.makePointGeometry(1d, 1d));
        SubjectUtils.save(Arrays.asList(cityOfLondon, cityOfLondonLsoa));

        Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr_label");
        TestFactory.makeTimedValue(localAuthority, "E09000001", attribute, "2011-01-01T00:00:00", 100d);

        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "lsoa").setMatcher("label", "E01000001")
        ).addFieldSpecification(
                FieldBuilder.mapToContainingSubjectField(
                        "local_authority",
                        AbstractONSImporter.PROVIDER.getLabel(),
                        "localAuthority",
                        FieldBuilder.latestValue("default_provider_label", "attr_label")
                )
        );

        engine.execute(builder.build(), writer);
        String jsonString = writer.toString();
        JSONAssert.assertEquals("{" +
                        "  features: [" +
                        "    {" +
                        "      properties: {" +
                        "        local_authority: 100.0" +
                        "      }" +
                        "    }" +
                        "  ]"+
                        "}", jsonString, false);
    }

    @Test
    public void testAggregatesGeographically() throws Exception {
        Subject cityOfLondon = TestFactory.makeNamedSubject("E09000001");
        Subject cityOfLondonLsoa1 = TestFactory.makeNamedSubject("E01000001"); // Subjects contained by 'City of London'
        Subject cityOfLondonLsoa2 = TestFactory.makeNamedSubject("E01000002");
        cityOfLondon.setShape(TestFactory.makePointGeometry(1d, 1d));
        cityOfLondonLsoa1.setShape(TestFactory.makePointGeometry(1d, 1d));
        cityOfLondonLsoa2.setShape(TestFactory.makePointGeometry(1d, 1d));
        SubjectUtils.save(Arrays.asList(cityOfLondon, cityOfLondonLsoa1, cityOfLondonLsoa2));

        Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr_label");
        TestFactory.makeTimedValue(lsoa, "E01000001", attribute, "2011-01-01T00:00:00", 100d);
        TestFactory.makeTimedValue(lsoa, "E01000002", attribute, "2011-01-01T00:00:00", 110d);

        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "localAuthority").setMatcher("label", "E09000001")
        ).addFieldSpecification(
                FieldBuilder.geographicAggregation(
                        "local_authority",
                        AbstractONSImporter.PROVIDER.getLabel(),
                        "lsoa",
                        "mean",
                        FieldBuilder.latestValue(TestFactory.DEFAULT_PROVIDER.getLabel(), "attr_label")
                )
        );

        engine.execute(builder.build(), writer);
        JSONAssert.assertEquals("{" +
                "  features: [" +
                "    {" +
                "      properties: {" +
                "        local_authority: 105.0" +
                "      }" +
                "    }" +
                "  ]"+
                "}", writer.toString(), false);
    }

    @Test
    public void testExportsCSV() throws Exception {
        DataExportSpecificationBuilder csvBuilder = DataExportSpecificationBuilder.withCSVExporter();
        csvBuilder
                .addSubjectSpecification(
                        new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "lsoa").setMatcher("label", "E01002766"))
                .addDatasourceSpecification("uk.org.tombolo.importer.ons.CensusImporter", "qs103ew", "")
                .addFieldSpecification(
                        FieldBuilder.fractionOfTotal("percentage_under_1_years_old_label")
                                .addDividendAttribute("uk.gov.ons", "Age: Age under 1") // number under one year old
                                .setDivisorAttribute("uk.gov.ons", "Age: All categories: Age") // total population
                );

        engine.execute(csvBuilder.build(), writer);

        List<CSVRecord> records = CSVParser.parse(writer.toString(), CSVFormat.DEFAULT.withHeader()).getRecords();

        assertEquals(1, records.size());
        assertEquals("E01002766", records.get(0).get("label"));
        assertEquals("0.012263099219620958", records.get(0).get("percentage_under_1_years_old_label"));
    }

    /*
    As only one subject type is supported this test would fail,
    need to reactivate this when CensusImporter starts working with msoa and localAuthority
     */
    @Test
    @Ignore
    public void testExportsMultipleSubjectTypes() throws Exception {
        builder .addSubjectSpecification(
                new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "lsoa").setMatcher("label", "E01002766"))
                .addSubjectSpecification(
                        new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "localAuthority").setMatcher("label", "E08000035"))
                .addDatasourceSpecification("uk.org.tombolo.importer.ons.CensusImporter", "qs103ew", "")
                .addFieldSpecification(
                        FieldBuilder.wrapperField("attributes", Arrays.asList(
                                FieldBuilder.fractionOfTotal("percentage_under_1_years_old_label")
                                        .addDividendAttribute("uk.gov.ons", "Age: Age under 1") // number under one year old
                                        .setDivisorAttribute("uk.gov.ons", "Age: All categories: Age") // total population
                        ))
                );

        engine.execute(builder.build(), writer);
        System.out.println(writer.toString());

        JSONAssert.assertEquals("{" +
                "  features: [" +
                "    {" +
                "      properties: {" +
                "        name: 'Islington 015E'," +
                "        attributes: {" +
                "          percentage_under_1_years_old_label: [" +
                "            {" +
                "              value: 0.012263099219620958," +
                "              timestamp: '2011-12-31T23:59:59'" +
                "            }" +
                "          ]" +
                "        }," +
                "        label: 'E01002766'" +
                "      }" +
                "    }," +
                "    {" +
                "      properties: {" +
                "        name: 'Leeds'," +
                "        attributes: {" +
                "          percentage_under_1_years_old_label: [" +
                "              {" +
                "                value: 0.013229804986127467," +
                "                timestamp: '2011-12-31T23:59:59'" +
                "              }" +
                "          ]" +
                "        }," +
                "        label: 'E08000035'" +
                "      }" +
                "    }" +
                "  ]" +
                "}", writer.toString(), false);
    }

    @Test
    public void testGeneratesModellingField() throws Exception {
        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "lsoa").setMatcher("label", "E01002766")
        ).addFieldSpecification(
                FieldBuilder.modellingField("aLabel", "ModellingFieldTest")
        );

        engine.execute(builder.build(), writer);
        JSONAssert.assertEquals("{" +
                "  features: [{" +
                "    properties: {" +
                "      name: 'Islington 015E'," +
                "      label: 'E01002766'," +
                "      aLabel: { Fraction_of_80: [" +
                "        {" +
                "          value: 0.005016722408026756," +
                "          timestamp: '2011-12-31T23:59:59'" +
                "        }" +
                "      ]}" +
                "    }" +
                "  }]" +
                "}", writer.toString(), false);
    }

    @Test
    public void testGeneratesModellingFieldWhenNested() throws Exception {
        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "lsoa").setMatcher("label", "E01002766")
        ).addFieldSpecification(
                FieldBuilder.wrapperField("aWrapper", Collections.singletonList(
                    FieldBuilder.modellingField("aLabel", "ModellingFieldTest")))
        );

        engine.execute(builder.build(), writer);

        JSONAssert.assertEquals("{" +
                "  features: [{" +
                "    properties: {" +
                "      name: 'Islington 015E'," +
                "      label: 'E01002766'," +
                "      aWrapper: {" +
                "        aLabel: { Fraction_of_80: [" +
                "          {" +
                "            value: 0.005016722408026756," +
                "            timestamp: '2011-12-31T23:59:59'" +
                "          }" +
                "        ]}" +
                "      }" +
                "    }" +
                "  }]" +
                "}", writer.toString(), false);
    }

    @Test
    public void testExportsPercentiles() throws Exception {
        builder .addSubjectSpecification(
                new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "lsoa").setMatcher("label", "E0100276_"))
                .addDatasourceSpecification("uk.org.tombolo.importer.ons.CensusImporter", "qs103ew", "")
                .addFieldSpecification(
                        FieldBuilder.percentilesField("quartile", 4, false)
                                .set("valueField", FieldBuilder.latestValue("uk.gov.ons", "Age: All categories: Age")) // total population
                                .set("normalizationSubjects", Collections.singletonList(new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "lsoa").setMatcher("label", "E0100276_")))
                );

        engine.execute(builder.build(), writer);

        JSONAssert.assertEquals("{" +
                "  features: [" +
                "    {" +
                "      properties: {" +
                "        name: 'Islington 015E'," +
                "        label: 'E01002766'," +
                "        quartile: 1.0" +
                "      }" +
                "    }," +
                "    {" +
                "      properties: {" +
                "        name: 'Islington 011D'," +
                "        label: 'E01002767'," +
                "        quartile: 3.0" +
                "      }" +
                "    }" +
                "  ]" +
                "}", writer.toString(), false);
    }
}