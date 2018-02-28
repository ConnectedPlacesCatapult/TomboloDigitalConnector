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
 *
 * Life expectancy (LE), healthy life expectancy (HLE) and disability-free life expectancy (DFLE) at birth and age 65 by sex, UK, 2014 to 2016
 * Link to dataset: https://www.ons.gov.uk/file?uri=/peoplepopulationandcommunity/healthandsocialcare/healthandlifeexpectancies/datasets/healthstatelifeexpectancyatbirthandatage65bylocalareasuk/current/refhealthstatelifeexpectancies1.xls
 * Example of how to use in recipe:
 *
 *  {
 * "importerClass": "uk.org.tombolo.importer.ons.ONSLifeExpectancyImporter",
 * "datasourceId": "ONSLifeExpectancy"
 *  }
 *
 * {
 *  "fieldClass": "uk.org.tombolo.field.value.LatestValueField",
 *  "label": "LEmaleAtBirth",
 *  "attribute": {
 *   "provider": "uk.gov.ons",
 *   "label": "LEmaleAtBirth"
 *   }
 * }
 */
public class ONSLifeExpectancyImporter extends AbstractONSImporter  {
    private static final Logger log = LoggerFactory.getLogger(ONSLifeExpectancyImporter.class);

    private enum DatasourceId {
        ONSLifeExpectancy(new DatasourceSpec(
                ONSLifeExpectancyImporter.class,
                "ONSLifeExpectancy",
                "Life Expectancy",
                "Life expectancy (LE), healthy life expectancy (HLE) and disability-free life expectancy (DFLE) at birth and age 65 by sex, UK, 2014 to 2016 for local areas in the UK. Figures are in years",
                "https://www.ons.gov.uk/file?uri=/peoplepopulationandcommunity/healthandsocialcare/healthandlifeexpectancies/datasets/healthstatelifeexpectancyatbirthandatage65bylocalareasuk/current/refhealthstatelifeexpectancies1.xls")
        );

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }
    public ONSLifeExpectancyImporter(){
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        return DatasourceId.valueOf(datasourceId).datasourceSpec;
    }

    @Override
    protected List<String> getOaDatasourceIds() {
        return Arrays.asList(
                OaImporter.OaType.localAuthority.datasourceSpec.getId(),
                OaImporter.OaType.englandBoundaries.datasourceSpec.getId());
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        // Get the url and pass it to a InputStream for downloading the file
        String fileLocation = DatasourceId.ONSLifeExpectancy.datasourceSpec.getUrl();
        InputStream isr = downloadUtils.fetchInputStream(new URL(fileLocation), getProvider().getLabel(), ".xls");

        // This dataset contains both subject types
        SubjectType localauthority = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(AbstractONSImporter.PROVIDER.getLabel(), getOaDatasourceIds().get(0));
        SubjectType englandboundaries = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(AbstractONSImporter.PROVIDER.getLabel(), getOaDatasourceIds().get(1));

        // Initialise the array that will store the dataset values
        List<TimedValue> timedValues = new ArrayList<TimedValue>();

        // Initialise the workbook that will be used to interact with the excel dataset
        HSSFWorkbook workbook = new HSSFWorkbook(isr);
        int sheetIndex = 0;

        // Getting the indices of the columns containing the data.
        List<Integer> columnLoop = new ArrayList<Integer>(Arrays.asList(4,8,13));

        // Looping through the excel sheets: HE - male at bith, HE - female at birth, HE - male at 65, HE - females at 65
        // We skip the first (Contents)
        for (int sheet = 1; sheet <= 4; sheet++) {
            Sheet datatypeSheet = workbook.getSheetAt(sheet);

            // Creating the row iterator object
            Iterator<Row> rowIterator = datatypeSheet.rowIterator();

            // Skipping unrelevant rows
            int ignore = 0;
            while (ignore++ < 4) {
                rowIterator.next();
            }

            Row rowTime = datatypeSheet.getRow(0);
            String year = rowTime.getCell(0).getStringCellValue();
            LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year.substring(year.length() - 4));
            log.info("Time is presented in the dataset as {} and we persist it as {}", year, timestamp);

            Row rowAttribute = datatypeSheet.getRow(3);


            // Looping through rows
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // The Area Codes is the first column in the excel file
                String geograghy = String.valueOf(row.getCell(0)).trim();

                // Fetch the geometry for our subjects based on the geography code.
                Subject subject = SubjectUtils.getSubjectByTypeAndLabel(localauthority, geograghy);
                subject = subject != null ? subject : SubjectUtils.getSubjectByTypeAndLabel(englandboundaries, geograghy);

                // Checking if subject is null
                if (subject != null) {

                    // Looping through the columns attributes
                    for (Integer item : columnLoop) {
                        try {
                            // We are distinguishing our attributes using a combination of column name and sheet name
                            String attributeName = rowAttribute.getCell(item).toString() + " " + datatypeSheet.getSheetName();
                            // Here is where we are assigning the values of our .xls file to the attribute fields we created.
                            Attribute attribute = AttributeId.getAttributeIdByEqual(attributeName).attribute;
                            Double record = row.getCell(item).getNumericCellValue();
                            timedValues.add(new TimedValue(subject, attribute, timestamp, record));
                        } catch (java.lang.IllegalStateException ne) {
                            continue;
                        }
                    }
                }
            }
        }
        // Finally we save the values in the database
        saveAndClearTimedValueBuffer(timedValues);
        workbook.close();
    }

    public enum AttributeId {
        LEmaleAtBirth("LE HE - male at birth", "Life Expectancy Health Expectancy - male at birth"),
        HLEmaleAtBirth("HLE HE - male at birth", "Health Life Expectancy Health Expectancy - male at birth"),
        DfLEmaleAtBirth("DfLE HE - male at birth", "Disability-free Life Expectancy - male at birth"),
        LEfemaleAtBirth("LE HE - female at birth", "Life Expectancy Health Expectancy - female at birth"),
        HLEfemaleAtBirth("HLE HE - female at birth", "Health Life Expectancy Health Expectancy - female at birth"),
        DfLEfemaleAtBirth("DfLE HE - female at birth", "Disability-free Life Expectancy - female at birth"),
        LEmaleAt65("LE HE - male at 65", "Life Expectancy Health Expectancy - male at 65"),
        HLEmaleAt65("HLE HE - male at 65", "Health Life Expectancy Health Expectancy - male at 65"),
        DfLEmaleAt65("DfLE HE - male at 65", "Disability-free Life Expectancy - male at 65"),
        LEfemaleAt65("LE HE - females at 65", "Life Expectancy Health Expectancy - females at 65"),
        HLEfemaleAt65("HLE HE - females at 65", "Health Life Expectancy Health Expectancy - females at 65"),
        DfLEfemaleAt65("DfLE HE - females at 65", "Disability-free Life Expectancy - females at 65")
        ;

        // Name and escription of the attribute
        String name;
        String description;
        Attribute attribute;

        AttributeId(String name, String description) {
            this.name = name;
            this.description = description;
            attribute = new Attribute(ONSLifeExpectancyImporter.PROVIDER, name(), description);
        }

        public static AttributeId getAttributeIdByEqual(String name) {
            return Arrays.stream(AttributeId.values()).filter(element -> name.equals(element.name))
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
