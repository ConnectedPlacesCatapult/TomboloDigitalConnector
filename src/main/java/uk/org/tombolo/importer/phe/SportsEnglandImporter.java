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
 * Importer for importing the adult obesity and physical activity from the Sports England website and provided by Public Health England.
 * Data fetched from http://activepeople.sportengland.org. Visualise full dataset at:
 * http://activepeople.sportengland.org/Result/ExportTable?Id=104519&TabDimension=2&RowDimension=1&ColDimension=4&SelectedTabs[0]=7&ValueMode=0
 * http://activepeople.sportengland.org/Result/ExportTable?Id=130498&TabDimension=2&RowDimension=1&ColDimension=4&SelectedTabs[0]=15&ValueMode=0
 *
 */


/* INFO FOR RECIPE
"importerClass":"uk.org.tombolo.importer.phe.SportsEnglandImporter"
"datasourceId":"adultObesity"
"provider":"uk.gov.phe"
"subjectTypes":["localAuthority"]
"timedValueAttributes":[
    {"label":"fractionUnderweight","provider":"uk.gov.london"},
    {"label":"fractionHealthyWeight","provider":"uk.gov.london"},
    {"label":"fractionOverweight","provider":"uk.gov.london"},
    {"label":"fractionObese","provider":"uk.gov.london"},
    {"label":"fractionExcessWeight","provider":"uk.gov.london"
    ]
"fixedValueAttributes":[]


"importerClass":"uk.org.tombolo.importer.phe.SportsEnglandImporter"
"datasourceId":"MVPA"
"provider":"uk.gov.phe"
"subjectTypes":["localAuthority"]
"timedValueAttributes":[
    {"label":"fractionActive","provider":"uk.gov.phe"},
    {"label":"fractionActive600PlusMVPA","provider":"uk.gov.phe"},
    {"label":"fractionActive150_599MVPA","provider":"uk.gov.phe"}
    ]
"fixedValueAttributes":[]


 */
public class SportsEnglandImporter extends AbstractPheImporter {
    private static Logger log = LoggerFactory.getLogger(SportsEnglandImporter.class);

    private enum DatasourceId {
        adultObesity(new DatasourceSpec(
                SportsEnglandImporter.class,
                "adultObesity",
                "Local Authority Adult Obesity",
                "Self reported adult obesity",
                "http://activepeople.sportengland.org/Result/ExportTable?Id=104519&TabDimension=2&RowDimension=1&ColDimension=4&SelectedTabs[0]=7&ValueMode=0")
        ),
        MVPA(new DatasourceSpec(
                SportsEnglandImporter.class,
                "MVPA",
                "Moderate or Vigorous of P.A",
                "Proportion of Residents performing Moderate or Vigorous of P.A",
                "http://activepeople.sportengland.org/Result/ExportTable?Id=130498&TabDimension=2&RowDimension=1&ColDimension=4&SelectedTabs[0]=15&ValueMode=0")
        );

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }

    public SportsEnglandImporter(){
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceIdString) throws Exception {
        return DatasourceId.valueOf(datasourceIdString).datasourceSpec;

    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        // Fetching the data
        String whichDatasource = datasource.getDatasourceSpec().getId();
        String fileLocation = getDatasourceSpec(whichDatasource).getUrl();
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
                AbstractONSImporter.PROVIDER.getLabel(),"localAuthority");
        // We create an empty list that will keep our .csv values. This importer contains timed values as they change
        // over time in the dataset.
        List<TimedValue> timedValues = new ArrayList<>();
        // Looping through the rows of the .csv file
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
                Attribute attribute;
                if (whichDatasource.equals("adultObesity")) {
                    attribute = AttributeId.getAttributeIdByDesc(attributeName).attribute;
                } else {
                    attribute = AttributeId.getAttributeIdByEqual(attributeName).attribute;
                }

                for (LocalDateTime timestamp: persistedTime.keySet()) {
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
        fractionUnderweight("Underweight", "BMI (Body Mass Index) - Underweight [BMI < 18.5 kg/m≤]"),
        fractionHealthyWeight("Healthy weight", "BMI (Body Mass Index) - Healthy weight  [BMI range 18.5 - 24.9 kg/m≤]"),
        fractionOverweight("Overweight", "BMI (Body Mass Index) - Overweight [BMI range 25 - 29.9 kg/m≤]"),
        fractionObese("Obese", "BMI (Body Mass Index) - Obese [BMI range 30-39.9 kg/m≤]"),
        fractionMorbidlyObese("Morbidly obese", "BMI (Body Mass Index) - Morbidly obese [BMI > 40 kg/m≤]"),
        fractionActive("Active", "Active"),
        fractionActive150_599MVPA("150-599 MVPA", "Active - 150-599 MVPA"),
        fractionActive600PlusMVPA("600+ MVPA", "Active - 600+ MVPA")
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

        public static AttributeId getAttributeIdByDesc(String description) {
            return Arrays.stream(AttributeId.values()).filter(element -> description.contains(element.name))
                    .findFirst().get();
        }
        public static AttributeId getAttributeIdByEqual(String description) {
            return Arrays.stream(AttributeId.values()).filter(element -> description.equals(element.description))
                    .findFirst().get();
        }
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceId) {
        // Creating a placeholder for our attributes
        List<Attribute> attributes = new ArrayList<>();
        for (AttributeId id : AttributeId.values()) {
            attributes.add(id.attribute);
        }
        return attributes;
    }
}
