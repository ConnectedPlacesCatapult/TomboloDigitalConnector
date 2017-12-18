package uk.org.tombolo.importer.manifestohealth;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.ons.AbstractONSImporter;
import uk.org.tombolo.importer.ons.OaImporter;

import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tbantis on 18/12/2017.
 */
public class MVPAImporter extends AbstractImporter {

    // Instantiating the list that will hold our .csv rows
    private List csvRecords;

    // Instantiating the link to our  file
    private static final String DATASOURCE = "http://activepeople.sportengland.org/Result/ExportTable?Id=105798&TabDimension=2&RowDimension=1&ColDimension=4&SelectedTabs[0]=5&ValueMode=0";


    public MVPAImporter(Config config) {
        super(config);
        try {
            // Specifying the datasourceId. This will be used by the DC recipe
            datasourceIds = Arrays.asList(getDatasourceSpec("MVPAactive").getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Instantiating the data Provider
    protected static final Provider PROVIDER = new Provider(
            "activepeople.sportengland.org",
            "Sports England"
    );
    @Override
    public Provider getProvider() {
        return PROVIDER;
    }

    // Instantiating the datasoure specifications.
    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        DatasourceSpec datasourceSpec = new DatasourceSpec(
                MVPAImporter.class,
                "MVPAactive",
                "MVPA active",
                "Whole population (16+) 150-599 MVPA, 600+ MVPA",
                DATASOURCE);
        return datasourceSpec;
    }


    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        // We create SubjectType object that we will use to get the appropriate geometries
        // from OaImporter class
        SubjectType localauthority = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());

        // We create an empty list that will keep our .csv values
        List<TimedValue> timedValues = new ArrayList<TimedValue>();

        CSVFormat format = CSVFormat.DEFAULT;
        String fileLocation = getDatasourceSpec("MVPAactive").getUrl();


        // The code below fetches the .csv file from the URL we specified in our DatasourceSpec object
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

        InputStreamReader isr = new InputStreamReader(
                downloadUtils.fetchInputStream(url, getProvider().getLabel(), ".csv"));

        // Parsing our csv file
        CSVParser csvFileParser = new CSVParser(isr, format);
        csvRecords = csvFileParser.getRecords();


        // We discard the first 6 records in our data file as these don't hold any meaningfull information.
        // We do this  calling an iterator object and simply ignoring them:
        Iterator<CSVRecord> rowIterator = csvRecords.iterator();

        // skipping first 6 rows
        rowIterator.next();
        rowIterator.next();
        rowIterator.next();
        rowIterator.next();
        rowIterator.next();
        rowIterator.next();


        // Looping through rows
        while (rowIterator.hasNext()) {
            CSVRecord row = rowIterator.next();

            try {
                Subject subject = SubjectUtils.getSubjectByTypeAndName(localauthority, String.valueOf(row.get(2)).trim());

                if (subject != null) {

                    // Creating the time index
                    for (int timeValuesIndex = 3; timeValuesIndex <= 5; timeValuesIndex++) {

                        CSVRecord rowTime = (CSVRecord) csvRecords.get(5);
                        String year = rowTime.get(timeValuesIndex).toString();
                        year = year.substring(0, 4);
                        LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);

                        // The value is a string in our .csv file. We need to clean it before using it.
                        // We  need to check for invalid rows so we will suround this with a try catch clause
                        try {
                            String recordString = row.get(timeValuesIndex).replace("%", "");
                            try {

                                Double record = Double.parseDouble(recordString);

                                // Here is where we are assigning the values of our .csv file to the attribute fields we
                                // created.
                                Attribute attribute = datasource.getTimedValueAttributes().get(0);
                                timedValues.add(new TimedValue(
                                        subject,
                                        attribute,
                                        timestamp,
                                        record));
                            } catch (IllegalStateException | NumberFormatException e) {
                                // TODO fix the missing values so they appear something else rather than 0. The NumberFormatException is for catching the * character
                                continue;
                            }

                            // Catching invalid rows
                        } catch (ArrayIndexOutOfBoundsException npe) {
                            System.out.println("INFO - Found invalid row: Skipping");

                        }

                    }
                }
                // Catching invalid geometries
                else {
                    System.out.println("INFO - Geometry not found for " + row.get(2) + ": Skipping");
                    continue;
                }

            }
            // We need to check for again for invalid rows when looping through the local authorities names
            catch (ArrayIndexOutOfBoundsException npe) {
                System.out.println("INFO - Found invalid local authority row: Skipping");
                continue;
            }
        }
        saveAndClearTimedValueBuffer(timedValues);
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceID) {
        // Creating a placeholder for our attributes. Its a list in case we want to update it
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(getProvider(), "MVPA_active", "MVPA_active"));

        return attributes;
    }
}
