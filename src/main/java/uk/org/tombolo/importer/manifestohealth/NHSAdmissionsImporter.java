package uk.org.tombolo.importer.manifestohealth;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.FixedValueUtils;
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

/**
 * Created by tbantis on 15/12/2017.
 */
public class NHSAdmissionsImporter extends AbstractImporter {

    // Instantiating the link to our .xls file
    private static final String DATASOURCE = "https://digital.nhs.uk/media/28729/Statistics-on-Obesity-Physical-Activity-and-Diet-England-2016-Tables/Any/obes-phys-acti-diet-eng-2016-tab";

    public NHSAdmissionsImporter(Config config) {
        super(config);
        try {
            // Specifying the datasourceId. This will be used by the DC recipe
            datasourceIds = Arrays.asList(getDatasourceSpec("NHSObese").getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Instantiating the data Provider
    protected static final Provider PROVIDER = new Provider(
            "digital.nhs.uk",
            "NHS Digital"
    );

    // Getting the data Provider
    @Override
    public Provider getProvider() {
        return PROVIDER;
    }

    // Instantiating the datasoure specifications.
    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        DatasourceSpec datasourceSpec = new DatasourceSpec(
                NHSAdmissionsImporter.class,
                "NHSObese",
                "NHS Obese",
                "Admission Episodes with a primary or secondary diagnosis of obesity, by Region, Local Authority of residence and gender",
                DATASOURCE);
        return datasourceSpec;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        SubjectType localauthority = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());
        List<TimedValue> timedValues = new ArrayList<TimedValue>();

        String fileLocation = getDatasourceSpec("NHSObese").getUrl();


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
        InputStream isr = downloadUtils.fetchInputStream(url, getProvider().getLabel(), ".xlsx");

        Workbook workbook = new XSSFWorkbook(isr);
        DataFormatter dataFormatter = new DataFormatter();

        // This is the excel sheet that contains our data
        int sheet = 7;

        Sheet datatypeSheet = workbook.getSheetAt(sheet);

        Iterator<Row> rowIterator = datatypeSheet.rowIterator();

        // Dataset specific: this is to skip the first two lines that don't have any values of interest
        rowIterator.next();
        rowIterator.next();
        rowIterator.next();
        rowIterator.next();
        rowIterator.next();
        rowIterator.next();
        rowIterator.next();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            // We create SubjectType object that we will use to get the appropriate geometries
            // from OaImporter class
            Subject subject = SubjectUtils.getSubjectByTypeAndLabel(localauthority, String.valueOf(row.getCell(0)).trim());

            // Checking if subject is null
            if (subject != null) {

                // Getting the fourth column
                int attributeIndex = 4;

                String record = dataFormatter.formatCellValue(row.getCell(attributeIndex));

                String year = "2017";
                LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);
                // Here is where we are assigning the values of our .csv file to the attribute fields we
                // created.
                for (Attribute attribute : datasource.getTimedValueAttributes()) {
                    timedValues.add(new TimedValue(
                            subject,
                            attribute,
                            timestamp,
                            Double.parseDouble(record.replace(",",""))));

                    attributeIndex++;


                }
            }
        }
        saveAndClearTimedValueBuffer(timedValues);
    }


    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceID) {
        // Creating a placeholder for our attributes
        List<Attribute> attributes = new ArrayList<>();

        // Dataset specific: we hardcode the columns names for the our .csv file
        String[] elements = { "admissions_all", "admissions_male", "admissions_female"};

        // We loop through the elements of the elements object and adding an Attribute object in the list
        // with nour column names.
        for( int i = 0; i < elements.length; i++) {
            attributes.add(new Attribute(getProvider(), elements[i], elements[i]));

        }
        return attributes;
    }
}
