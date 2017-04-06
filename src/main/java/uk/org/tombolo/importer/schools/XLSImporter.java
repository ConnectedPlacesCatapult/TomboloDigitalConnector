package uk.org.tombolo.importer.schools;

import org.apache.poi.ss.usermodel.*;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.GeneralImporter;
import uk.org.tombolo.importer.utils.ExcelUtils;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Importer class common for all the xls format datasets.
 */
public abstract class XLSImporter extends GeneralImporter {

    // Excel only
    Workbook workbook;

    @Override
    protected void setupUtils(Datasource datasource) throws Exception {
        ExcelUtils excelUtils = new ExcelUtils(downloadUtils);
        workbook = excelUtils.getWorkbook(datasource);
    }

    @Override
    protected int importDatasource(Datasource datasource) throws Exception {
        saveDatasourceMetadata(datasource);

        List<Subject> subjects = new ArrayList<>();
        List<FixedValue> fixedValues = new ArrayList<FixedValue>();

        Iterator<Row> rowIterator = workbook.getSheetAt(0).rowIterator();
        DataFormatter dataFormatter = new DataFormatter();
        Row header = rowIterator.next();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            String label;
            String name;

            try {
                label = datasource.getProvider().getLabel() + "_schools_" + row.getCell(0).toString();
                name = row.getCell(4).toString();

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

        SubjectUtils.save(subjects);
        FixedValueUtils.save(fixedValues);

        return 0;
    }

    protected List<Attribute> getFixedValuesAttributes(DataSourceID dataSourceID) {
        List<Attribute> attributes = new ArrayList<>();

        Row attributeHeader = workbook.getSheetAt(dataSourceID.getSheet()).rowIterator().next();
        IntStream.rangeClosed(attributeHeader.getFirstCellNum(), attributeHeader.getLastCellNum() - 1)
                .forEach(idx -> {
                            String name = attributeHeader.getCell(idx).getStringCellValue();
                            attributes.add(new Attribute(getProvider(), AttributeUtils.nameToLabel(name),
                                    name, name, Attribute.DataType.string));
                        }
                );
        return attributes;
    }
}
