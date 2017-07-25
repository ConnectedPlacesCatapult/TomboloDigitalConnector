package uk.org.tombolo.importer.dfe;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.DataSourceID;
import uk.org.tombolo.importer.utils.ExcelUtils;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Class importing schools in England
 *
 * Data sourced here: https://www.gov.uk/government/publications/schools-in-england
 * NOTE the file containing the schools is updated monthly (in theory).
 */
public class SchoolsImporter extends AbstractDfEImporter {

    // Column index for the subject label builder
    private static final int LABEL_COLUMN_INDEX = 0;
    // Column index for the subject name
    private static final int NAME_COLUMN_INDEX = 4;

    // Method used to get the dataset if it is actually updated monthly
    public static String getFormattedMonthYear() {
        DateTimeFormatter dft = DateTimeFormatter.ofPattern("MMMM_yyyy");
        LocalDate localDate = LocalDate.now();
        return dft.format(localDate).toString();
    }

    public enum schoolsDataSourceID {
        schools(new DataSourceID(
                "schools",
                "Schools in England",
                "Schools in England",
                "https://www.gov.uk/government/publications/schools-in-england/",
                "https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/597965/EduBase_Schools_April_2017.xlsx"
                ),
                0
        );

        private DataSourceID dataSourceID;
        private int sheetIdx;

        schoolsDataSourceID(DataSourceID dataSourceID, int sheetIdx) {
            this.dataSourceID = dataSourceID;
            this.sheetIdx = sheetIdx;
        }
    }

    public SchoolsImporter(Config config) {
        super(config);
        datasourceIds = stringsFromEnumeration(schoolsDataSourceID.class);
    }

    @Override
    public Datasource getDatasource(String datasourceId) throws Exception {
        DataSourceID id;
        try {
            id = schoolsDataSourceID.valueOf(datasourceId).dataSourceID;
        } catch (IllegalArgumentException e) {
            throw new Error("Unknown DataSourceID " + datasourceId);
        }
        return getDatasource(getClass(), id);
    }

    // Schools' workbook
    Workbook workbook;

    @Override
    protected void setupUtils(Datasource datasource) throws Exception {
        ExcelUtils excelUtils = new ExcelUtils();
        workbook = excelUtils.getWorkbook(downloadUtils.fetchInputStream(new URL(datasource.getRemoteDatafile()), getProvider().getLabel(), ".xlsx"));
    }

    @Override
    protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {

        List<Subject> subjects = new ArrayList<>();
        List<FixedValue> fixedValues = new ArrayList<FixedValue>();

        Iterator<Row> rowIterator = workbook.getSheetAt(schoolsDataSourceID.schools.sheetIdx).rowIterator();
        DataFormatter dataFormatter = new DataFormatter();
        Row header = rowIterator.next();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            String label;
            String name;

            try {
                label = datasource.getProvider().getLabel() + "_schools_" + row.getCell(LABEL_COLUMN_INDEX).toString();
                name = row.getCell(NAME_COLUMN_INDEX).toString();

            } catch (Exception e) {
                // Continue with the other data, if any.
                continue;
            }

            Subject subject = new Subject(datasource.getUniqueSubjectType(), label, name,null);
            subjects.add(subject);

            int attributeIndex = 0;
            for (Attribute attribute : datasource.getFixedValueAttributes()) {
                fixedValues.add(new FixedValue(
                        subject,
                        attribute,
                        dataFormatter.formatCellValue(row.getCell(attributeIndex++))));
            }
        }
        saveAndClearSubjectBuffer(subjects);
        saveAndClearFixedValueBuffer(fixedValues);
    }

    protected List<Attribute> getFixedValuesAttributes(DataSourceID dataSourceID) {
        List<Attribute> attributes = new ArrayList<>();

        Row attributeHeader = workbook.getSheetAt(schoolsDataSourceID.schools.sheetIdx).rowIterator().next();
        IntStream.rangeClosed(attributeHeader.getFirstCellNum(), attributeHeader.getLastCellNum() - 1)
                .forEach(idx -> {
                            String name = attributeHeader.getCell(idx).getStringCellValue();
                            attributes.add(new Attribute(getProvider(), AttributeUtils.nameToLabel(name),
                                    name, name, Attribute.DataType.string));
                        }
                );
        return attributes;
    }

    protected List<SubjectType> getSubjectTypes(DataSourceID dataSourceID){
        return Arrays.asList(new SubjectType(getProvider(), dataSourceID.getLabel(), dataSourceID.getName()));
    }
}
