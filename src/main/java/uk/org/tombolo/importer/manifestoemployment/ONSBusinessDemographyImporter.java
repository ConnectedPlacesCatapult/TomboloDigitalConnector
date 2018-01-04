package uk.org.tombolo.importer.manifestoemployment;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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
 * Created by tbantis on 02/01/2018.
 */
public class ONSBusinessDemographyImporter extends AbstractImporter {

    private static final String DATASOURCE = "https://www.ons.gov.uk/file?uri=/businessindustryandtrade/business/activitysizeandlocation/datasets/businessdemographyreferencetable/current/businessdemographyexceltables2016v2.xls";


    public ONSBusinessDemographyImporter(Config config) {
        super(config);
        try {
            // Specifying the datasourceId. This will be used by the DC recipe
            datasourceIds = Arrays.asList(getDatasourceSpec("ONSBusiness").getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Instantiating the data Provider
    protected static final Provider PROVIDER = new Provider(
            "uk.gov.ons",
            "ONS Business Demography"
    );
    @Override
    public Provider getProvider() {
        return PROVIDER;
    }

    // Instantiating the datasoure specifications.
    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        DatasourceSpec datasourceSpec = new DatasourceSpec(
                ONSBusinessDemographyImporter.class,
                "ONSBusiness",
                "ONS Business Demography",
                "SURVIVAL OF NEWLY BORN ENTERPRISES",
                DATASOURCE);
        return datasourceSpec;
    }


    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        // We create SubjectType object that we will use to get the appropriate geometries
        // from OaImporter class
        SubjectType localauthority = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());


        List<TimedValue> timedValues = new ArrayList<TimedValue>();
        String fileLocation = getDatasourceSpec("ONSBusiness").getUrl();

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

        int sheetIndex = 13;
        Sheet datatypeSheet = workbook.getSheetAt(sheetIndex);

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
                // Hardcoded year of survey
                String year = "2011";
                LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);

                try {
                    Double record = row.getCell(12).getNumericCellValue();

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
        saveAndClearTimedValueBuffer(timedValues);
    }
    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceID) {
        // Creating a placeholder for our attributes
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(getProvider(), "new_business_survival", "new_business_survival"));

        return attributes;
    }
}
