package uk.org.tombolo.importer.ons;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.DatasourceSpec;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.importer.utils.ExcelUtils;
import uk.org.tombolo.importer.utils.extraction.ConstantExtractor;
import uk.org.tombolo.importer.utils.extraction.RowCellExtractor;
import uk.org.tombolo.importer.utils.extraction.TimedValueExtractor;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Importer for the ONS Wages data
 *
 * http://www.ons.gov.uk/employmentandlabourmarket/peopleinwork/earningsandworkinghours/datasets/placeofresidencebylocalauthorityashetable8
 */
public class ONSWagesImporter extends AbstractONSImporter {

    private enum DatasourceId {
        wages(new DatasourceSpec(
                ONSWagesImporter.class,
                "wages",
                "Wages per Local Authority",
                "Estimates of paid hours worked, weekly, hourly and annual earnings for UK employees by gender " +
                        "and full/part-time working by home based Region to Local Authority level.",
                "http://www.ons.gov.uk/employmentandlabourmarket/" +
                        "peopleinwork/earningsandworkinghours/datasets/placeofresidencebylocalauthorityashetable8")
        );

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }

    private enum AttributePrefix {
        asheTable81aWeeklyPayGross,
        //asheTable81bWeeklyPayGrossCV,
        asheTable82aWeeklyPayExcludingOvertime,
        //asheTable82bWeeklyPayExcludingOvertimeCV,
        asheTable83aBasicPayIncludingOtherPay,
        //asheTable83bBasicPayIncludingOtherPayCV,
        asheTable84aOvertimePay,
        //asheTable84bOvertimePayCV,
        asheTable85aHourlyPayGross,
        //asheTable85bHourlyPayGrossCV,
        asheTable86aHourlyPayExcludingOvertime,
        //asheTable86bHourlyPayExcludingOvertimeCV,
        asheTable87aAnnualPayGross,
        //asheTable87bAnnualPayGrossCV,
        asheTable88aAnnualPayIncentive,
        //asheTable88bAnnualPayIncentiveCV,
        asheTable89aPaidHoursWorkedTotal,
        //asheTable89bPaidHoursWorkedTotalCV,
        asheTable810aPaidHoursWorkedBasic,
        //asheTable810bPaidHoursWorkedBasicCV,
        asheTable811aPaidHoursWorkedOvertime,
        //asheTable811bPaidHoursWorkedOvertimeCV,

    };
    private String[] attributeNames = {
            "Weekly Pay Gross",
            //"Weekly Pay Gross (Coefficients of Variation)",
            "Weekly Pay Excluding Overtime",
            //"Weekly Pay Excluding Overtime (Coefficients of Variation)",
            "Basic Pay Including Other Pay",
            //"Basic Pay Including Other Pay (Coefficients of Variation)",
            "Overtime Pay",
            //"Overtime Pay (Coefficients of Variation)",
            "Hourly Pay Gross",
            //"Hourly Pay Gross (Coefficients of Variation)",
            "Hourly Pay Excluding Overtime",
            //"Hourly Pay Excluding Overtime (Coefficients of Variation)",
            "Annual Pay Gross",
            //"Annual Pay Gross (Coefficients of Variation)",
            "Annual Pay Incentive",
            //"Annual Pay Incentive (Coefficients of Variation)",
            "Paid Hours Worked Total",
            //"Paid Hours Worked Total (Coefficients of Variation)",
            "Paid Hours Worked Basic",
            //"Paid Hours Worked Basic (Coefficients of Variation)",
            "Paid Hours Worked Overtime"
            //"Paid Hours Worked Overtime (Coefficients of Variation)"
    };

    private static final String DATAFILE = "http://www.ons.gov.uk/file?" +
            "uri=/employmentandlabourmarket/peopleinwork/earningsandworkinghours/datasets/" +
            "placeofresidencebylocalauthorityashetable8/2016/table82016provisional.zip";

    private String[] bookNames = {
            "PROV - Home Geography Table 8.1a   Weekly pay - Gross 2016.xls",
            //"PROV - Home Geography Table 8.1b   Weekly pay - Gross 2016 CV.xls",
            "PROV - Home Geography Table 8.2a   Weekly pay - Excluding overtime 2016.xls",
            //"PROV - Home Geography Table 8.2b   Weekly pay - Excluding overtime 2016 CV.xls",
            "PROV - Home Geography Table 8.3a   Basic Pay - Including other pay 2016.xls",
            //"PROV - Home Geography Table 8.3b   Basic Pay - Including other pay 2016 CV.xls",
            "PROV - Home Geography Table 8.4a   Overtime pay 2016.xls",
            //"PROV - Home Geography Table 8.4b   Overtime pay 2016 CV.xls",
            "PROV - Home Geography Table 8.5a   Hourly pay - Gross 2016.xls",
            //"PROV - Home Geography Table 8.5b   Hourly pay - Gross 2016 CV.xls",
            "PROV - Home Geography Table 8.6a   Hourly pay - Excluding overtime 2016.xls",
            //"PROV - Home Geography Table 8.6b   Hourly pay - Excluding overtime 2016 CV.xls",
            "PROV - Home Geography Table 8.7a   Annual pay - Gross 2016.xls",
            //"PROV - Home Geography Table 8.7b   Annual pay - Gross 2016 CV.xls",
            "PROV - Home Geography Table 8.8a   Annual pay - Incentive 2016.xls",
            //"PROV - Home Geography Table 8.8b   Annual pay - Incentive 2016 CV.xls",
            "PROV - Home Geography Table 8.9a   Paid hours worked - Total 2016.xls",
            //"PROV - Home Geography Table 8.9b   Paid hours worked - Total 2016 CV.xls",
            "PROV - Home Geography Table 8.10a   Paid hours worked - Basic 2016.xls",
            //"PROV - Home Geography Table 8.10b   Paid hours worked - Basic 2016 CV.xls",
            "PROV - Home Geography Table 8.11a   Paid hours worked - Overtime 2016.xls",
            //"PROV - Home Geography Table 8.11b   Paid hours worked - Overtime 2016 CV.xls"
    };
    private String[] sheetNames = {"All",
            "Male", "Female",
            "Full-Time", "Part-Time",
            "Male Full-Time", "Male Part-Time",
            "Female Full-Time", "Female Part-Time"};
    private String[] metricNames = {"Mean", "Median"};

    public ONSWagesImporter(){
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceIdString) throws Exception {
        return DatasourceId.valueOf(datasourceIdString).datasourceSpec;
    }

    @Override
    protected List<String> getOaDatasourceIds() {
        return Collections.singletonList(OaImporter.OaType.localAuthority.datasourceSpec.getId());
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        SubjectType subjectType = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());

        ExcelUtils excelUtils = new ExcelUtils();

        File localFile = downloadUtils.fetchFile(new URL(DATAFILE), getProvider().getLabel(), ".zip");
        ZipFile zipFile = new ZipFile(localFile);
        for (AttributePrefix attributePrefix : AttributePrefix.values()){
            ZipArchiveEntry zipEntry = zipFile.getEntry(bookNames[attributePrefix.ordinal()]);
            Workbook workbook = excelUtils.getWorkbook(zipFile.getInputStream(zipEntry));
            for (String sheetName : sheetNames) {
                RowCellExtractor subjectLabelExtractor = new RowCellExtractor(1, CellType.STRING);
                ConstantExtractor timestampExtractor = new ConstantExtractor("2016");   // FIXME: Need to generalise when we update Datasource to be time aware

                List<TimedValueExtractor> extractors = new ArrayList<>();

                for (String metricName : metricNames) {
                    ConstantExtractor attributeLabelExtractor = new ConstantExtractor(getAttributeLabel(attributePrefix, sheetName, metricName));
                    RowCellExtractor valueExtractor;
                    switch (metricName){
                        case "Mean":
                            valueExtractor = new RowCellExtractor(5, CellType.NUMERIC);
                            break;
                        case "Median":
                            valueExtractor = new RowCellExtractor(3, CellType.NUMERIC);
                            break;
                        default:
                            throw new Error("Unknown metric name: " + metricName);
                    }
                    extractors.add(new TimedValueExtractor(getProvider(), subjectType, subjectLabelExtractor, attributeLabelExtractor, timestampExtractor, valueExtractor));
                }

                Sheet sheet = workbook.getSheet(sheetName);
                excelUtils.extractAndSaveTimedValues(sheet,this,extractors);
            }
        }
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceId) {
        List<Attribute> attributes = new ArrayList<>();

        for (AttributePrefix attributePrefix : AttributePrefix.values()){
            // attributePrefix is a prefix of an attribute (one to one mapping with xls file)
            for (String sheetName: sheetNames){
                // sheetName is a name of a data sheet
                for (String metricName : metricNames){
                    // metricName is a name of a metric
                    attributes.add(new Attribute(
                            getProvider(),
                            getAttributeLabel(attributePrefix, sheetName, metricName), // Id
                            attributeNames[attributePrefix.ordinal()]
                    ));
                }
            }
        }
        return attributes;
    }

    private String getAttributeLabel(AttributePrefix attributePrefix, String sheetName, String metricName){
        return attributePrefix.name() + sheetName.replaceAll(" ","-") + metricName;
    }
}
