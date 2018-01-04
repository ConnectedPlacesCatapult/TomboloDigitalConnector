package uk.org.tombolo.importer.manifestoenvironment;

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
public class WasteWaterImporter extends AbstractImporter {
    private static final String DATASOURCE = "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/664608/LA_and_Regional_Spreadsheet_201617.xlsx";

    public WasteWaterImporter(Config config) {
        super(config);
        try {
            // Specifying the datasourceId. This will be used by the DC recipe
            datasourceIds = Arrays.asList(getDatasourceSpec("DefraWastewater").getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected static final Provider PROVIDER = new Provider(
            "uk.gov.defra",
            "DEFRA"
    );
    @Override
    public Provider getProvider() {
        return PROVIDER;
    }


    // Instantiating the datasoure specifications.
    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        DatasourceSpec datasourceSpec = new DatasourceSpec(
                WasteWaterImporter.class,
                "DefraWastewater",
                "Waste Statistics",
                "Local Authority Collected Waste Statistics - Local Authority data",
                DATASOURCE);
        return datasourceSpec;
    }



    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        SubjectType localauthority = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());
        List<TimedValue> timedValues = new ArrayList<TimedValue>();
        String fileLocation = getDatasourceSpec("wasteStatistics").getUrl();
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

        int sheet = 7;
        Sheet datatypeSheet = workbook.getSheetAt(sheet);

        Iterator<Row> rowIterator = datatypeSheet.rowIterator();

        // Dataset specific: this is to skip the first two lines that don't have any values of interest
        rowIterator.next();
        rowIterator.next();
        rowIterator.next();
        rowIterator.next();
        while (rowIterator.hasNext()){
            Row row = rowIterator.next();

            // fetching the subject geometry from OaImporter to save it in getFixedValueAttributes
            Subject subject = SubjectUtils.getSubjectByTypeAndLabel(localauthority, String.valueOf(row.getCell(2)).trim());

            // Dataset specific: The dataset contains mixed geometries. Check that the geometries in the excel file
            // match the "Area code" column. If they are not null proceed
            if (subject!=null){
                // Dataset specific:  Looping through the time values
                String year = row.getCell(0).toString();
                year = year.substring(0, 4);
                LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);
                Double record = row.getCell(6).getNumericCellValue();

                // Here is where we are assigning the values of our .xls file to the attribute fields we
                // created.
                Attribute attribute = datasource.getTimedValueAttributes().get(0);
                timedValues.add(new TimedValue(
                        subject,
                        attribute,
                        timestamp,
                        record));

                String stringRecord = row.getCell(7).toString();
                stringRecord = stringRecord.replace("%","");
                Double record2 = Double.parseDouble(stringRecord);
                // Here is where we are assigning the values of our .xls file to the attribute fields we
                // created.
                Attribute attribute2 = datasource.getTimedValueAttributes().get(1);
                timedValues.add(new TimedValue(
                        subject,
                        attribute2,
                        timestamp,
                        record2));

            }
        }
        saveAndClearTimedValueBuffer(timedValues);
    }
    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceID) {

        // Adding the attributes
        List<Attribute> attributes = new ArrayList<>();
        String[] elements = { "Residual household waste per household (kg/household)", "Percentage of household waste sent for reuse, recycling or composting"};
        for( int i = 0; i < elements.length; i++) {
            attributes.add(new Attribute(getProvider(), elements[i], elements[i]));

        }
        return attributes;
    }

}
