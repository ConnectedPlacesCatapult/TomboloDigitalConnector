package uk.org.tombolo.importer.utils.extraction;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.hamcrest.core.StringStartsWith;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class RowCellExtractorTest extends AbstractExtractorTest {

    Workbook workbook = makeDummyWorkbook();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void extract() throws Exception {
        RowCellExtractor extractor = new RowCellExtractor(1, CellType.NUMERIC);

        extractor.setRow(workbook.getSheet("sheet").getRow(0));
        assertEquals("5.0", extractor.extract());

        extractor.setRow(workbook.getSheet("sheet").getRow(1));
        assertEquals("6.0", extractor.extract());
    }

    @Test
    public void extractBlankValue() throws Exception {
        RowCellExtractor extractor = new RowCellExtractor(2, CellType.BOOLEAN);

        extractor.setRow(workbook.getSheet("sheet").getRow(0));
        assertEquals("true", extractor.extract());

        extractor.setRow(workbook.getSheet("sheet").getRow(1));
        thrown.expect(BlankCellException.class);
        thrown.expectMessage("Empty cell value");
        extractor.extract();
    }

    @Test
    public void extractSillyValue() throws Exception {

        RowCellExtractor extractor = new RowCellExtractor(3, CellType.NUMERIC);

        extractor.setRow(workbook.getSheet("sheet").getRow(0));
        thrown.expect(BlankCellException.class);
        thrown.expectMessage(new StringStartsWith("Could not extract value"));
        extractor.extract();

        extractor.setRow(workbook.getSheet("sheet").getRow(1));
        assertEquals("7.0", extractor.extract());

    }

    @Test
    public void extractUnhandledCellType() throws Exception {

        RowCellExtractor extractor = new RowCellExtractor(4, CellType.FORMULA);

        extractor.setRow(workbook.getSheet("sheet").getRow(0));
        thrown.expect(ExtractorException.class);
        thrown.expectMessage(new StringStartsWith("Unhandled cell type"));
        extractor.extract();
    }

    @Test
    public void extractNonExistingColumn() throws Exception {

        RowCellExtractor extractor = new RowCellExtractor(4, CellType.FORMULA);

        extractor.setRow(workbook.getSheet("sheet").getRow(1));
        thrown.expect(ExtractorException.class);
        thrown.expectMessage(new StringStartsWith("Column with index 4 does not exit"));
        extractor.extract();
    }


}