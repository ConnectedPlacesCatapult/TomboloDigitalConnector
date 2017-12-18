package uk.org.tombolo.importer.manifestohealth;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import org.apache.commons.csv.CSVRecord;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.ons.AbstractONSImporter;
import uk.org.tombolo.importer.ons.OaImporter;

import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by tbantis on 13/12/2017.
 */
public class ObeseImporter extends AbstractImporter {

    // Instantiating the list that will hold our .csv rows
    private List csvRecords;

    // Instantiating the link to our .csv file
    private static final String DATASOURCE = "http://activepeople.sportengland.org/Result/ExportTable?Id=134820&TabDimension=2&RowDimension=1&ColDimension=4&SelectedTabs[0]=10&ValueMode=0";

    // Instantiating the AbstractImporter constructor
    public ObeseImporter(Config config) {
        super(config);
        try {
            // Specifying the datasourceId. This will be used by the DC recipe
            datasourceIds = Arrays.asList(getDatasourceSpec("BMIObese").getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Instantiating the data Provider
    protected static final Provider PROVIDER = new Provider(
            "activepeople.sportengland.org",
            "Sports England"
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
                ObeseImporter.class,
                "BMIObese",
                "BMI Obese",
                "% of people with BMI 30-39.9 kg/m^2",
                DATASOURCE);
        return datasourceSpec;
    }

    // The method that does the data  fetching, cleaning, reformatting and importing
    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {

        // We create SubjectType object that we will use to get the appropriate geometries
        // from OaImporter class
        SubjectType localauthority = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());

        // We create an empty list that will keep our .csv values
        List<FixedValue> fixedValues = new ArrayList<FixedValue>();

        CSVFormat format = CSVFormat.DEFAULT;
        String fileLocation = getDatasourceSpec("BMIObese").getUrl();

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

        // Looping through the rows of the .csv file
        while (rowIterator.hasNext()){
            CSVRecord row = rowIterator.next();

            // Fetching the subject geometry from OaImporter to save it in getFixedValueAttributes. Note that this
            // corresponds to the 3rd element of our row: row.get(2).
            try{
                Subject subject = SubjectUtils.getSubjectByTypeAndName(localauthority, String.valueOf(row.get(2)).trim());

                // Checking not matched geometries
                if (subject!=null){
                    // Dataset specific: attributeIndex is the column index that we are interested in.
                    int attributeIndex = 3;

                    // The value is a string in our .csv file. We need to clean it before using it.
                    // We  need to check for invalid rows so we will suround this with a try catch clause
                    try {
                        String record = row.get(attributeIndex).replace("%","");

                        // We discard the rows that contain no values. In the .csv these are depicted as '*'
                        if (!Objects.equals(record, "*")){
                            System.out.println(record);
                            // Here is where we are assigning the values of our .csv file to the attribute fields we
                            // created.
                            for (Attribute attribute : datasource.getFixedValueAttributes()) {
                                fixedValues.add(new FixedValue(
                                        subject,
                                        attribute,
                                        record));

                                // We increment to get the rest of the values in the row
                                attributeIndex++;

                            }
                        }
                    // Catching invalid rows
                    } catch (ArrayIndexOutOfBoundsException npe) {
                        System.out.println("INFO - Found invalid row: Skipping");

                    }
                }
                // Catching invalid geometries
                else {
                    System.out.println("INFO - Geometry not found for "+ row.get(2) + ": Skipping");
                    continue;
                }
            }
            // We need to check for again for invalid rows when looping through the local authorities names
            catch (ArrayIndexOutOfBoundsException npe){
                System.out.println("INFO - Found invalid local authority row: Skipping");
                continue;
            }

        }

        // Finally we save the values in the database
        FixedValueUtils.save(fixedValues);
        fixedValues.clear();

    }

    @Override
    public List<Attribute> getFixedValueAttributes(String datasourceID) {
        // Creating a placeholder for our attributes
        List<Attribute> attributes = new ArrayList<>();

        // Dataset specific: we hardcode the columns names for the our .csv file
        String[] elements = { "BMI_obesity_2013", "BMI_obesity_2014", "BMI_obesity_2015"};

        // We loop through the elements of the elements object and adding an Attribute object in the list
        // with nour column names.
        for( int i = 0; i < elements.length; i++) {
            attributes.add(new Attribute(getProvider(), elements[i], elements[i]));

        }
        return attributes;
    }

}
