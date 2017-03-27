package uk.org.tombolo.importer.schools;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.ss.usermodel.*;
import org.hibernate.annotations.common.util.impl.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.utils.ExcelUtils;
import uk.org.tombolo.importer.utils.extraction.ExtractorException;
import uk.org.tombolo.importer.utils.extraction.RowCellExtractor;
import uk.org.tombolo.importer.utils.extraction.SingleValueExtractor;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

/**
 * Importer class common for all the xls format datasets.
 */
public abstract class XLSImporter extends AbstractImporter {
// @TODO
    /*
    Ideally have a general class for all excel formats and in the specific ones specify only:
    - Sheets to consider
    - datasourceID
    - attributes mapped to a column number or just a column number

    This would probably mean that the representation of the datasources and attributes might change
    to something other than enums.
     */

    // Excel only
    Workbook workbook;

    public interface DataSourceID {
        public String getEnumConstantName();
        public String getName();
        public String getDescription();
        public String getUrl();
        public String getRemoteDataFile();
        public int getSheet();
    }

    public interface AttributeID {
        public String getName();
        public String getDescription();
        public int columnID();
        public Attribute.DataType getType();
    }

    public <T extends Enum<T> & AttributeID> Datasource getDatasource(
            Class<? extends Importer> importerClass, DataSourceID dataSourceID, Class<T> attributeID) throws Exception {
        Datasource dataSource = new Datasource(importerClass,
                dataSourceID.getEnumConstantName(),
                getProvider(),
                dataSourceID.getName(),
                dataSourceID.getDescription()
        );
        dataSource.setUrl(dataSourceID.getUrl());
        dataSource.setRemoteDatafile(dataSourceID.getRemoteDataFile());

        //add subject type, in this case is similar to the datasource
        dataSource.addSubjectType(new SubjectType(dataSourceID.getEnumConstantName(), dataSourceID.getName()));

        // Get the attributes iterating through the file rows
        /* @FIXME could do this in other method so getDatasource can be use for all formats and also include the
        other types of attributes */
        workbook = getWorkbook(dataSource, dataSourceID.getSheet());

        //dataSource.addAllTimedValueAttributes(getPreCompiledAttributes(attributeID, dataSource));
        dataSource.addAllFixedValueAttributes(getAttributes(dataSourceID.getSheet()));

        return dataSource;
    }

    @Override
    protected int importDatasource(Datasource datasource) throws Exception {
        List<Subject> subjects = new ArrayList<Subject>();
        List<FixedValue> fixedValues = new ArrayList<FixedValue>();

        Iterator<Row> rowIterator = workbook.getSheetAt(1).rowIterator();
        DataFormatter dataFormatter = new DataFormatter();
        Row header = rowIterator.next();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            String label = datasource.getProvider().getLabel() + "_schools_" + row.getCell(0).toString();
            String name = row.getCell(4).getStringCellValue();

            Subject subject = new Subject(datasource.getUniqueSubjectType(), label, name,null);
            subjects.add(subject);

            // Maybe find another way to get the index
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

    private <T extends Enum<T> & AttributeID> List<?> getExtractors(
            SingleValueExtractor subjectLabelExtractor, Class<T> enumerator) {
        List<Object> extractors = new ArrayList<>();

        for (T attribute : enumerator.getEnumConstants()) {
            extractors.add(getExtractor(subjectLabelExtractor, attribute));
        }

        return extractors;

    }


    private List<Attribute> getAttributes(int sheetIdx) {
        List<Attribute> attributes = new ArrayList<>();

        Row attributeHeader = workbook.getSheetAt(sheetIdx).rowIterator().next();
        IntStream.rangeClosed(attributeHeader.getFirstCellNum(), attributeHeader.getFirstCellNum())
                .forEach(idx -> {
                            String name = attributeHeader.getCell(idx).getStringCellValue();
                            attributes.add(new Attribute(getProvider(), nameToLabel(name), name, name, Attribute.DataType.string));
                        }
                );
        return attributes;
    }

    ////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////

    /**
     * Gets the attributes specified in the enumerator. Used in case we don't need to get all the attributes in the
     * dataset.
     *
     * @param enumerator attribute enumerator
     * @param datasource
     * @param <T>
     * @return
     */

    private <T extends Enum<T> & AttributeID> List<Attribute> getPreCompiledAttributes(Class<T> enumerator, Datasource datasource) {
        List<Attribute> attributes = new ArrayList<Attribute>();

        for (T attribute : enumerator.getEnumConstants()) {
            attributes.add(new Attribute(datasource.getProvider(),
                    attribute.name(),
                    attribute.getName(),
                    attribute.getDescription(),
                    attribute.getType())
            );
        }

        return attributes;
    }

    /**
     * Get the attributes from a metadata sheet if available and in a specific format.
     * The accepted format is a two columns format for the attributes' names and description
     *
     * @param metadataSheetIndex index of the metadata sheet
     * @param labelRowStart start row to parse the attributes
     * @param labelRowEnd end row to parse the attributes
     * @param labelColumnStart start column to parse the attributes
     * @param labelColumnEnd end column to parse the attributes
     * @return returns the attributes described in the metadata sheet
     * @throws Exception
     */
    private List<Attribute> getAttributes(int metadataSheetIndex, int labelRowStart, int labelRowEnd,
        int labelColumnStart, int labelColumnEnd) throws Exception {
        List<Attribute> attributes = new ArrayList<>();

        Sheet metadataSheet = workbook.getSheetAt(metadataSheetIndex);
        IntStream.rangeClosed(labelRowStart, labelRowEnd)
                .forEach(idx -> {
                        attributes.add(getSingleCellAttribute(metadataSheet, idx, labelColumnStart, labelColumnEnd));
                    }
                );
        return attributes;
    }

    private Attribute getSingleCellAttribute(Sheet metadataSheet, int idx, int nameIdx, int descriptionIdx) {
        RowCellExtractor attributeNameExtractor = new RowCellExtractor(nameIdx, Cell.CELL_TYPE_STRING);
        attributeNameExtractor.setRow(metadataSheet.getRow(idx));
        RowCellExtractor attributeDescExtractor = new RowCellExtractor(descriptionIdx, Cell.CELL_TYPE_STRING);
        attributeNameExtractor.setRow(metadataSheet.getRow(idx));

        String attributeName = "";
        String attributeDescription = "";
        try {
            attributeName = attributeNameExtractor.extract();
            attributeDescription = attributeDescExtractor.extract();
        } catch (ExtractorException e) {
            LoggerFactory.logger(XLSImporter.class).warn(e.getMessage());
        }
        String attributeLabel = nameToLabel(attributeName);

        return new Attribute(
                getProvider(),
                attributeLabel,
                attributeName,
                attributeDescription,
                Attribute.DataType.numeric
        );


    }

    ////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    // Only Excel
    private Workbook getWorkbook(Datasource datasource, int sheetIndex) throws Exception{
        ExcelUtils excelUtils = new ExcelUtils(downloadUtils);
        return excelUtils.getWorkbook(datasource);
    }

    private String nameToLabel(String name){
        return DigestUtils.md5Hex(name);
    }

    protected abstract <T extends Enum<T> & AttributeID> Object getExtractor(SingleValueExtractor subjectLabelExtractor, T attribute);
}
