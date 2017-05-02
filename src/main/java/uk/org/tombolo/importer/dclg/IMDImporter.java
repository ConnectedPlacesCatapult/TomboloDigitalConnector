package uk.org.tombolo.importer.dclg;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.Importer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This importer is for the DCLG IMD dataset
 * https://www.gov.uk/government/statistics/english-indices-of-deprivation-2015
 * https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/467774/File_7_ID_2015_All_ranks__deciles_and_scores_for_the_Indices_of_Deprivation__and_population_denominators.csv
 */
public class IMDImporter extends AbstractDCLGImporter implements Importer {

    private enum DatasourceId {imd};
    private enum GeographyLabel {england};
    private enum TemporalLabel {y2015};

    private static final String IMD_DATA_CSV
            = "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/467774/" +
            "File_7_ID_2015_All_ranks__deciles_and_scores_for_the_Indices_of_Deprivation__and_population_denominators.csv";

    public IMDImporter(Config config) {
        super(config);
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
        geographyLabels = stringsFromEnumeration(GeographyLabel.class);
        temporalLabels = stringsFromEnumeration(TemporalLabel.class);
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {
        DatasourceId datasourceLabel = DatasourceId.valueOf(datasourceId);
        switch(datasourceLabel){
            case imd:
                Datasource datasource = new Datasource(
                        getClass(),
                        datasourceLabel.name(),
                        getProvider(),
                        "English indices of deprivation 2015",
                        "Statistics on relative deprivation in small areas in England.");
                datasource.setUrl("https://www.gov.uk/government/statistics/english-indices-of-deprivation-2015");
                datasource.addAllTimedValueAttributes(getAttributes());
                return datasource;
            default:
                return null;
        }
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {
        // Save timed values
        LocalDateTime timestamp = LocalDateTime.parse("2015-01-01T00:00:01", TimedValueId.DATE_TIME_FORMATTER);
        String line = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(
                downloadUtils.fetchInputStream(new URL(IMD_DATA_CSV), getProvider().getLabel(), ".csv")));
        while ((line = br.readLine())!=null){
            CSVParser parser = CSVParser.parse(line, CSVFormat.DEFAULT);
            List<CSVRecord> records = parser.getRecords();

            String lsoaLabel = records.get(0).get(0);

            if (lsoaLabel.startsWith("LSOA"))
                continue;
            Subject lsoa = SubjectUtils.getSubjectByLabel(lsoaLabel);

            if (lsoa == null)
                continue;

            List<TimedValue> timedValueBuffer = new ArrayList<>();
            for (int i = 0; i<datasource.getTimedValueAttributes().size(); i++){
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

    private List<Attribute> getAttributes(){
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(getProvider(),"imd.score", "IMD Score", "Index of Multiple Deprivation (IMD) Score", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.rank", "IMD Rank", "Index of Multiple Deprivation (IMD) Rank (where 1 is most deprived)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.decile", "IMD Decile", "Index of Multiple Deprivation (IMD) Decile (where 1 is most deprived 10% of LSOAs)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.income.score", "IMD Income Score", "Income Score (rate)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.income.rank", "IMD Income Rank", "Income Rank (where 1 is most deprived)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.income.decile", "IMD Income Decile", "Income Decile (where 1 is most deprived 10% of LSOAs)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.employment.score", "IMD Employment Score", "Employment Score (rate)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.employment.rank", "IMD Employment Rank", "Employment Rank (where 1 is most deprived)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.employment.decile", "IMD Employment Decile", "Employment Decile (where 1 is most deprived 10% of LSOAs)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.education.score", "IMD Education Score", "Education, Skills and Training Score", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.education.rank", "IMD Education Rank", "Education, Skills and Training Rank (where 1 is most deprived)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.education.decile", "IMD Education Decile", "Education, Skills and Training Decile (where 1 is most deprived 10% of LSOAs)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.disability.score", "IMD Disability Score", "Health Deprivation and Disability Score", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.disability.rank", "IMD Disability Rank", "Health Deprivation and Disability Rank (where 1 is most deprived)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.disability.decile", "IMD Disability Decile", "Health Deprivation and Disability Decile (where 1 is most deprived 10% of LSOAs)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.crime.score", "IMD Crime Score", "Crime Score", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.crime.rank", "IMD Crime Rank", "Crime Rank (where 1 is most deprived)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.crime.decile", "IMD Crime Decile", "Crime Decile (where 1 is most deprived 10% of LSOAs)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.housing.score", "IMD Housing Score", "Barriers to Housing and Services Score", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.housing.rank", "IMD Housing Rank", "Barriers to Housing and Services Rank (where 1 is most deprived)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.housing.decile", "IMD Housing Decile", "Barriers to Housing and Services Decile (where 1 is most deprived 10% of LSOAs)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.environment.score", "IMD Environment Score", "Living Environment Score", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.environment.rank", "IMD Environment Rank", "Living Environment Rank (where 1 is most deprived)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.environment.decile", "IMD Environment Decile", "Living Environment Decile (where 1 is most deprived 10% of LSOAs)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.idaci.score", "IMD IDACI Score", "Income Deprivation Affecting Children Index (IDACI) Score (rate)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.idaci.rank", "IMD IDACI Rank", "Income Deprivation Affecting Children Index (IDACI) Rank (where 1 is most deprived)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.idaci.decile", "IMD IDACI Decile", "Income Deprivation Affecting Children Index (IDACI) Decile (where 1 is most deprived 10% of LSOAs)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.idaopi.score", "IMD IDAOPI Score", "Income Deprivation Affecting Older People (IDAOPI) Score (rate)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.idaopi.rank", "IMD IDAOPI Rank", "Income Deprivation Affecting Older People (IDAOPI) Rank (where 1 is most deprived)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.idaopi.decile", "IMD IDAOPI Decile", "Income Deprivation Affecting Older People (IDAOPI) Decile (where 1 is most deprived 10% of LSOAs)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.cypsd.score", "IMD CYPSD Score", "Children and Young People Sub-domain Score", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.cypsd.rank", "IMD CYPSD Rank", "Children and Young People Sub-domain Rank (where 1 is most deprived)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.cypsd.decile", "IMD CYPSD Decile", "Children and Young People Sub-domain Decile (where 1 is most deprived 10% of LSOAs)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.assd.score", "IMD ASSD Score", "Adult Skills Sub-domain Score", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.assd.rank", "IMD ASSD Rank", "Adult Skills Sub-domain Rank (where 1 is most deprived)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.assd.decile", "IMD ASSD Decile", "Adult Skills Sub-domain Decile (where 1 is most deprived 10% of LSOAs)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.gbsd.score", "IMD GBSD Score", "Geographical Barriers Sub-domain Score", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.gbsd.rank", "IMD GBSD Rank", "Geographical Barriers Sub-domain Rank (where 1 is most deprived)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.gbsd.decile", "IMD GBSD Decile", "Geographical Barriers Sub-domain Decile (where 1 is most deprived 10% of LSOAs)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.wbsd.score", "IMD WBSD Score", "Wider Barriers Sub-domain Score", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.wbsd.rank", "IMD WBSD Rank", "Wider Barriers Sub-domain Rank (where 1 is most deprived)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.wbsd.decile", "IMD WBSD Decile", "Wider Barriers Sub-domain Decile (where 1 is most deprived 10% of LSOAs)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.isd.score", "IMD ISD Score", "Indoors Sub-domain Score", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.isd.rank", "IMD ISD Rank", "Indoors Sub-domain Rank (where 1 is most deprived)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.isd.decile", "IMD ISD Decile", "Indoors Sub-domain Decile (where 1 is most deprived 10% of LSOAs)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.osd.score", "IMD OSD Score", "Outdoors Sub-domain Score", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.osd.rank", "IMD OSD Rank", "Outdoors Sub-domain Rank (where 1 is most deprived)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"imd.osd.decile", "IMD OSD Decile", "Outdoors Sub-domain Decile (where 1 is most deprived 10% of LSOAs)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"totalPopulation", "Total Population", "Total population: mid 2012 (excluding prisoners)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"childrenPopulation", "Children Population", "Dependent Children aged 0-15: mid 2012 (excluding prisoners)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"adultPopulation", "Adult Population", "Population aged 16-59: mid 2012 (excluding prisoners)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"elderlyPopulation", "Elderly Population", "Older population aged 60 and over: mid 2012 (excluding prisoners)", Attribute.DataType.numeric));
        attributes.add(new Attribute(getProvider(),"workingAgePopulation", "Working Age Population", "Working age population 18-59/64: for use with Employment Deprivation Domain (excluding prisoners)", Attribute.DataType.numeric));
        return attributes;
    }
}
