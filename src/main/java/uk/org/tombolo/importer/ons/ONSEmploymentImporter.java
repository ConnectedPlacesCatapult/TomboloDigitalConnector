package uk.org.tombolo.importer.ons;

import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.utils.CSVUtils;
import uk.org.tombolo.importer.utils.extraction.CSVExtractor;
import uk.org.tombolo.importer.utils.extraction.ConstantExtractor;
import uk.org.tombolo.importer.utils.extraction.TimedValueExtractor;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * NOMIS API Claimants related importer.
 *
 * https://www.nomisweb.co.uk/query/select/getdatasetbytheme.asp?theme=72
 * NOTE: The scores of the variable ESAclaimants needs to be standardise per population density. Please use census importer to get the population density
 * The indicator needs to be standarised to a "per 1000 inhabitants" measurement to make it
 * Example of how to use in recipe:
 *
 * {
 *  "importerClass": "uk.org.tombolo.importer.ons.CensusImporter",
 *  "datasourceId": "qs102ew"
 * }
 *
 * {
 *  "importerClass": "uk.org.tombolo.importer.ons.ONSEmploymentImporter",
 *  "datasourceId": "ESAclaimants"
 * }
 *
 *  "fields": [
 *      {
 *      "fieldClass": "uk.org.tombolo.field.transformation.ArithmeticField",
 *      "label": "final",
 *      "operation": "mul",
 *       "field1": {
 *          "fieldClass": "uk.org.tombolo.field.transformation.ArithmeticField",
 *          "label": "Division",
 *          "operation": "div",
 *          "field1": {
 *              "fieldClass": "uk.org.tombolo.field.value.LatestValueField",
 *              "label": "ESAclaimants",
 *              "attribute": {
 *                  "provider": "uk.gov.ons",
 *                   "label": "ESAclaimants"
 *              }
 *           },
 *          "field2": {
 *           "fieldClass": "uk.org.tombolo.field.value.LatestValueField",
 *           "label": "qs102ew",
 *           "attribute": {
 *               "provider": "uk.gov.ons",
 *               "label": "Area/Population Density: All usual residents"
 *           }
 *          }
 *       },
 *   "field2": {
 *      "fieldClass": "uk.org.tombolo.field.value.ConstantField",
 *      "label": "Constant",
 *      "value": 1000
 *      }
 *     }
 *  ]
 */

public class ONSEmploymentImporter extends AbstractONSImporter {

    private enum DatasourceId {
        claimantsCount(new DatasourceSpec(
                ONSEmploymentImporter.class,
                "claimantsCount",
                "Claimants per LSOA",
                "This experimental series counts the number of people claiming Jobseeker's Allowance plus those " +
                        "who claim Universal Credit and are required to seek work and be available for work and " +
                        "replaces the number of people claiming Jobseeker's Allowance as the headline indicator " +
                        "of the number of people claiming benefits principally for the reason of being unemployed. " +
                        "The JSA datasets have all been moved to a new Jobseeker's Allowance theme.",
                "http://www.nomisweb.co.uk/api/v01/dataset/NM_162_1.data.csv?" +
                        "geography=1249902593...1249937345&" +
                        "date=latestMINUS1-latest&" +
                        "gender=0&" +
                        "age=0&" +
                        "measure=1&" +
                        "measures=20100&" +
                        "select=date_name,geography_name,geography_code,gender_name,age_name,measure_name,measures_name," +
                        "obs_value,obs_status_name")
        ),
        JSAclaimantsCount(new DatasourceSpec(
                ONSEmploymentImporter.class,
                "JSAclaimantsCount",
                "Claimants per Local Authority",
                "Total counts of Jobseeker's Allowance (JSA) claimants " +
                        "Aged 16-64, All claimant durations,  All sexes. " +
                        "Totals exclude non-computerised clerical claims (approx. 1%). " +
                        "Available for Local Authorities. For more details visit https://www.nomisweb.co.uk/api/v01/help",
                "http://www.nomisweb.co.uk/api/v01/dataset/NM_18_1.csv?geography=TYPE463&"+
                        "date=latestMINUS1-latest&"+
                        "sex=7&"+
                        "age=0&"+
                        "duration=0&"+
                        "measures=20100&"+
                        "select=date_name,geography_name,geography_code,measures_name,duration_name,sex_name,obs_value,obs_status_name")
        ),
        JSAclaimantsProportion(new DatasourceSpec(
                ONSEmploymentImporter.class,
                "JSAclaimantsProportion",
                "Jobseeker's Allowance (JSA) claimants proportion of resident population per Local Authority",
                "Claimants proportion of resident population of Jobseeker's Allowance (JSA) claimants " +
                        "Aged 16-64, All claimant durations,  All sexes. " +
                        "Totals exclude non-computerised clerical claims (approx. 1%). " +
                        "Available for Local Authorities. For more details visit https://www.nomisweb.co.uk/api/v01/help",
                "http://www.nomisweb.co.uk/api/v01/dataset/NM_18_1.csv?geography=TYPE463&"+
                        "date=latestMINUS1-latest&"+
                        "sex=7&"+
                        "age=0&"+
                        "duration=0&"+
                        "measures=20206&"+
                        "select=date_name,geography_name,geography_code,measures_name,duration_name,sex_name,obs_value,obs_status_name")
        ),
        ESAclaimants(new DatasourceSpec(
                ONSEmploymentImporter.class,
                "ESAclaimants",
                "Total number of people who are claiming Employment and Support Allowance (ESA) per Local Authority",
                "Total number of people who are claiming Employment and Support Allowance (ESA) per Local Authority " +
                        "Aged 16-64, All claimant durations,  All sexes. " +
                        "Available for Local Authorities. For more details visit https://www.nomisweb.co.uk/api/v01/help",
                "http://www.nomisweb.co.uk/api/v01/dataset/NM_134_1.csv?geography=TYPE463&"+
                        "date=latestMINUS1-latest&"+
                        "sex=7&"+
                        "age=0&"+
                        "esa_phase=0&"+
                        "payment_type=0&"+
                        "icdgp_condition=0&"+
                        "duration=0&"+
                        "ethnic_group=0&"+
                        "measures=20100&"+
                        "select=date_name,geography_name,geography_code,measures_name,duration_name,sex_name,obs_value,obs_status_name")
        ),
        APSEmploymentRate(new DatasourceSpec(
                ONSWagesImporter.class,
                "APSEmploymentRate",
                "Annual Population Survey (APS) Employment Rate per Local Authority",
                "A residence based labour market survey encompassing population, economic activity (employment) per local authority " +
                        "Aged 16-64" +
                        "Available for Local Authorities. The date appears in the dataset as yearly interval and are imported using the latest date. " +
                        "For more details visit https://www.nomisweb.co.uk/api/v01/help",
                "http://www.nomisweb.co.uk/api/v01/dataset/NM_17_5.data.csv?geography=TYPE463&"+
                        "date=latestMINUS1-latest&"+
                        "variable=45&"+
                        "measures=20599&"+
                        "select=date_name,geography_name,geography_code,variable_name,measures_name,obs_value,obs_status_name")
        ),
        APSUnemploymentRate(new DatasourceSpec(
                ONSEmploymentImporter.class,
                "APSUnemploymentRate",
                "Annual Population Survey (APS) Unemployment Rate per Local Authority",
                "A residence based labour market survey encompassing population, economic activity (unemployment) per local authority " +
                        "Aged 16-64" +
                        "Available for Local Authorities. The date appears in the dataset as yearly interval and are imported using the latest date" +
                        "For more details visit https://www.nomisweb.co.uk/api/v01/help",
                "http://www.nomisweb.co.uk/api/v01/dataset/NM_17_5.data.csv?geography=TYPE463&"+
                        "date=latestMINUS1-latest&"+
                        "variable=84&"+
                        "measures=20599&"+
                        "select=date_name,geography_name,geography_code,variable_name,measures_name,obs_value,obs_status_name")
        ),
        ONSJobsDensity(new DatasourceSpec(
                ONSEmploymentImporter.class,
                "ONSJobsDensity",
                "Jobs density per Local Authority",
                "The number of jobs in an area divided by the resident population aged 16-64 in that area. For example, a job density of 1.0 " +
                        "would mean that there is one job for every resident aged 16-64. " +
                        "For more details visit https://www.nomisweb.co.uk/query/construct/summary.asp?mode=construct&version=0&dataset=57",
                "http://www.nomisweb.co.uk/api/v01/dataset/NM_57_1.data.csv?geography=TYPE463&"+
                        "date=latestMINUS1-latest&"+
                        "item=3&"+
                        "measures=20100&"+
                        "select=date_name,geography_name,geography_code,item_name,measures_name,obs_value,obs_status_name")
        ),
        ONSTotalJobs(new DatasourceSpec(
                ONSEmploymentImporter.class,
                "ONSTotalJobs",
                "Total Jobs per Local Authority",
                "The total number of jobs is a workplace-based measure and comprises employee jobs, self-employed, government-supported trainees and HM Forces. " +
                        "For more details visit https://www.nomisweb.co.uk/query/construct/summary.asp?mode=construct&version=0&dataset=57",
                "http://www.nomisweb.co.uk/api/v01/dataset/NM_57_1.data.csv?geography=TYPE463&"+
                        "date=latestMINUS1-latest&"+
                        "item=1&"+
                        "measures=20100&"+
                        "select=date_name,geography_name,geography_code,item_name,measures_name,obs_value,obs_status_name")
        ),
        ONSGrossAnnualIncome(new DatasourceSpec(
                ONSEmploymentImporter.class,
                "ONSGrossAnnualIncome",
                "Median Gross Annual Income in GB pounds per Local Authority",
                "Annual Survey of Hours and Earnings - Residential Gross Annual Income. " +
                        "For more details visit https://www.nomisweb.co.uk/datasets/asher",
                "http://www.nomisweb.co.uk/api/v01/dataset/NM_30_1.data.csv?geography=TYPE463&"+
                        "date=latestMINUS1-latest&"+
                        "sex=8&"+
                        "item=2&"+
                        "pay=7&"+
                        "measures=20100&"+
                        "select=date_name,geography_name,geography_code,item_name,measures_name,obs_value,obs_status_name")
        )
        ;
        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasource) {
            this.datasourceSpec = datasource;
        }
    }

    private enum AttributeId {claimantsCount,JSAclaimantsCount,JSAclaimantsProportion,ESAclaimants, APSEmploymentRate,APSUnemploymentRate,
        ONSJobsDensity,ONSTotalJobs,ONSGrossAnnualIncome};

    public ONSEmploymentImporter(){
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceIdString) throws Exception {
        return DatasourceId.valueOf(datasourceIdString).datasourceSpec;
    }

    @Override
    protected List<String> getOaDatasourceIds() {
        return Arrays.asList(OaImporter.OaType.lsoa.datasourceSpec.getId(),
                OaImporter.OaType.localAuthority.datasourceSpec.getId());
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {

        // Check the datasourceid to reference the corresponding url
        String whichDatasource = datasource.getDatasourceSpec().getId();

        // Columns containing the subject type and the time
        CSVExtractor subjectLabelExtractor = new CSVExtractor(2);
        CSVExtractor timestampExtractor = new CSVExtractor(0);

        List<TimedValueExtractor> extractors = new ArrayList<>();

        int columnID = 0;
        int subjectID = 0;
        switch (whichDatasource) {
            case "claimantsCount":
                columnID = 7;
                subjectID = 0;
                break;
            case "JSAclaimantsCount":
            case "JSAclaimantsProportion":
            case "ESAclaimants":
                columnID = 6;
                subjectID = 1;
                break;
            case "APSEmploymentRate":
            case "APSUnemploymentRate":
            case "ONSJobsDensity":
            case "ONSTotalJobs":
            case "ONSGrossAnnualIncome":
                columnID = 5;
                subjectID = 1;
                break;
        }
        CSVExtractor valueExtractor = new CSVExtractor(columnID);
        extractors.add(new TimedValueExtractor(
                        getProvider(),
                        SubjectTypeUtils.getSubjectTypeByProviderAndLabel(AbstractONSImporter.PROVIDER.getLabel(), getOaDatasourceIds().get(subjectID)),
                        subjectLabelExtractor,
                        new ConstantExtractor(whichDatasource),
                        timestampExtractor,
                        valueExtractor
                )
        );
        File localFile = downloadUtils.fetchFile(new URL(getDatasourceSpec(whichDatasource).getUrl()), getProvider().getLabel(), ".csv");
        CSVUtils.extractAndSaveTimedValues(extractors, this, localFile);
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceId) {
        return Arrays.asList(
                new Attribute(getProvider(), AttributeId.claimantsCount.name(), "Number of claimants"),
                new Attribute(getProvider(), AttributeId.JSAclaimantsCount.name(), "Total counts of Jobseeker's Allowance (JSA) claimants"),
                new Attribute(getProvider(), AttributeId.JSAclaimantsProportion.name(), "Jobseeker's Allowance (JSA) claimants proportion (represented as fraction) per LA"),
                new Attribute(getProvider(), AttributeId.ESAclaimants.name(), "Total number of people who are claiming Employment and Support Allowance (ESA) per LA"),
                new Attribute(getProvider(), AttributeId.APSEmploymentRate.name(), "Annual Population Survey (APS) Employment Rate per LA"),
                new Attribute(getProvider(), AttributeId.APSUnemploymentRate.name(), "Annual Population Survey (APS) Unemployment Rate per LA"),
                new Attribute(getProvider(), AttributeId.ONSJobsDensity.name(), "The number of jobs in an area divided by the resident population aged 16-64 in that area"),
                new Attribute(getProvider(), AttributeId.ONSTotalJobs.name(), "The total number of jobs is a workplace-based measure and comprises employee jobs, self-employed, government-supported trainees and HM Forces"),
                new Attribute(getProvider(), AttributeId.ONSGrossAnnualIncome.name(), "Annual Survey of Hours and Earnings - Residential Gross Annual Income"));
    }

}

