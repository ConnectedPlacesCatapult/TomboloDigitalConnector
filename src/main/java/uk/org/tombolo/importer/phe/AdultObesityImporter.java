package uk.org.tombolo.importer.phe;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.ons.AbstractONSImporter;
import uk.org.tombolo.importer.ons.OaImporter;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Importer for importing the adult obesity from the Sports England website.
 * data fetched from http://activepeople.sportengland.org
 */
public class AdultObesityImporter extends AbstractPheImporter {

    private static Logger log = LoggerFactory.getLogger(AdultObesityImporter.class);

    // Instantiating the link to our .csv file
    private static final String DATASOURCE = "http://activepeople.sportengland.org/Result/ExportTable?Id=104519&TabDimension=2&RowDimension=1&ColDimension=4&SelectedTabs[0]=7&ValueMode=0";

    private enum DatasourceId {
        adultObesity(new DatasourceSpec(
                AdultObesityImporter.class,
                "adultObesity",
                "Local Authority Adult Obesity",
                "Self reported adult obesity",
                DATASOURCE)
        );

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }

    public AdultObesityImporter(){
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceIdString) throws Exception {
        return DatasourceId.valueOf(datasourceIdString).datasourceSpec;

    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {

        // Instantiating the list that will hold our .csv rows
        List csvRecords;

        // We create SubjectType object that we will use to get the appropriate geometries
        // from OaImporter class
        SubjectType localauthority = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(AbstractONSImporter.PROVIDER.getLabel(),"localAuthority");
//        SubjectType localauthority = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
//                OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());

        // We create an empty list that will keep our .csv values
        List<TimedValue> timedValues = new ArrayList<TimedValue>();

        CSVFormat format = CSVFormat.DEFAULT;
        String fileLocation = getDatasourceSpec("adultObesity").getUrl();

        // The code below fetches the .csv file from the URL we specified in our DatasourceSpec object
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
            try{
                Subject subject = SubjectUtils.getSubjectByTypeAndNameUnique(localauthority, String.valueOf(row.get(2)).trim());

                // Checking not matched geometries
                if (subject!=null){
                    // Creating the time index
                    for (int timeValuesIndex = 3; timeValuesIndex <= 5; timeValuesIndex++) {

                        CSVRecord rowTime = (CSVRecord) csvRecords.get(5);
                        String year = rowTime.get(timeValuesIndex);

                        // Cleaning the year record as it appears as: 2014 (Mid-January 2014 to Mid-January 2015)
                        year = year.substring(0, 4);
                        LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);
                        log.info("The date appears as :" + rowTime.get(timeValuesIndex) + " in the dataset. Saving it as: " + timestamp.toString());
                        // The value is a string in our .csv file. We need to clean it before using it.
                        // We  need to check for invalid rows so we will suround this with a try catch clause
                        try {
                            String recordString = row.get(timeValuesIndex).replace("%", "");
                            String attributeName = row.get(0);
                            try {

                                Double record = Double.parseDouble(recordString);
                                // Here is where we are assigning the values of our .csv file to the attribute fields we
                                // created.
                                if (attributeName.contains("Underweight")){
                                    Attribute attribute = datasource.getTimedValueAttributes().get(0);
                                    timedValues.add(new TimedValue(
                                            subject,
                                            attribute,
                                            timestamp,
                                            record/100.));
                                }
                                else if (attributeName.contains("Healthy")){
                                    Attribute attribute = datasource.getTimedValueAttributes().get(1);
                                    timedValues.add(new TimedValue(
                                            subject,
                                            attribute,
                                            timestamp,
                                            record/100.));
                                }
                                else if (attributeName.contains("Overweight")){
                                    Attribute attribute = datasource.getTimedValueAttributes().get(2);
                                    timedValues.add(new TimedValue(
                                            subject,
                                            attribute,
                                            timestamp,
                                            record/100.));
                                }
                                else if (attributeName.contains("Obese")){
                                    Attribute attribute = datasource.getTimedValueAttributes().get(3);
                                    timedValues.add(new TimedValue(
                                            subject,
                                            attribute,
                                            timestamp,
                                            record/100.));
                                }
                                else if (attributeName.contains("Morbidly obese")){
                                    Attribute attribute = datasource.getTimedValueAttributes().get(4);
                                    timedValues.add(new TimedValue(
                                            subject,
                                            attribute,
                                            timestamp,
                                            record/100.));
                                }
                            } catch (IllegalStateException | NumberFormatException e) {
                                log.warn("Value for subject " + subject.getLabel().toString() + " not found. Defaulting to 0.0. Consider using a BackoffField or ConstantField");
                                continue;
                            }
                            // Catching invalid rows
                        } catch (ArrayIndexOutOfBoundsException npe) {
                            log.info("Found invalid row: Skipping");

                        }
                    }
                }
                // Catching invalid geometries
                else {
                    log.warn("Geometry not found for "+ row.get(2) + ": Skipping");
                    continue;
                }
            }
            // We need to check for again for invalid rows when looping through the local authorities names
            catch (ArrayIndexOutOfBoundsException npe){
                log.warn("Found invalid local authority row: Skipping");
                continue;
            }
        }
        // Finally we save the values in the database
        saveAndClearTimedValueBuffer(timedValues);
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceId) {

        // Creating a placeholder for our attributes
        List<Attribute> attributes = new ArrayList<>();

        // Dataset specific: we hardcode the columns names for the our .csv file
        String[] elements = { "fractionUnderweight", "fractionHealthyWeight", "fractionOverweight", "fractionObese", "fractionExcessWeight"};
        String[] descriptions = { "BMI less than 18.5kg/m2", "BMI greater than or equal to 18.5 but less than 25kg/m2", "BMI greater than or equal to 25 but less than 30kg/m2", "BMI greater than or equal to 30kg/m2", "BMI greater than or equal to 25kg/m2 (overweight including obese)"};

        // We loop through the elements of the elements object and adding an Attribute object in the list
        // with nour column names.
        for( int i = 0; i < elements.length; i++) {
            attributes.add(new Attribute(getProvider(), elements[i], descriptions[i]));
        }
        return attributes;
    }
}
