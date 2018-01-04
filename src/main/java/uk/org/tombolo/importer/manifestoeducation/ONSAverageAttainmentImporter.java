package uk.org.tombolo.importer.manifestoeducation;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.manifestohealth.NHSAdmissionsImporter;
import uk.org.tombolo.importer.ons.AbstractONSImporter;
import uk.org.tombolo.importer.ons.OaImporter;

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
 * Created by tbantis on 03/01/2018.
 */
public class ONSAverageAttainmentImporter extends AbstractImporter {
    private static final String DATASOURCE = "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/652293/SFR57_2017_LA__tables.xlsx";

    public ONSAverageAttainmentImporter(Config config) {
        super(config);
        try {
            // Specifying the datasourceId. This will be used by the DC recipe
            datasourceIds = Arrays.asList(getDatasourceSpec("ONSAverageAttainment").getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Instantiating the data Provider
    protected static final Provider PROVIDER = new Provider(
            "uk.gov.ons",
            "Average Attainment 8 score per pupil"
    );

    @Override
    public Provider getProvider() {
        return PROVIDER;
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        DatasourceSpec datasourceSpec = new DatasourceSpec(
                NHSAdmissionsImporter.class,
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
                System.out.println("ERROR: File does not exist: " + fileLocation);
            }
            url = file.toURI().toURL();
        }
        InputStream isr = downloadUtils.fetchInputStream(url, getProvider().getLabel(), ".xlsx");

        XSSFWorkbook workbook = new XSSFWorkbook(isr);
        DataFormatter dataFormatter = new DataFormatter();

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
            // fetching the subject geometry from OaImporter to save it in getFixedValueAttributes
            Subject subject = SubjectUtils.getSubjectByTypeAndLabel(localauthority, String.valueOf(row.getCell(0)).trim());

            // Dataset specific: The dataset contains mixed geometries. Check that the geometries in the excel file
            // match the "Area code" column. If they are not null proceed
            if (subject!=null){
                // Looping through the time values
                for (int timeValuesIndex=3; timeValuesIndex <= 5; timeValuesIndex++ ) {
                    // This is the row number that contains our time values (years) in the dataset

                    Row rowTime = datatypeSheet.getRow(5);
                    String year = rowTime.getCell(timeValuesIndex).toString();

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
                        // TODO fix the missing values so they appear something else rather than 0
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
