package uk.org.tombolo.importer.manifestohealth;

import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
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
 * Created by tbantis on 11/01/2018.
 */
public class ActivePeopleSurveyCycleImporter extends AbstractImporter {
    private static final String DATASOURCE = "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/536501/cw0104.ods";

    public ActivePeopleSurveyCycleImporter(Config config) {
        super(config);
        try {
            // Specifying the datasourceId. This will be used by the DC recipe
            datasourceIds = Arrays.asList(getDatasourceSpec("ActivePeopleCycle").getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static final Provider PROVIDER = new Provider(
            "uk.gov.dft",
            "Department for Transport"
    );
    @Override
    public Provider getProvider() {
        return PROVIDER;
    }


    // Instantiating the datasoure specifications.
    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        DatasourceSpec datasourceSpec = new DatasourceSpec(
                ActivePeopleSurveyWalkImporter.class,
                "ActivePeopleCycle",
                "Proportion of adults who cycle at least once per week",
                "Proportion of adults who cycle at least once per week",
                DATASOURCE);
        return datasourceSpec;
    }


    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {
        SubjectType localauthority = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());

        List<TimedValue> timedValues = new ArrayList<TimedValue>();
        String fileLocation = getDatasourceSpec("ActivePeopleWalk").getUrl();
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
        InputStream isr = downloadUtils.fetchInputStream(url, getProvider().getLabel(), ".ods");
        SpreadsheetDocument workbook = SpreadsheetDocument.loadDocument(isr);
        Table sheet = workbook.getSheetByIndex(0);
        Iterator<Row> rowIterator = sheet.getRowIterator();

        for (int i = 0; i < 7; i++) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Subject subject = SubjectUtils.getSubjectByTypeAndLabel(localauthority, String.valueOf(row.getCellByIndex(0).getDisplayText()).trim());
            if (subject!=null){
                String year = "2017";
                LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);

                Double record = row.getCellByIndex(3).getDoubleValue();
                System.out.println(record);

                // Here is where we are assigning the values of our .xls file to the attribute fields we
                // created.
                Attribute attribute = datasource.getTimedValueAttributes().get(0);
                timedValues.add(new TimedValue(
                        subject,
                        attribute,
                        timestamp,
                        record));

            }
            saveAndClearTimedValueBuffer(timedValues);
        }
    }
    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceID) {
        // Creating a placeholder for our attributes
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(getProvider(), "cycle_x1pw", "Proportion of adults who cycle at least once per week"));

        return attributes;
    }
}
