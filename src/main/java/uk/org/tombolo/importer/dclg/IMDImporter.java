package uk.org.tombolo.importer.dclg;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.ons.OaImporter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This importer is for the DCLG IMD dataset
 * https://www.gov.uk/government/statistics/english-indices-of-deprivation-2015
 * https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/467774/File_7_ID_2015_All_ranks__deciles_and_scores_for_the_Indices_of_Deprivation__and_population_denominators.csv
 */
public class IMDImporter extends AbstractDCLGImporter {

    private enum DatasourceId {
        imd(new DatasourceSpec(
                IMDImporter.class,
                "imd",
                "English indices of deprivation 2015",
                "Statistics on relative deprivation in small areas in England.",
                "https://www.gov.uk/government/statistics/english-indices-of-deprivation-2015")
        );

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }

    private enum GeographyLabel {england}
    private enum TemporalLabel {y2015}

    private static final String IMD_DATA_CSV
            = "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/467774/" +
            "File_7_ID_2015_All_ranks__deciles_and_scores_for_the_Indices_of_Deprivation__and_population_denominators.csv";

    public IMDImporter() {
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
        geographyLabels = stringsFromEnumeration(GeographyLabel.class);
        temporalLabels = stringsFromEnumeration(TemporalLabel.class);
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        return DatasourceId.valueOf(datasourceId).datasourceSpec;

    }

    @Override
    public List<SubjectType> getSubjectTypes(String datasourceId) {
        return Collections.singletonList(OaImporter.getSubjectType(OaImporter.OaType.lsoa));
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        // Save timed values
        LocalDateTime timestamp = LocalDateTime.parse("2015-01-01T00:00:01", TimedValueId.DATE_TIME_FORMATTER);
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(
                downloadUtils.fetchInputStream(new URL(IMD_DATA_CSV), getProvider().getLabel(), ".csv")));
        while ((line = br.readLine())!=null){
            CSVParser parser = CSVParser.parse(line, CSVFormat.DEFAULT);
            List<CSVRecord> records = parser.getRecords();

            String lsoaLabel = records.get(0).get(0);

            if (lsoaLabel.startsWith("LSOA"))
                continue;
            Subject lsoa = SubjectUtils.getSubjectByTypeAndLabel(datasource.getUniqueSubjectType(), lsoaLabel);

            if (lsoa == null)
                continue;

            List<TimedValue> timedValueBuffer = new ArrayList<>();
            for (int i = 0; i < datasource.getTimedValueAttributes().size(); i++){
                TimedValue timedValue = new TimedValue(
                        lsoa,
                        datasource.getTimedValueAttributes().get(i),
                        timestamp,
                        Double.valueOf(records.get(0).get(i+4)));
                timedValueBuffer.add(timedValue);
            }
            saveAndClearTimedValueBuffer(timedValueBuffer);
        }
    }

    private enum AttributeId {
        score("Index of Multiple Deprivation (IMD) Score"),
        rank("Index of Multiple Deprivation (IMD) Rank (where 1 is most deprived)"),
        decile("Index of Multiple Deprivation (IMD) Decile (where 1 is most deprived 10% of LSOAs)"),
        income_score("Income Score (rate)"),
        income_rank("Income Rank (where 1 is most deprived)"),
        income_decile("Income Decile (where 1 is most deprived 10% of LSOAs)"),
        employment_score("Employment Score (rate)"),
        employment_rank("Employment Rank (where 1 is most deprived)"),
        employment_decile("Employment Decile (where 1 is most deprived 10% of LSOAs)"),
        education_score("Education, Skills and Training Score"),
        education_rank("Education, Skills and Training Rank (where 1 is most deprived)"),
        education_decile("Education, Skills and Training Decile (where 1 is most deprived 10% of LSOAs)"),
        disability_score("Health Deprivation and Disability Score"),
        disability_rank("Health Deprivation and Disability Rank (where 1 is most deprived)"),
        disability_decile("Health Deprivation and Disability Decile (where 1 is most deprived 10% of LSOAs)"),
        crime_score("Crime Score"),
        crime_rank("Crime Rank (where 1 is most deprived)"),
        crime_decile("Crime Decile (where 1 is most deprived 10% of LSOAs)"),
        housing_score("Barriers to Housing and Services Score"),
        housing_rank("Barriers to Housing and Services Rank (where 1 is most deprived)"),
        housing_decile("Barriers to Housing and Services Decile (where 1 is most deprived 10% of LSOAs)"),
        environment_score("Living Environment Score"),
        environment_rank("Living Environment Rank (where 1 is most deprived)"),
        environment_decile("Living Environment Decile (where 1 is most deprived 10% of LSOAs)"),
        idaci_score("Income Deprivation Affecting Children Index (IDACI) Score (rate)"),
        idaci_rank("Income Deprivation Affecting Children Index (IDACI) Rank (where 1 is most deprived)"),
        idaci_decile("Income Deprivation Affecting Children Index (IDACI) Decile (where 1 is most deprived 10% of LSOAs)"),
        idaopi_score("Income Deprivation Affecting Older People (IDAOPI) Score (rate)"),
        idaopi_rank("Income Deprivation Affecting Older People (IDAOPI) Rank (where 1 is most deprived)"),
        idaopi_decile("Income Deprivation Affecting Older People (IDAOPI) Decile (where 1 is most deprived 10% of LSOAs)"),
        cypsd_score("Children and Young People Sub-domain Score"),
        cypsd_rank("Children and Young People Sub-domain Rank (where 1 is most deprived)"),
        cypsd_decile("Children and Young People Sub-domain Decile (where 1 is most deprived 10% of LSOAs)"),
        assd_score("Adult Skills Sub-domain Score"),
        assd_rank("Adult Skills Sub-domain Rank (where 1 is most deprived)"),
        assd_decile("Adult Skills Sub-domain Decile (where 1 is most deprived 10% of LSOAs)"),
        gbsd_score("Geographical Barriers Sub-domain Score"),
        gbsd_rank("Geographical Barriers Sub-domain Rank (where 1 is most deprived)"),
        gbsd_decile("Geographical Barriers Sub-domain Decile (where 1 is most deprived 10% of LSOAs)"),
        wbsd_score("Wider Barriers Sub-domain Score"),
        wbsd_rank("Wider Barriers Sub-domain Rank (where 1 is most deprived)"),
        wbsd_decile("Wider Barriers Sub-domain Decile (where 1 is most deprived 10% of LSOAs)"),
        isd_score("Indoors Sub-domain Score"),
        isd_rank("Indoors Sub-domain Rank (where 1 is most deprived)"),
        isd_decile("Indoors Sub-domain Decile (where 1 is most deprived 10% of LSOAs)"),
        osd_score("Outdoors Sub-domain Score"),
        osd_rank("Outdoors Sub-domain Rank (where 1 is most deprived)"),
        osd_decile("Outdoors Sub-domain Decile (where 1 is most deprived 10% of LSOAs)"),
        totalPopulation("Total population: mid 2012 (excluding prisoners)"),
        childrenPopulation("Dependent Children aged 0-15: mid 2012 (excluding prisoners)"),
        adultPopulation("Population aged 16-59: mid 2012 (excluding prisoners)"),
        elderlyPopulation("Older population aged 60 and over: mid 2012 (excluding prisoners)"),
        workingAgePopulation("Working age population 18-59/64: for use with Employment Deprivation Domain (excluding prisoners)")
        ;

        String description;
        AttributeId(String description) { this.description = description; }

    }
    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceId){
        List<Attribute> attributes = new ArrayList<>();
        Arrays.stream(AttributeId.values()).map(attributeId ->
                new Attribute(getProvider(), attributeId.name(), attributeId.description)).forEach(attributes::add);

        return attributes;
    }
}
