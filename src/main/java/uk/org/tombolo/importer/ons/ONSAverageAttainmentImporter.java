package uk.org.tombolo.importer.ons;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.AbstractOaImporter;
import uk.org.tombolo.importer.Config;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Importer for Average Attainment score 8 per pupil by local authority created as part of the Barking and Dahenham manifesto.
 * The data are relevant for state-funded schools only.
 * The data are fetched from ONS Department for Education.
 * Geography: Local authorities
 * Unit of measurement: Please consult https://www.gov.uk/education/school-performance-measures
 */
public class ONSAverageAttainmentImporter extends AbstractONSImporter {
    private static final String DATASOURCE = "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/652293/SFR57_2017_LA__tables.xlsx";
    private static final Logger log = LoggerFactory.getLogger(ONSAverageAttainmentImporter.class);

    public ONSAverageAttainmentImporter(Config config) {
        super(config);
        try {
            // Specifying the datasourceId. This will be used by the DC recipe
            datasourceIds = Arrays.asList(getDatasourceSpec("ONSAverageAttainment").getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        DatasourceSpec datasourceSpec = new DatasourceSpec(
                ONSAverageAttainmentImporter.class,
                "ONSAverageAttainment",
                "Average Attainment 8 score per pupil",
                "Average Attainment 8 scores by local authority and region",
                DATASOURCE);
        return datasourceSpec;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        SubjectType localauthority = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());
        List<TimedValue> timedValues = new ArrayList<TimedValue>();
        String fileLocation = getDatasourceSpec("ONSAverageAttainment").getUrl();
        URL url;
        try {
            url = new URL(fileLocation);
        } catch (MalformedURLException e) {
            File file;
            if (!(file = new File(fileLocation)).exists()) {
                log.error("ERROR: File does not exist: " + fileLocation);
            }
            url = file.toURI().toURL();
        }
        InputStream isr = downloadUtils.fetchInputStream(url, getProvider().getLabel(), ".xlsx");

        XSSFWorkbook workbook = new XSSFWorkbook(isr);
        int sheet = 4;
        Sheet datatypeSheet = workbook.getSheetAt(sheet);

        Iterator<Row> rowIterator = datatypeSheet.rowIterator();

        // Skipping unrelevant rows
        rowIterator.next();
        rowIterator.next();
        rowIterator.next();
        rowIterator.next();
        rowIterator.next();
        rowIterator.next();

        while (rowIterator.hasNext()){
            Row row = rowIterator.next();

            Subject subject = SubjectUtils.getSubjectByTypeAndLabel(localauthority, String.valueOf(row.getCell(0)).trim());

            // Dataset specific: The dataset contains mixed geometries. Check that the geometries in the excel file
            // match the "Area code" column. If they are not null proceed

            if (subject!=null){
                // Looping through the time values
                for (int timeValuesIndex=3; timeValuesIndex <= 5; timeValuesIndex++ ) {

                    // This is the row number that contains our time values (years) in the dataset
                    Row rowTime = datatypeSheet.getRow(5);
                    String year = rowTime.getCell(timeValuesIndex).toString();

                    // The date is formated as first year appearing. Eg: 2014/15 is formatted as 2014
                    year = year.substring(0, 4);
                    LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);

                    try {

                        Double record = row.getCell(timeValuesIndex).getNumericCellValue();

                        // Here is where we are assigning the values of our .csv file to the attribute fields we
                        // created.
                        Attribute attribute = datasource.getTimedValueAttributes().get(0);
                        timedValues.add(new TimedValue(
                                subject,
                                attribute,
                                timestamp,
                                record));

                    } catch (IllegalStateException e) {
                        log.warn("Missing value fo subject:" + subject.getLabel().toString() + ". Defaulting to zero. Consider using BackoffField or ConstantField");
                        continue;
                    }

                }
            }
        }
        saveAndClearTimedValueBuffer(timedValues);
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceID) {

        // Creating a placeholder for our attributes
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(getProvider(), "average_attainment", "average_attainment"));

        return attributes;
    }


}