package uk.org.tombolo.importer.manifestohealth;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Config;
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

import static com.google.common.base.Strings.emptyToNull;

/**
 * Created by tbantis on 15/12/2017.
 */
public class ONSWellbeingImporter extends AbstractImporter {

    // Instantiating the link to our .xls file
    private static final String DATASOURCE = "https://www.ons.gov.uk/file?uri=/peoplepopulationandcommunity/wellbeing/datasets/headlineestimatesofpersonalwellbeing/april2016tomarch2017localauthorityupdate/headlinewellbeinglocalauthorityupdate2016to2017.xls";


    public ONSWellbeingImporter(Config config) {
        super(config);
        try {
            // Specifying the datasourceId. This will be used by the DC recipe
            datasourceIds = Arrays.asList(getDatasourceSpec("ONSWellbeing").getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Instantiating the data Provider
    protected static final Provider PROVIDER = new Provider(
            "uk.gov.ons",
            "Wellbeing Survey"
    );

    @Override
    public Provider getProvider() {
        return PROVIDER;
    }

    // Instantiating the datasoure specifications.
    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        DatasourceSpec datasourceSpec = new DatasourceSpec(
                NHSAdmissionsImporter.class,
                "ONSWellbeing",
                "ONS Wellbeing",
                "Life Satisfaction: by counties, local and unitary authorities; 2011/12  – 2016/17",
                DATASOURCE);
        return datasourceSpec;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        SubjectType localauthority = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());

        List<TimedValue> timedValues = new ArrayList<TimedValue>();

        String fileLocation = getDatasourceSpec("ONSWellbeing").getUrl();


        // The code below fetches the .xls file from the URL we specified in our DatasourceSpec object
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

        // Fetching and reading the file using fetchInputStream
        InputStream isr = downloadUtils.fetchInputStream(url, getProvider().getLabel(), ".xls");

        HSSFWorkbook workbook = new HSSFWorkbook(isr);
        DataFormatter dataFormatter = new DataFormatter();

        int sheetIndex = 0;

        // This is the excel sheet that contains our data
        for (int sheet = 1; sheet <= 8; sheet = sheet+2){
            Sheet datatypeSheet = workbook.getSheetAt(sheet);

            // Creating the row iterator object
            Iterator<Row> rowIterator = datatypeSheet.rowIterator();

            // Skipping unrelevant rows
            rowIterator.next();
            rowIterator.next();
            rowIterator.next();
            rowIterator.next();
            rowIterator.next();
            rowIterator.next();
            rowIterator.next();
            rowIterator.next();

            // Looping through rows
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                Subject subject = SubjectUtils.getSubjectByTypeAndLabel(localauthority, String.valueOf(row.getCell(0)).trim());

                // Checking if subject is null
                if (subject != null) {

                // Looping through the time values
                    for (int timeValuesIndex=2; timeValuesIndex <= 5; timeValuesIndex++ ) {

                        // This is the row number that contains our time values (years) in the dataset
                        Row rowTime = datatypeSheet.getRow(5);
                        String year = rowTime.getCell(timeValuesIndex).toString();
                        year = year.substring(0, year.length() - 3);

                        LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);

                        try {
                            Double record = row.getCell(timeValuesIndex).getNumericCellValue();
                            // Here is where we are assigning the values of our .csv file to the attribute fields we
                            // created.
                            Attribute attribute = datasource.getTimedValueAttributes().get(sheetIndex);
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
            sheetIndex++;
        }
        saveAndClearTimedValueBuffer(timedValues);
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceID) {
        // Creating a placeholder for our attributes
        List<Attribute> attributes = new ArrayList<>();

        // Dataset specific: we hardcode the columns names for the our .csv file
        String[] elements = { "life_satisfaction", "worthwhile", "happy", "anxiety"};

        // We loop through the elements of the elements object and adding an Attribute object in the list
        // with nour column names.
        for( int i = 0; i < elements.length; i++) {
            attributes.add(new Attribute(getProvider(), elements[i], elements[i]));

        }
        return attributes;
    }

}
