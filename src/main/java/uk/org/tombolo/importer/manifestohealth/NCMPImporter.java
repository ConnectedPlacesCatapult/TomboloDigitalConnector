package uk.org.tombolo.importer.manifestohealth;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tbantis on 11/12/2017.
 */
public class NCMPImporter extends AbstractImporter {

    private static final String DATASOURCE = "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/610203/NCMP_data_LA_and_England.xlsx";

    // A standard importer inherits the below classes from AbstractImporter

    public NCMPImporter(Config config) {
        super(config);
        try {
            // Specifying the datasourceId. This will be used by the DC recipe
            datasourceIds = Arrays.asList(getDatasourceSpec("childhoodObesity").getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static final Provider PROVIDER = new Provider(
            "uk.nhs.phe",
            "Public Health England"
    );

    // Instantiating the datasoure specifications.
    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        DatasourceSpec datasourceSpec = new DatasourceSpec(
                NHSAdmissionsImporter.class,
                "childhoodObesity",
                "Childhood Obesity",
                "Prevalence of excess weight among children in Year 6 (age 10-11 years)",
                DATASOURCE);
        return datasourceSpec;
    }



    // This is implemented once per dataset and we create a new object new Provider() with the details of our dataset
    @Override
    public Provider getProvider() {
        return PROVIDER;
    }


    // This is where the actual importing happens
    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        // We are planning to use the column "Area code" in the excel file as our geometry. This will be the subject. However, this just
        // contains a, local authority in this case, code and not any polygon information. We get that by calling
        // the below object that allows us to get the geometry from OaImporter. At this point we are only creating a reference
        // which we will use afterwards
        SubjectType localauthority = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());

        // We create an empty list that will keep our excell sheet values
        List<TimedValue> timedValues = new ArrayList<TimedValue>();

        String fileLocation = getDatasourceSpec("childhoodObesity").getUrl();

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

        XSSFWorkbook workbook = new XSSFWorkbook(isr);
        DataFormatter dataFormatter = new DataFormatter();

        int sheet = 3;

        Sheet datatypeSheet = workbook.getSheetAt(sheet);

        Iterator<Row> rowIterator = datatypeSheet.rowIterator();

        // Dataset specific: this is to skip the first two lines that don't have any values of interest
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
                // Dataset specific:  Looping through the time values
                for (int timeValuesIndex=2; timeValuesIndex <= 21; timeValuesIndex+=5) {
                    Row rowTime = datatypeSheet.getRow(1);
                    String year = rowTime.getCell(timeValuesIndex).toString();
                    year = year.substring(0, 4);
                    LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);
                    Double record = row.getCell(timeValuesIndex + 2).getNumericCellValue();

                    // Here is where we are assigning the values of our .xls file to the attribute fields we
                    // created.
                    Attribute attribute = datasource.getTimedValueAttributes().get(0);
                    timedValues.add(new TimedValue(
                            subject,
                            attribute,
                            timestamp,
                            record));
                    }
                }
        }
        saveAndClearTimedValueBuffer(timedValues);
    }




    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceID) {

        // Adding the attribute
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(getProvider(), "year6_excess_weight", "year6_excess_weight"));

        return attributes;
    }

}
