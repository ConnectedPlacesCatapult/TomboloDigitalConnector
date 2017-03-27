package uk.org.tombolo.importer.schools;

import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry;
import com.sun.tools.doclint.HtmlTag;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.annotations.common.util.impl.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.SubjectType;
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
        /*
        List<Subject> subjects = new ArrayList<Subject>();
        List<FixedValue> fixedValues = new ArrayList<FixedValue>();

        foreach row in file
            Subject subject = new Subject(datasource.getUniqueSubjectType(), label, name, point: null);
            subjects.add(subject);

            foreach attribute in datasource.getAllFixedValueAttributes
                fixedValues.add(new FixedValue(subject, attribute, attributeExtractor.extract()));

        SubjectUtils.save(subjects);
        FixedValueUtils.save(fixedValues);

         */
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

    // set to be only two columns: label and description
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
