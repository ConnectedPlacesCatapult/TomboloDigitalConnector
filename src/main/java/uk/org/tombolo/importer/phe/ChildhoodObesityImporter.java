package uk.org.tombolo.importer.phe;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.ons.AbstractONSImporter;
import uk.org.tombolo.importer.ons.OaImporter;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;


/**
 * Importer for importing childhood obesity data.
 * Note that this is the second iteration of the importer after the data for the first became obsolete
 * As a result, the importer underwent major reformatting. If you wish to refer to the first iteration
 * importer please trace the appropriate node on github.
 */
public class ChildhoodObesityImporter extends AbstractPheImporter {
    private static Logger log = LoggerFactory.getLogger(ChildhoodObesityImporter.class);

    private enum DatasourceId {
        childhoodObesityLA(new DatasourceSpec(
                ChildhoodObesityImporter.class,
                "childhoodObesityLA",
                "Childhood Obesity at local authority level",
                "",
                "https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/610203/NCMP_data_LA_and_England.xlsx")
        ),
        childhoodObesityMSOA(new DatasourceSpec(
                ChildhoodObesityImporter.class,
                "childhoodObesityMSOA",
                "Childhood Obesity at MSOA level",
                "",
                "https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/610199/NCMP_data_MSOA.xlsx")
        ),
        childhoodObesityWard(new DatasourceSpec(
                ChildhoodObesityImporter.class,
                "childhoodObesityWard",
                "Childhood Obesity at Ward level",
                "",
                "https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/652653/NCMP_data_Ward.xlsx")
        );
        private DatasourceSpec datasourceSpec;
        DatasourceId(DatasourceSpec datasourceSpec) {
            this.datasourceSpec = datasourceSpec;
        }
    }

    public ChildhoodObesityImporter(){
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    @Override
    public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
        return DatasourceId.valueOf(datasourceId).datasourceSpec;
    }

    @Override
    protected List<String> getOaDatasourceIds() {
        return Arrays.asList(super.getOaDatasourceIds().get(0), OaImporter.OaType.msoa.datasourceSpec.getId());
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope,  List<String> datasourceLocation) throws Exception {

        // Check the datasourceid to reference the corresponding url
        String whichDatasource = datasource.getDatasourceSpec().getId();
        String fileLocation = getDatasourceSpec(whichDatasource).getUrl();

        List<TimedValue> timedValues = new ArrayList<TimedValue>();

        // The code below fetches the .xls file from the URL we specified in our DatasourceSpec object
        URL url;
        try {
            url = new URL(fileLocation);
        } catch (MalformedURLException e) {
            File file;
            if (!(file = new File(fileLocation)).exists()) {
                log.error("File does not exist: " + fileLocation);
            }
            url = file.toURI().toURL();
        }

        // Fetching and reading the file using fetchInputStream
        InputStream isr = downloadUtils.fetchInputStream(url, getProvider().getLabel(), ".xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(isr);

        // Loop through the excell sheets
        for (int sheet = 1; sheet <= 4; sheet = sheet+1){
            Sheet datatypeSheet = workbook.getSheetAt(sheet);

            Iterator<Row> rowIterator = datatypeSheet.rowIterator();
            int j = whichDatasource.equals("childhoodObesityLA")
                    || whichDatasource.equals("childhoodObesityMSOA") ? 3 : 4;
            for (int i=0; i<j; i++){
                rowIterator.next();

            }

            while (rowIterator.hasNext()) {

                Row row = rowIterator.next();
                List<Integer> loopingIndices = getLoopingIndices(whichDatasource);
                Subject subject = SubjectUtils.getSubjectByTypeAndLabel(getSubjectGeometry(whichDatasource), String.valueOf(row.getCell(loopingIndices.get(2))).trim());

                // Dataset specific: The dataset contains mixed geometries. Check that the geometries in the excel file
                // match the "Area code" column. If they are not null proceed
                if (subject!=null){

                    // Dataset specific:  Looping through the time value
                    for (int timeValuesIndex=loopingIndices.get(0); timeValuesIndex < datatypeSheet.getRow(loopingIndices.get(3)).getLastCellNum(); timeValuesIndex+=loopingIndices.get(1)) {

                        // The temporal framework is given in intervals. We take the end of the interval to be our time value
                        Row rowTime = datatypeSheet.getRow(loopingIndices.get(3));
                        String year = rowTime.getCell(timeValuesIndex).toString();
                        year =  "20" + year.substring(year.length()-2);
                        LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);

                        Row rowAttr = datatypeSheet.getRow(loopingIndices.get(3)+1);
                        String attrPercentage = rowAttr.getCell(timeValuesIndex+ 2).getStringCellValue();
                        String attrLci = rowAttr.getCell(timeValuesIndex+ 3).getStringCellValue();
                        String attrUci = rowAttr.getCell(timeValuesIndex+ 4).getStringCellValue();

                        try {

                            String attributeSheetName = datatypeSheet.getSheetName();

                            for( int i = 0; i < datasource.getTimedValueAttributes().size(); i++) {
                                if (datasource.getTimedValueAttributes().get(i).getLabel().contains(attributeSheetName + "_" + attrPercentage)){
                                    Double record_percentage = row.getCell(timeValuesIndex + 2).getNumericCellValue();
                                    Attribute attribute_percentage = datasource.getTimedValueAttributes().get(i);
                                    timedValues.add(new TimedValue(
                                            subject,
                                            attribute_percentage,
                                            timestamp,
                                            record_percentage/100.));

                                } else if (datasource.getTimedValueAttributes().get(i).getLabel().contains(attributeSheetName + "_" + attrLci)){
                                    Double record_lci = row.getCell(timeValuesIndex + 3).getNumericCellValue();
                                    Attribute attribute_lci = datasource.getTimedValueAttributes().get(i);
                                    timedValues.add(new TimedValue(
                                            subject,
                                            attribute_lci,
                                            timestamp,
                                            record_lci/100.));

                                } else if (datasource.getTimedValueAttributes().get(i).getLabel().contains(attributeSheetName + "_" + attrUci)){
                                    Double record_uci = row.getCell(timeValuesIndex + 4).getNumericCellValue();
                                    Attribute attribute_uci = datasource.getTimedValueAttributes().get(i);
                                    timedValues.add(new TimedValue(
                                            subject,
                                            attribute_uci,
                                            timestamp,
                                            record_uci/100.));
                                } else {
                                    continue;
                                }
                            }
                        } catch (IllegalStateException e) {
                            continue;
                        }
                    }
                }
            }
        }
        saveAndClearTimedValueBuffer(timedValues);
        workbook.close();
    }

    public List<Integer> getLoopingIndices (String which_datasource){
        // There are discrepancies in the excel files when it comes to column/row numbers
        // This function creates a list that corresponds to the column names we are interested in
        // First element corresponds to the position of the data column
        // Second element corresponds to the loop step
        // Third element  corresponds to the subject column
        // Fourth element  corresponds to the time column
        List<Integer> loopingIndices = new ArrayList<>();
        if (which_datasource.equals("childhoodObesityLA")){
            loopingIndices = Arrays.asList(2,5,0,1);
        } else if (which_datasource.equals("childhoodObesityMSOA")){
            loopingIndices = Arrays.asList(4,5,0,1);
        } else {
            loopingIndices = Arrays.asList(5,5,1,2);
        }
        return loopingIndices;
    }

    public SubjectType getSubjectGeometry (String which_datasource){
        SubjectType subjectGeometry = new SubjectType();
        if (which_datasource.equals("childhoodObesityLA")){
            subjectGeometry = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                    OaImporter.OaType.localAuthority.name(), OaImporter.OaType.localAuthority.datasourceSpec.getDescription());

        } else if (which_datasource.equals("childhoodObesityMSOA")){
            subjectGeometry = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                    OaImporter.OaType.msoa.name(), OaImporter.OaType.msoa.datasourceSpec.getDescription());
        } else {
            subjectGeometry = SubjectTypeUtils.getOrCreate(AbstractONSImporter.PROVIDER,
                    OaImporter.OaType.ward.name(), OaImporter.OaType.ward.datasourceSpec.getDescription());
        }
        return subjectGeometry;
    }

    @Override
    public List<Attribute> getTimedValueAttributes(String datasourceId) {

        // Creating a placeholder for our attributes
        List<Attribute> attributes = new ArrayList<>();

        // Dataset specific: we hardcode the columns names for the our .csv file
        String[] elements = { "Reception_ExcessWeight_%","Reception_ExcessWeight_LCI", "Reception_ExcessWeight_UCI",
                "Reception_Obese_%", "Reception_Obese_LCI", "Reception_Obese_UCI",
                "Year6_ExcessWeight_%", "Year6_ExcessWeight_LCI", "Year6_ExcessWeight_UCI",
                "Year6_Obese_%", "Year6_Obese_LCI", "Year6_Obese_UCI"
        };

        // We loop through the elements of the elements object and adding an Attribute object in the list
        // with nour column names.
        for( int i = 0; i < elements.length; i++) {
            attributes.add(new Attribute(getProvider(), elements[i], elements[i]));
        }
        return attributes;
    }
}

