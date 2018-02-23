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
import java.util.*;

/**
 *
 * Headline estimates of personal well-being from the Annual Population Survey (APS)local and
 * Average Means for each of the 4 measurements.
 * NOTE: The scores of the variable Anxiety needs to be inverted to make it comparable with the other variables (because higher anxiety is negative).
 *
 * Example of how to use in recipe:
 *
 *  {
 * "importerClass": "uk.org.tombolo.importer.ons.ONSWellbeingImporter",
 * "datasourceId": "ONSWellbeing"
 *  }
 *
 * {
 *  "fieldClass": "uk.org.tombolo.field.value.LatestValueField",
 *  "label": "life_satisfaction",
 *  "attribute": {
 *   "provider": "uk.gov.ons",
 *   "label": "life_satisfaction"
 *   }
 * }
 */
public class ONSWellbeingImporter extends AbstractONSImporter {

    private static final Logger log = LoggerFactory.getLogger(ONSWellbeingImporter.class);

    private enum DatasourceId {
        ONSWellbeing(new DatasourceSpec(
                ONSWellbeingImporter.class,
                "ONSWellbeing",
                "Headline estimates of personal well-being at Local Authority level",
                "Self-perception of Life satisfaction, Self-perception of Anxiety, Self-perception of Happiness, Self-perception of Worthiness",
                "https://www.ons.gov.uk/file?uri=/peoplepopulationandcommunity/wellbeing/datasets/headlineestimatesofpersonalwellbeing/april2016tomarch2017localauthorityupdate/headlinewellbeinglocalauthorityupdate2016to2017.xls")
        );

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }
    public ONSWellbeingImporter(){
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

        String fileLocation = DatasourceId.ONSWellbeing.datasourceSpec.getUrl();
        InputStream isr = downloadUtils.fetchInputStream(new URL(fileLocation), getProvider().getLabel(), ".xls");

        // This dataset contains both subject types
        SubjectType localauthority = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(AbstractONSImporter.PROVIDER.getLabel(), getOaDatasourceIds().get(0));
        SubjectType englandboundaries = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(AbstractONSImporter.PROVIDER.getLabel(), getOaDatasourceIds().get(1));

        List<TimedValue> timedValues = new ArrayList<TimedValue>();

        HSSFWorkbook workbook = new HSSFWorkbook(isr);
        int sheetIndex = 0;

        // Looping through the excell sheets
        for (int sheet = 1; sheet <= 8; sheet = sheet+2){
            Sheet datatypeSheet = workbook.getSheetAt(sheet);

            // Creating the row iterator object
            Iterator<Row> rowIterator = datatypeSheet.rowIterator();
            // Skipping unrelevant rows
            int ignore = 0;
            while (ignore++ < 7) {
                rowIterator.next();
            }

            // Looping through rows
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String geograghy =  String.valueOf(row.getCell(0)).trim();
                Subject subject = SubjectUtils.getSubjectByTypeAndLabel(localauthority, geograghy);
                subject = subject != null ? subject : SubjectUtils.getSubjectByTypeAndLabel(englandboundaries, geograghy);

                // Checking if subject is null
                if (subject != null) {

                    // Looping through the time values
                    for (int timeValuesIndex=2; timeValuesIndex <= 7; timeValuesIndex++ ) {
                        // This is the row number that contains our time values (years) in the dataset
                        Row rowTime = datatypeSheet.getRow(5);
                        String year = rowTime.getCell(timeValuesIndex).toString();
                        year = year.substring(0, year.length() - 3);

                        LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);

                        try {
                            Double record = row.getCell(timeValuesIndex).getNumericCellValue();
                            // Here is where we are assigning the values of our .xls file to the attribute fields we
                            // created.
                            Attribute attribute = datasource.getTimedValueAttributes().get(sheetIndex);
                            timedValues.add(new TimedValue(
                                    subject,
                                    attribute,
                                    timestamp,
                                    record));
                        } catch (IllegalStateException e) {
                            log.warn("Value for subject " + subject.getLabel() + " not found. " +
                                    "Defaulting to 0.0. Consider using a BackoffField or ConstantField.");
                            continue;
                        }
                    }
                }
            }
            sheetIndex++;
        }
        saveAndClearTimedValueBuffer(timedValues);
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceID) {
        // Creating a placeholder for our attributes
        List<Attribute> attributes = new ArrayList<>();

        // Dataset specific: we hardcode the columns names for the our .csv file
        String[] elements = { "life_satisfaction", "worthwhile", "happiness", "anxiety"};
        String[] description = { "Self-perception of Life satisfaction", "Self-perception of Worthiness", "Self-perception of Happiness", "Self-perception of Anxiety"};

        // We loop through the elements of the elements object and adding an Attribute object in the list
        // with nour column names.
        for( int i = 0; i < elements.length; i++) {
            attributes.add(new Attribute(getProvider(), elements[i], elements[i]));

        }
        return attributes;
    }
}
