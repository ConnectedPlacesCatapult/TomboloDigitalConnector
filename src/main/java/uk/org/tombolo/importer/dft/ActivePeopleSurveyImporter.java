package uk.org.tombolo.importer.dft;

import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.ons.AbstractONSImporter;
import uk.org.tombolo.importer.ons.OaImporter;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Importer for Proportion of how often and how long adults walk/cycle by local authority/england boundaries, 2014/15
 * sourced from Department for Transport statistics
 * url: https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/536499/cw0105.ods
 * url: https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/536501/cw0104.ods
 *
 * Example of using this in recipe:
 * // datasource
 * {
 *  "importerClass": "uk.org.tombolo.importer.dft.ActivePeopleSurveyImporter",
 *  "datasourceId": "activePeopleCycle"
 * }
 *
 * // field
 * {
 *  "fieldClass": "uk.org.tombolo.field.value.LatestValueField",
 *  "label": "fractionCycleRecreation_less30mins",
 *  "attribute": {
 *  "provider": "uk.gov.dft",
 *  "label": "fractionCycleRecreation_less30mins"
 *   }
 * }
 */
public class ActivePeopleSurveyImporter extends AbstractDFTImporter {
    private static Logger log = LoggerFactory.getLogger(ActivePeopleSurveyImporter.class);

    private enum DatasourceId {
        activePeopleCycle(new DatasourceSpec(
                ActivePeopleSurveyImporter.class,
                "activePeopleCycle",
                "Proportion of adults who cycle at least once per week",
                "Proportion of adults who cycle at different time periods",
                "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/536501/cw0104.ods")
        ),
        activePeopleWalk(new DatasourceSpec(
                ActivePeopleSurveyImporter.class,
                "activePeopleWalk",
                "Proportion of adults who walk",
                "Proportion of adults who walk at different time periods",
                "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/536499/cw0105.ods")
        );

        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }

    public ActivePeopleSurveyImporter(){
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    // Instantiating the datasource specifications.
    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceIdString) throws Exception {
        return DatasourceId.valueOf(datasourceIdString).datasourceSpec;

    }

    @Override
    protected List<String> getOaDatasourceIds() {
        return Arrays.asList(
                OaImporter.OaType.localAuthority.datasourceSpec.getId(),
                OaImporter.OaType.englandBoundaries.datasourceSpec.getId());
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {

        // Check the datasourceid to reference the corresponding url
        String whichDatasource = datasource.getDatasourceSpec().getId();
        String fileLocation = getDatasourceSpec(whichDatasource).getUrl();
        URL url = new URL(fileLocation);
        InputStream isr = downloadUtils.fetchInputStream(url, getProvider().getLabel(), ".ods");

        SpreadsheetDocument workbook = SpreadsheetDocument.loadDocument(isr);
        Table sheet = workbook.getSheetByIndex(0);
        Iterator<Row> rowIterator = sheet.getRowIterator();

        SubjectType localauthority = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(AbstractONSImporter.PROVIDER.getLabel(), getOaDatasourceIds().get(0));
        SubjectType englandboundaries = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(AbstractONSImporter.PROVIDER.getLabel(), getOaDatasourceIds().get(1));

        List<TimedValue> timedValues = new ArrayList<TimedValue>();

        int ignore = 0;
        while (ignore++ < 7) {
            rowIterator.next();
        }

        // Getting the indices of the columns containing the data.
        int columnIndex = 0;
        List<Integer> columnLoop = new ArrayList<>();
        while ( columnIndex++ <= 24) {
            if (!(columnIndex % 5 == 0 && columnIndex != 0)) {
                columnLoop.add(columnIndex+1);
            }
        }

        Row rowTime = sheet.getRowByIndex(3);
        String year = rowTime.getCellByIndex(0).getStringValue();
        year =  "20" + year.substring(year.length()-2);
        LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Subject subject = SubjectUtils.getSubjectByTypeAndLabel(localauthority, String.valueOf(row.getCellByIndex(0).getDisplayText()).trim());
            Subject subjecteb = SubjectUtils.getSubjectByTypeAndLabel(englandboundaries, String.valueOf(row.getCellByIndex(0).getDisplayText()).trim());

            if (subject != null) {
                for (int i = 0; i < columnLoop.size(); i++) {
                    Double record = row.getCellByIndex(columnLoop.get(i)).getDoubleValue();
                    Attribute attribute = datasource.getTimedValueAttributes().get(i);

                    timedValues.add(new TimedValue(
                            subject,
                            attribute,
                            timestamp,
                            record/100.));
                }
            }
            else if (subjecteb != null) {
                for (int i = 0; i < columnLoop.size(); i++) {
                    Double record = row.getCellByIndex(columnLoop.get(i)).getDoubleValue();
                    Attribute attribute = datasource.getTimedValueAttributes().get(i);

                    timedValues.add(new TimedValue(
                            subjecteb,
                            attribute,
                            timestamp,
                            record/100.));
                }
            }
        }
        saveAndClearTimedValueBuffer(timedValues);
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceId) {
        // Creating a placeholder for our attributes
        List<Attribute> attributes = new ArrayList<>();

        if (datasourceId.equals("activePeopleWalk")) {
            String[] elements = {
                    "fractionWalk_1pm", "fractionWalk_1pw", "fractionWalk_3pw", "fractionWalk_5pw",
                    "fractionWalkRecreation_1pm", "fractionWalkRecreation_1pw", "fractionWalkRecreation_3pw", "fractionWalkRecreation_5pw",
                    "fractionWalkUtility_1pm", "fractionWalkUtility_1pw", "fractionWalkUtility_3pw", "fractionWalkUtility_5pw",
                    "fractionWalk_less30mins", "fractionWalk_less60mins", "fractionWalk_less120mins", "fractionWalk_more120mins",
                    "fractionWalkRecreation_less30mins", "fractionWalkRecreation_less60mins", "fractionWalkRecreation_less120mins", "fractionWalkRecreation_more120mins"
            };
            String[] description = {
                    "% of adults that walk at least 1 x per month", "% of adults that walk at least 1 x per week", "% of adults that walk at least 3 x per week", "% of adults that walk at least 5 x per week",
                    "% of adults that walk for recreational purposes at least 1 x per month", "% of adults that walk for recreational purposes at least 1 x per week", "% of adults that walk for recreational purposes at least 3 x per week", "% of adults that walk for recreational purposes at least 5 x per week",
                    "% of adults that walk for utility purposes at least at least 1 x per month", "% of adults that walk for utility purposes at least at least 1 x per week", "% of adults that walk for utility purposes at least at least 3 x per week", "% of adults that walk for utility purposes at least at least 5 x per week",
                    "% of adults usually walking for given lengths of time per day < half hour", "% of adults usually walking for given lengths of time per day half to <1 hour", "% of adults usually walking for given lengths of time per day 1 to <2 hours", "% of adults usually walking for given lengths of time per day 2 to 17 hours",
                    "% of adults usually walking recreationally for given lengths of time per day < half hour", "% of adults usually walking recreationally for given lengths of time per day half to <1 hour", "% of adults usually walking recreationally for given lengths of time per day 1 to <2 hours", "% of adults usually walking recreationally for given lengths of time per day 2 to 17 hours"
            };
            for (int i = 0; i < elements.length; i++) {
                attributes.add(new Attribute(getProvider(), elements[i], description[i]));
            }
        } else if (datasourceId.equals("activePeopleCycle")) {
            String[] elements = {
                    "fractionCycle_1pm", "fractionCycle_1pw", "fractionCycle_3pw", "fractionCycle_5pw",
                    "fractionCycleRecreation_1pm", "fractionCycleRecreation_1pw", "fractionCycleRecreation_3pw", "fractionCycleRecreation_5pw",
                    "fractionCycleUtility_1pm", "fractionCycleUtility_1pw", "fractionCycleUtility_3pw", "fractionCycleUtility_5pw",
                    "fractionCycle_less30mins", "fractionCycle_less60mins", "fractionCycle_less120mins", "fractionCycle_more120mins",
                    "fractionCycleRecreation_less30mins", "fractionCycleRecreation_less60mins", "fractionCycleRecreation_less120mins", "fractionCycleRecreation_more120mins"
            };
            String[] description = {
                    "% of adults that cycle at least 1 x per month", "% of adults that cycle at least 1 x per week", "% of adults that cycle at least 3 x per week", "% of adults that cycle at least 5 x per week",
                    "% of adults that cycle for recreational purposes at least 1 x per month", "% of adults that cycle for recreational purposes at least 1 x per week", "% of adults that cycle for recreational purposes at least 3 x per week", "% of adults that cycle for recreational purposes at least 5 x per week",
                    "% of adults that cycle for utility purposes at least at least 1 x per month", "% of adults that cycle for utility purposes at least at least 1 x per week", "% of adults that cycle for utility purposes at least at least 3 x per week", "% of adults that cycle for utility purposes at least at least 5 x per week",
                    "% of adults usually cycleing for given lengths of time per day < half hour", "% of adults usually cycling for given lengths of time per day half to <1 hour", "% of adults usually cycling for given lengths of time per day 1 to <2 hours", "% of adults usually cycling for given lengths of time per day 2 to 17 hours",
                    "% of adults usually cycleing recreationally for given lengths of time per day < half hour", "% of adults usually cycling recreationally for given lengths of time per day half to <1 hour", "% of adults usually cycling recreationally for given lengths of time per day 1 to <2 hours", "% of adults usually cycling recreationally for given lengths of time per day 2 to 17 hours"
            };
            for (int i = 0; i < elements.length; i++) {
                attributes.add(new Attribute(getProvider(), elements[i], description[i]));
            }
        }
        return attributes;
    }
}
