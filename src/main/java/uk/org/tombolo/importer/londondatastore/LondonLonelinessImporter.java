package uk.org.tombolo.importer.londondatastore;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.ConfigurationException;
import uk.org.tombolo.importer.ons.AbstractONSImporter;
import uk.org.tombolo.importer.ons.OaImporter;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Age UK loneliness prevalence importer for London. For details visit https://data.london.gov.uk/dataset/probability-of-loneliness-for-those-aged-65-and-over
 *
 * Example of using it in a recipe:
 *
 * Datasource:
 *  {
 *   "importerClass": "uk.org.tombolo.importer.londondatastore.LondonLonelinessImporter",
 *   "datasourceId": "lonelinessPrevalence",
 *   "geographyScope": [
 *    "localAuthority",
 *    "msoa"
 *   ]
 *  }
 *
 *
 * {
 *  "fieldClass": "uk.org.tombolo.field.value.LatestValueField",
 *  "label": "logOdds",
 *  "attribute": {
 *    "provider": "uk.gov.london",
 *    "label": "logOdds"
 *   }
 *  }
 *
 */
public class LondonLonelinessImporter extends AbstractLondonDatastoreImporter {
    private static Logger log = LoggerFactory.getLogger(LondonLonelinessImporter.class);

    private enum DatasourceId {
        lonelinessPrevalence(new DatasourceSpec(
                LondonLonelinessImporter.class,
                "lonelinessPrevalence",
                "Prediction of the prevalence of loneliness at local authority level",
                "prediction of the prevalence of loneliness amongst usual residents, living in households, aged 65 in London. " +
                "The result is a final prediction value for each person, this tables shows these results as averages for each geographical area. "+
                "Areas with a value closer to 0 predict a greater prevalence of loneliness amongst those aged 65 and over and living in households compared to areas with a value further away. "+
                "The prediction values as described have not been designed by ONS, they are based on analysis by Age UK of the Wave 5 (June 2010 - July 2012) of the English Longitudinal Study." +
                "For more information visit https://data.london.gov.uk/dataset/probability-of-loneliness-for-those-aged-65-and-over",
                "https://files.datapress.com/london/dataset/probability-of-loneliness-for-those-aged-65-and-over/2015-12-01T14:12:11/london-loneliness-MSOA-LSOA.xlsx")
        );
        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }

    public LondonLonelinessImporter(){
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        return DatasourceId.valueOf(datasourceId).datasourceSpec;
    }
    @Override
    protected List<String> getOaDatasourceIds() {
        return Arrays.asList(
                OaImporter.OaType.localAuthority.datasourceSpec.getId(),
                OaImporter.OaType.msoa.datasourceSpec.getId(),
                OaImporter.OaType.lsoa.datasourceSpec.getId());
    }
    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        String fileLocation = LondonLonelinessImporter.DatasourceId.lonelinessPrevalence.datasourceSpec.getUrl();
        InputStream isr = downloadUtils.fetchInputStream(new URL(fileLocation), getProvider().getLabel(), ".xlsx");

        List<TimedValue> timedValues = new ArrayList<TimedValue>();

        XSSFWorkbook workbook = new XSSFWorkbook(isr);

        if (geographyScope == null || geographyScope.isEmpty()) {
            geographyScope = new ArrayList<>();
            geographyScope.add("localAuthority");
            log.warn("No geography scope provided. Defaulting to Local Authority");
        }

        for (String geographyLabel : geographyScope) {
            Sheet datatypeSheet = null;
            SubjectType subjectType = null;
            List<Integer> validAttributes = new ArrayList<>();

            switch (geographyLabel) {
                case "localAuthority":
                    datatypeSheet = workbook.getSheetAt(1);
                    subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(AbstractONSImporter.PROVIDER.getLabel(), getOaDatasourceIds().get(0));
                    validAttributes.addAll(Arrays.asList(0,1,2,3));
                    break;
                case "msoa":
                    datatypeSheet = workbook.getSheetAt(2);
                    subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(AbstractONSImporter.PROVIDER.getLabel(), getOaDatasourceIds().get(1));
                    validAttributes.addAll(Arrays.asList(0,1,2,3,4));
                    break;
                case "lsoa":
                    datatypeSheet = workbook.getSheetAt(3);
                    subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(AbstractONSImporter.PROVIDER.getLabel(), getOaDatasourceIds().get(2));
                    validAttributes.addAll(Arrays.asList(0,2,3,4));
                    break;
            }
            // Creating the row iterator object
            Iterator<Row> rowIterator = datatypeSheet.rowIterator();
            LocalDateTime timestamp = TimedValueUtils.parseTimestampString("Jun-15");
            log.info("The analysis was made in {} and we persist it as {}", "June 2015", timestamp);
            // Skipping unrelevant rows
            rowIterator.next();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String geograghy =  String.valueOf(row.getCell(0)).trim();
                Subject subject = SubjectUtils.getSubjectByTypeAndLabel(subjectType, geograghy);
                if (subject != null) {
                    try {
                        ListIterator<Integer> it = validAttributes.listIterator();
                        while (it.hasNext()) {
                            Double record = row.getCell(it.nextIndex()+2).getNumericCellValue();
                            Attribute attribute = datasource.getTimedValueAttributes().get(it.next());
                            timedValues.add(new TimedValue(
                                    subject,
                                    attribute,
                                    timestamp,
                                    record));
                        }
                    } catch (IllegalStateException e) {
                        log.warn("Value for subject " + subject.getLabel() + " not found. " +
                                "Defaulting to 0.0. Consider using a BackoffField or ConstantField.");
                        continue;
                    }
                }
            }
        }
        saveAndClearTimedValueBuffer(timedValues);
        workbook.close();
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceID) {
        // Creating a placeholder for our attributes
        List<Attribute> attributes = new ArrayList<>();

        // Dataset specific: we hardcode the columns names for the our .csv file
        String[] elements = { "logOdds","prevalence", "rankInLondon", "rankInEngland", "londonDecile"};
        String[] description = { "Logg Odds value of the Logistic Regression (see https://www.ageuk.org.uk/globalassets/age-uk/documents/reports-and-publications/reports-and-briefings/health--wellbeing/predicting_the_prevalence_of_loneliness_at_older_ages.pdf for details)",
                "Prevalence",
                "Rank in London 1=highest 4835= lowest",
                "Rank England 1 = highest 32,843 = lowest",
                "London Decile, 1=most lonely"
        };

        // We loop through the elements of the elements object and adding an Attribute object in the list
        // with nour column names.
        for( int i = 0; i < elements.length; i++) {
            attributes.add(new Attribute(getProvider(), elements[i], description[i]));
        }
        return attributes;
    }
}
