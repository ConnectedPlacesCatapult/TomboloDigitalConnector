package uk.org.tombolo.importer.dft;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.ConfigurationException;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.ons.AbstractONSImporter;
import uk.org.tombolo.importer.ons.OaImporter;
import uk.org.tombolo.importer.utils.ExcelUtils;
import uk.org.tombolo.importer.utils.extraction.ConstantExtractor;
import uk.org.tombolo.importer.utils.extraction.RowCellExtractor;
import uk.org.tombolo.importer.utils.extraction.TimedValueExtractor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Importer for DfT Accessibility information.
 *
 * https://www.gov.uk/government/statistical-data-sets/acs05-travel-time-destination-and-origin-indicators-to-key-sites-and-services-by-lower-super-output-area-lsoa
 *
 */
public class AccessibilityImporter extends AbstractDFTImporter implements Importer{
    private static final Logger log = LoggerFactory.getLogger(AccessibilityImporter.class);
    private static SubjectType subjectType = OaImporter.getSubjectType(OaImporter.OaType.lsoa);

    private enum DatasourceId {
        acs0501, acs0502, acs0503, acs0504, acs0505, acs0506, acs0507, acs0508
    };

    private static final String DATASOURCE_URL
            = "https://www.gov.uk/government/statistical-data-sets/" +
                "acs05-travel-time-destination-and-origin-indicators-to-key-sites-and-services-" +
                "by-lower-super-output-area-lsoa";

    private static final String DATASET_FILE_SUFFIX = ".xls";
    private static final String[] datasetFiles = {
            "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357458/acs0501.xls",
            "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357460/acs0502.xls",
            "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357461/acs0503.xls",
            "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357464/acs0504.xls",
            "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357467/acs0505.xls",
            "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357468/acs0506.xls",
            "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357469/acs0507.xls",
            "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/357467/acs0508.xls"
    };

    private String[] datasetDescriptions = {
            "Travel time, destination and origin indicators to Employment centres by mode of travel",
            "Travel time, destination and origin indicators to Primary schools by mode of travel",
            "Travel time, destination and origin indicators to Secondary schools by mode of travel",
            "Travel time, destination and origin indicators to Further Education institutions by mode of travel",
            "Travel time, destination and origin indicators to GPs by mode of travel",
            "Travel time, destination and origin indicators to Hospitals by mode of travel",
            "Travel time, destination and origin indicators to Food stores by mode of travel",
            "Travel time, destination and origin indicators to Town centres by mode of travel"
    };

    ExcelUtils excelUtils = new ExcelUtils();

    public AccessibilityImporter(Config config){
        super(config);
        datasourceIds = stringsFromEnumeration(DatasourceId.class);
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {
        DatasourceId datasourceIdValue = DatasourceId.valueOf(datasourceId);
        if (datasourceIdValue == null)
            throw new ConfigurationException("Unknown datasourceId: " + datasourceId);

        Datasource datasource = new Datasource(
                getClass(),
                datasourceId,
                getProvider(),
                datasourceId,
                datasetDescriptions[datasourceIdValue.ordinal()]);
        datasource.setUrl(DATASOURCE_URL);

        // Attributes
        // In order to get the attributes we need to download the entire xls file, which is a bit of an overload.
        // In addition, if we want to get a list of all available datasets we need to download all the xls file.
        // An alternative would be to use a pre-compiled list of attributes with the downside that it is not
        // robust to changes in the underlying xls file.
        // FIXME: Consider using a pre-compiled list of attributes
        Workbook workbook = excelUtils.getWorkbook(
                downloadUtils.fetchInputStream(getDatasourceUrl(datasourceIdValue), getProvider().getLabel(), DATASET_FILE_SUFFIX));
        Sheet metadataSheet = workbook.getSheet("Metadata");

        int rowId = 12;
        while(true){
            rowId++;
            Row row = metadataSheet.getRow(rowId);
            if (row == null || row.getCell(0) == null)
                break;
            String name = row.getCell(0).getStringCellValue();
            String label = row.getCell(1).getStringCellValue();
            String description = row.getCell(2).getStringCellValue();
            String parameterValue = row.getCell(3).getStringCellValue();

            if (parameterValue.startsWith("Reference"))
                continue;

            datasource.addTimedValueAttribute(new Attribute(getProvider(), label, name, description, Attribute.DataType.numeric));
        }

        return datasource;
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {
        DatasourceId datasourceId = DatasourceId.valueOf(datasource.getId());
        Workbook workbook = excelUtils.getWorkbook(
                downloadUtils.fetchInputStream(getDatasourceUrl(datasourceId), getProvider().getLabel(), DATASET_FILE_SUFFIX));
        List<TimedValue> timedValueBuffer = new ArrayList<>();

        // Loop over years
        for (int sheetId = 0; sheetId < workbook.getNumberOfSheets(); sheetId++){
            Sheet sheet = workbook.getSheetAt(sheetId);

            int year = -1;
            try {
                year = Integer.parseInt(sheet.getSheetName().substring(sheet.getSheetName().length()-4, sheet.getSheetName().length()));
            }catch (NumberFormatException e){
                // Sheetname does not end in a year
                continue;
            }

            // Create extractors for each timed value
            List<TimedValueExtractor> timedValueExtractors = new ArrayList<>();

            RowCellExtractor subjectExtractor = new RowCellExtractor(0, Cell.CELL_TYPE_STRING);
            ConstantExtractor timestampExtractor = new ConstantExtractor(String.valueOf(year));

            // Get the attribute label row and create TimedValueExtractors
            Row attributeLabelRow = sheet.getRow(5);
            for (int columnId = 0; columnId < attributeLabelRow.getLastCellNum(); columnId++){
                RowCellExtractor tmpAttributeLabelExtractor = new RowCellExtractor(columnId,Cell.CELL_TYPE_STRING);
                tmpAttributeLabelExtractor.setRow(attributeLabelRow);
                Attribute attribute = AttributeUtils.getByProviderAndLabel(getProvider(), tmpAttributeLabelExtractor.extract());
                if (attribute != null){
                    ConstantExtractor attributeExtractor = new ConstantExtractor(attribute.getLabel());
                    RowCellExtractor valueExtractor = new RowCellExtractor(columnId, Cell.CELL_TYPE_NUMERIC);
                    timedValueExtractors.add(new TimedValueExtractor(getProvider(), subjectType, subjectExtractor, attributeExtractor, timestampExtractor, valueExtractor));
                }
            }

            // Extract timed values
            excelUtils.extractAndSaveTimedValues(sheet, this, timedValueExtractors, BUFFER_THRESHOLD);
        }
    }

    private static URL getDatasourceUrl(DatasourceId datasourceId) throws MalformedURLException {
        return new URL(datasetFiles[datasourceId.ordinal()]);
    }
}
