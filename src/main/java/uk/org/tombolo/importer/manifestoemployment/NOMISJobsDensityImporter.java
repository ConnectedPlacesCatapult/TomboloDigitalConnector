package uk.org.tombolo.importer.manifestoemployment;

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
 * Created by tbantis on 02/01/2018.
 */
public class NOMISJobsDensityImporter extends AbstractImporter {

    private static final String DATASOURCE = "http://www.nomisweb.co.uk/api/v01/dataset/NM_57_1.data.csv?geography=1870659585...1870659791,1870659801,1870659792...1870659800&date=latestMINUS1-latest&item=1,3&measures=20100&select=date_name,geography_name,geography_code,item_name,measures_name,obs_value,obs_status_name";
    private List csvRecords;

    public NOMISJobsDensityImporter(Config config) {
        super(config);
        try {
            // Specifying the datasourceId. This will be used by the DC recipe
            datasourceIds = Arrays.asList(getDatasourceSpec("NOMISJobsDensityImporter").getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Instantiating the data Provider
    protected static final Provider PROVIDER = new Provider(
            "nomisweb.co.uk",
            "NOMIS Jobs density and total jobs"
    );
    @Override
    public Provider getProvider() {
        return PROVIDER;
    }

    // Instantiating the datasoure specifications.
    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        DatasourceSpec datasourceSpec = new DatasourceSpec(
                NOMISJobsDensityImporter.class,
                "NOMISJobs",
                "NOMIS Jobs",
                "Jobs Density and Total jobs",
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
        String fileLocation = getDatasourceSpec("NOMISJobs").getUrl();

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

        rowIterator.next();

        while (rowIterator.hasNext()) {
            CSVRecord row = rowIterator.next();
            if (String.valueOf(row.get(6)).trim().equals("Normal Value")){
                try {
                    Subject subject = SubjectUtils.getSubjectByTypeAndLabel(localauthority, String.valueOf(row.get(2)).trim());
                    if (subject!=null){
                        String year = row.get(0).toString();
                        String variable = row.get(3).toString();
                        if (year.trim().equals("2014") && variable.trim().equals("Total jobs")){

                            Double record = Double.parseDouble(row.get(5));
                            LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);
                            // Here is where we are assigning the values of our .csv file to the attribute fields we
                            // created.
                            Attribute attribute = datasource.getTimedValueAttributes().get(0);


                            timedValues.add(new TimedValue(
                                    subject,
                                    attribute,
                                    timestamp,
                                    record));
                        }
                        if (year.trim().equals("2015") && variable.trim().equals("Total jobs")){
                            Double record = Double.parseDouble(row.get(5));
                            LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);
                            // Here is where we are assigning the values of our .csv file to the attribute fields we
                            // created.
                            Attribute attribute = datasource.getTimedValueAttributes().get(1);

                            timedValues.add(new TimedValue(
                                    subject,
                                    attribute,
                                    timestamp,
                                    record));
                        }
                        if (year.trim().equals("2014") && variable.trim().equals("Jobs density")){

                            Double record = Double.parseDouble(row.get(5));
                            LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);
                            // Here is where we are assigning the values of our .csv file to the attribute fields we
                            // created.
                            Attribute attribute = datasource.getTimedValueAttributes().get(2);


                            timedValues.add(new TimedValue(
                                    subject,
                                    attribute,
                                    timestamp,
                                    record));
                        }
                        if (year.trim().equals("2015") && variable.trim().equals("Jobs density")){
                            Double record = Double.parseDouble(row.get(5));
                            LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);
                            // Here is where we are assigning the values of our .csv file to the attribute fields we
                            // created.
                            Attribute attribute = datasource.getTimedValueAttributes().get(3);

                            timedValues.add(new TimedValue(
                                    subject,
                                    attribute,
                                    timestamp,
                                    record));
                        }

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
        String[] elements = { "total_jobs_2014", "total_jobs_2015", "job_density_2014", "job_density_2015"};
        for( int i = 0; i < elements.length; i++) {
            attributes.add(new Attribute(getProvider(), elements[i], elements[i]));

        }
        return attributes;
    }
}
