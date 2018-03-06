package uk.org.tombolo.importer.ons;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Survival rates of new businesses from ONS Business demography
 * Importer imports absolute numbers and percentages as fractions.
 * For more information please visit https://www.ons.gov.uk/businessindustryandtrade/business/activitysizeandlocation/bulletins/businessdemography/2015/relateddata
 *
 * How to use in a recipe:
 *
 * Datasource:
 * {
 *    "importerClass": "uk.org.tombolo.importer.ons.ONSBusinessDemographyImporter",
 *    "datasourceId": "ONSNewBusinessSurvival"
 * }
 *
 * Field:
 * {
 *     "fieldClass": "uk.org.tombolo.field.value.LatestValueField",
 *     "label": "survival_5_year_total",
 *     "attribute": {
 *         "provider": "uk.gov.ons",
 *         "label": "survival_5_year_total"
 *   }
 *  }
 */
public class ONSBusinessDemographyImporter extends AbstractONSImporter{
    private static Logger log = LoggerFactory.getLogger(ONSBusinessDemographyImporter.class);

    private enum DatasourceId {
        ONSNewBusinessSurvival(new DatasourceSpec(
                ONSBusinessDemographyImporter.class,
                "ONSNewBusinessSurvival",
                "Survival of newly born businesses",
                "Business Demography - Enterprise survivals per Local Authority. A business is deemed to have survived if having been a birth in year "+
                        "t or having survived to year t; it is active in terms of employment and/or turnover "+
                        "in any part of t+1. " +
                        "For more info visit https://www.ons.gov.uk/businessindustryandtrade/business/activitysizeandlocation/datasets/businessdemographyreferencetable",
                "https://www.ons.gov.uk/file?uri=/businessindustryandtrade/business/activitysizeandlocation/datasets/businessdemographyreferencetable/current/businessdemographyexceltables2016v2.xls")
        )
        ;

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }



    public ONSBusinessDemographyImporter(){
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        return DatasourceId.valueOf(datasourceId).datasourceSpec;
    }

    @Override
    protected List<String> getOaDatasourceIds() {
        return Arrays.asList(OaImporter.OaType.localAuthority.datasourceSpec.getId(),
                OaImporter.OaType.englandBoundaries.datasourceSpec.getId());
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        String fileLocation = DatasourceId.ONSNewBusinessSurvival.datasourceSpec.getUrl();
        InputStream isr = downloadUtils.fetchInputStream(new URL(fileLocation), getProvider().getLabel(), ".xls");

        // This dataset contains both subject types
        SubjectType localauthority = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(AbstractONSImporter.PROVIDER.getLabel(), getOaDatasourceIds().get(0));
        SubjectType englandboundaries = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(AbstractONSImporter.PROVIDER.getLabel(), getOaDatasourceIds().get(1));

        List<TimedValue> timedValues = new ArrayList<TimedValue>();
        HSSFWorkbook workbook = new HSSFWorkbook(isr);

        // Looping through the excell sheets
        for (int sheet = 13; sheet <= 17; sheet++){
            Sheet datatypeSheet = workbook.getSheetAt(sheet);
            Iterator<Row> rowIterator = datatypeSheet.rowIterator();
            int ignore = 0;
            while (ignore++ < 6) {
                rowIterator.next();
            }
            // Hardcoded year of survey
            String year = null;
            switch (sheet){
                case 13:
                    year = "2011";
                    break;
                case 14:
                    year = "2012";
                    break;
                case 15:
                    year = "2013";
                    break;
                case 16:
                    year = "2014";
                    break;
                case 17:
                    year = "2015";
                    break;
            }
            Row rowAttribute = datatypeSheet.getRow(6);
            // Looping through rows
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String geography =  String.valueOf(row.getCell(0)).trim();
                Subject subject = SubjectUtils.getSubjectByTypeAndLabel(localauthority, geography);
                subject = subject != null ? subject : SubjectUtils.getSubjectByTypeAndLabel(englandboundaries, geography);
                // Checking if subject is null
                if (subject != null) {
                    // loop through attribute columns
                    for (int i=3; i<=12;i++){
                        String attributeName = String.valueOf(rowAttribute.getCell(i)).trim();
                        LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);
                        try {
                            Double record;
                            if (attributeName.contains("per cent")) {
                                record = row.getCell(i).getNumericCellValue() / 100;
                                log.info("Value for " + subject.getLabel()+". Appears as: "+ row.getCell(i).getNumericCellValue()+
                                " Saving as: "+record);
                            } else {
                                record = row.getCell(i).getNumericCellValue();
                            }
                            row.getCell(i).getNumericCellValue();
                            Attribute attribute = AttributeId.getAttributeIdByEqual(attributeName).attribute;
                            timedValues.add(new TimedValue(
                                    subject,
                                    attribute,
                                    timestamp,
                                    record));
                        } catch (IllegalStateException | NullPointerException e) {
                            log.warn("Invalid value for subject " + subject.getLabel()+". Skipping");
                            continue;
                        }
                    }
                }
            }
        }
        saveAndClearTimedValueBuffer(timedValues);
        workbook.close();
    }

    private enum AttributeId {
        survival_5_year_total("survival_5_year_total","5-year survival"),
        survival_5_year_fraction("survival_5_year_fraction", "5-year      per cent"),
        survival_4_year_total("survival_4_year_total", "4-year survival"),
        survival_4_year_fraction("survival_4_year_fraction", "4-year      per cent"),
        survival_3_year_total("survival_3_year_total","3-year survival"),
        survival_3_year_fraction("survival_3_year_fraction","3-year      per cent"),
        survival_2_year_total("survival_2_year_total","2-year survival"),
        survival_2_year_fraction("survival_2_year_fraction", "2-year      per cent"),
        survival_1_year_total("survival_1_year_total", "1-year survival"),
        survival_1_year_fraction("survival_1_year_fraction","1-year      per cent")
        ;

        // Name and description of the attribute
        String name;
        String description;
        Attribute attribute;

        AttributeId(String name, String description) {
            this.name = name;
            this.description = description;
            attribute = new Attribute(ONSBusinessDemographyImporter.PROVIDER, name(), description);
        }

        public static AttributeId getAttributeIdByEqual(String description) {
            return Arrays.stream(AttributeId.values()).filter(element -> description.equals(element.description))
                    .findFirst().get();
        }
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceId) {
        // Creating a placeholder for our attributes
        List<Attribute> attributes = new ArrayList<>();
        for (AttributeId id : AttributeId.values()) {
            attributes.add(id.attribute);
        }
        return attributes;
    }
}
