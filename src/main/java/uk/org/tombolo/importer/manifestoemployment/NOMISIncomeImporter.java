package uk.org.tombolo.importer.manifestoemployment;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tbantis on 21/12/2017.
 */
public class NOMISIncomeImporter extends AbstractImporter {
    private static final String DATASOURCE = "http://www.nomisweb.co.uk/api/v01/dataset/NM_30_1.data.csv?geography=1941962753...1941962958&date=latestMINUS1-latest&sex=8&item=2&pay=7&measures=20100,20701&select=date_name,geography_name,geography_code,sex_name,pay_name,item_name,measures_name,obs_value,obs_status_name";
    private List csvRecords;

    public NOMISIncomeImporter(Config config) {
        super(config);
        try {
            // Specifying the datasourceId. This will be used by the DC recipe
            datasourceIds = Arrays.asList(getDatasourceSpec("NOMISIncome").getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Instantiating the data Provider
    protected static final Provider PROVIDER = new Provider(
            "nomisweb.co.uk",
            "NOMIS Income"
    );

    @Override
    public Provider getProvider() {
        return PROVIDER;
    }

    // Instantiating the datasoure specifications.
    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        DatasourceSpec datasourceSpec = new DatasourceSpec(
                NOMISIncomeImporter.class,
                "NOMISIncome",
                "NOMIS Income",
                "annual survey of hours and earnings  - resident analysis",
                DATASOURCE);
        return datasourceSpec;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        // We create SubjectType object that we will use to get the appropriate geometries
        // from OaImporter class
        SubjectType localauthority = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());

        // We create an empty list that will keep our values
        List<TimedValue> timedValues = new ArrayList<TimedValue>();
        CSVFormat format = CSVFormat.DEFAULT;
        String fileLocation = getDatasourceSpec("NOMISIncome").getUrl();

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
        InputStreamReader isr = new InputStreamReader(
                downloadUtils.fetchInputStream(url, getProvider().getLabel(), ".csv"));

        // Parsing our csv file
        CSVParser csvFileParser = new CSVParser(isr, format);
        csvRecords = csvFileParser.getRecords();
        // This is the excel sheet that contains our data
        Iterator<CSVRecord> rowIterator = csvRecords.iterator();


        // Dataset specific: this is to skip the first two lines that don't have any values of interest
        rowIterator.next();

        while (rowIterator.hasNext()) {
            CSVRecord row = rowIterator.next();

                if (String.valueOf(row.get(6)).trim().equals("Value")){
                    try {
                        Subject subject = SubjectUtils.getSubjectByTypeAndLabel(localauthority, String.valueOf(row.get(2)).trim());
                        if (subject!=null){
                            String year = row.get(0).toString();
                            LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);
                            Double record = Double.parseDouble(row.get(7));
                            // Here is where we are assigning the values of our .csv file to the attribute fields we
                            // created.
                            Attribute attribute = datasource.getTimedValueAttributes().get(0);
                            timedValues.add(new TimedValue(
                                    subject,
                                    attribute,
                                    timestamp,
                                    record));
                        }  else {
                            System.out.println("INFO - Geometry not found for "+ row.get(2) + ": Skipping");
                            continue;
                        } 
                    } catch (IllegalStateException | NumberFormatException e){
                        continue;
                    }
                } else {
                    continue;
                }
        }
        saveAndClearTimedValueBuffer(timedValues);
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceID) {
        // Creating a placeholder for our attributes
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(getProvider(), "NOMIS_Income", "NOMIS_Income"));

        return attributes;
    }
}
