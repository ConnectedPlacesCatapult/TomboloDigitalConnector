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

import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by tbantis on 21/02/2018.
 */
public class PhysicalActivityImporter extends AbstractPheImporter {

    private static Logger log = LoggerFactory.getLogger(PhysicalActivityImporter.class);

    private enum DatasourceId {
        MVPA(new DatasourceSpec(
                PhysicalActivityImporter.class,
                "MVPA",
                "Moderate or Vigorous of P.A",
                "Proportion of  Active Residents",
                "http://activepeople.sportengland.org/Result/ExportTable?Id=130498&TabDimension=2&RowDimension=1&ColDimension=4&SelectedTabs[0]=15&ValueMode=0")
        );

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }

    public PhysicalActivityImporter(){
        datasourceIds = stringsFromEnumeration(PhysicalActivityImporter.DatasourceId.class);
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceIdString) throws Exception {
        return PhysicalActivityImporter.DatasourceId.valueOf(datasourceIdString).datasourceSpec;

    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        // We create SubjectType object that we will use to get the appropriate geometries
        // from OaImporter class
        // Fetching the data
        String fileLocation = DatasourceId.MVPA.datasourceSpec.getUrl();
        InputStreamReader isr = new InputStreamReader(
                downloadUtils.fetchInputStream(new URL(fileLocation), getProvider().getLabel(), ".csv"));

        // Parsing our csv file
        CSVParser csvFileParser = new CSVParser(isr, CSVFormat.DEFAULT);
        // Instantiating the list that will hold our .csv rows, contains also the empty lines so wee need to handle them
        List<CSVRecord> csvRecords = csvFileParser.getRecords();
        Iterator<CSVRecord> rowIterator = csvRecords.iterator();

        // We discard the first 6 records in our data file as these don't hold any meaningful information.
        int ignore = 0;
        while (ignore++ < 5) {
            rowIterator.next();
        }

        // Time labels
        CSVRecord rowTime = rowIterator.next();
        // Time series in the list
        HashMap<LocalDateTime, Integer> persistedTime = new HashMap<>();
        // The values are at columns: 3, 4, 5
        for (int timeValuesIndex = 3; timeValuesIndex <= 5; timeValuesIndex++) {
            String year = rowTime.get(timeValuesIndex);

            // Cleaning the year record as it appears as: 2014 (Mid-January 2014 to Mid-January 2015)
            LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year.substring(0, 4));
            persistedTime.put(timestamp, timeValuesIndex);
            log.info("The date appears as e.g. " + year + " in the dataset. Saving it as: " + timestamp);
        }

        // Get the SubjectType that we will use to get the appropriate geometries from OaImporter class
        SubjectType localauthority = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(
                AbstractONSImporter.PROVIDER.getLabel(), "localAuthority");
        // We create an empty list that will keep our .csv values. This importer contains timed values as they change
        // over time in the dataset.
        List<TimedValue> timedValues = new ArrayList<>();

        while (rowIterator.hasNext()) {
            CSVRecord row = rowIterator.next();
            try {
                String geography = row.get(2).trim();
                Subject subject = SubjectUtils.getSubjectByTypeAndNameUnique(localauthority, geography);

                if (subject == null) {
                    // Invalid or unknown geometries. In this importer a geometry is unknown to the system if it
                    // was not previously imported with the other Oa geometries.
                    log.warn("Geometry not found for " + geography + ": Skipping...");
                    continue;
                }

                String attributeName = row.get(0).trim();
                // Here is where we are assigning the values of our .csv file to the attribute fields we created.
                Attribute attribute = PhysicalActivityImporter.AttributeId.getAttributeIdByDesc(attributeName).attribute;
                for (LocalDateTime timestamp : persistedTime.keySet()) {
                    // We  need to check for invalid rows so we will surround this with a try catch clause
                    try {
                        // The value is a string in our .csv file, representing the value as percentage. We are
                        // persisting it as a fraction (/100) to make it easier for operations.
                        String recordString = row.get(persistedTime.get(timestamp)).replace("%", "");
                        Double record = Double.parseDouble(recordString);
                        timedValues.add(new TimedValue(subject, attribute, timestamp, record / 100.));
                    } catch (IllegalStateException | NumberFormatException e) {
                        log.warn("Value for subject " + subject.getLabel().toString() + " not found. " +
                                "Defaulting to 0.0. Consider using a BackoffField or ConstantField.");
                        continue;
                    }
                }
                // We need to check for again for invalid rows when looping through the local authorities names
            } catch (ArrayIndexOutOfBoundsException npe) {
                log.warn("Found invalid local authority row: Skipping...");
                continue;
            }
        }
        // Finally we save the values in the database
        saveAndClearTimedValueBuffer(timedValues);
    }
    public enum AttributeId {
        fractionActive("Active", "Active"),
        fractionActive150_599MVPA("Active - 150-599 MVPA", "Active - 150-599 MVPA"),
        fractionActive600PlusMVPA("Active - 600+ MVPA", "Active - 600+ MVPA")
        ;

        // Name and escription of the attribute
        String name;
        String description;
        Attribute attribute;

        AttributeId(String name, String description) {
            this.name = name;
            this.description = description;
            attribute = new Attribute(AbstractPheImporter.PROVIDER, name(), description);
        }

        public static PhysicalActivityImporter.AttributeId getAttributeIdByDesc(String description) {
            return Arrays.stream(PhysicalActivityImporter.AttributeId.values()).filter(element -> description.contains(element.name))
                    .findFirst().get();

        }
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceId) {
        // Creating a placeholder for our attributes
        List<Attribute> attributes = new ArrayList<>();
        for (PhysicalActivityImporter.AttributeId id : PhysicalActivityImporter.AttributeId.values()) {
            attributes.add(id.attribute);
        }
        return attributes;
    }
}
