package uk.org.tombolo.importer.utils.extraction;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import uk.org.tombolo.AbstractTest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class AbstractExtractorTest extends AbstractTest{

    protected Workbook makeDummyWorkbook(){
        List<Object> rowSpec0 = new ArrayList<Object>();
        rowSpec0.add(new String("E01000001"));  // Cell 1
        rowSpec0.add(new Double(5.0d));         // Cell 2
        rowSpec0.add(new Boolean(true));        // Cell 3
        rowSpec0.add(new String("--"));         // Cell 4
        rowSpec0.add(new Date());               // Cell 5
        List<Object> rowSpec1 = new ArrayList<Object>();
        rowSpec1.add(new String("E01000002"));  // Cell 1
        rowSpec1.add(new Integer(6));           // Cell 2
        rowSpec1.add(null);                     // Cell 3
        rowSpec1.add(new Double(7.0d));         // Cell 4
                                                // Cell 5
        List<List<Object>> rowSpecs = new ArrayList<List<Object>>();
        rowSpecs.add(rowSpec0);
        rowSpecs.add(rowSpec1);
       return makeWorkbook("sheet", rowSpecs);
    }

    protected Workbook makeWorkbook(String sheetName, List<List<Object>> rowSpecs){
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);

        for (List<Object> rowSpec : rowSpecs) {
            int rowId = (sheet.getPhysicalNumberOfRows()==0)?0:sheet.getLastRowNum()+1;
            Row row = sheet.createRow(rowId);

            for (Object cellSpec : rowSpec) {
                // Note that sheet.getLastRowNum() and row.getLastCellNum() do not behave alike
                int cellId = (row.getPhysicalNumberOfCells()==0)?0:row.getLastCellNum();
                if (cellSpec == null) {
                    row.createCell(cellId).setCellType(Cell.CELL_TYPE_BLANK);
                    continue;
                }
                switch (cellSpec.getClass().getCanonicalName()) {
                    case "java.lang.Integer":
                        row.createCell(cellId).setCellValue((Integer)cellSpec);
                        break;
                    case "java.lang.String":
                        row.createCell(cellId).setCellValue((String)cellSpec);
                        break;
                    case "java.lang.Double":
                        row.createCell(cellId).setCellValue((Double)cellSpec);
                        break;
                    case "java.lang.Boolean":
                        row.createCell(cellId).setCellValue((Boolean)cellSpec);
                        break;
                    case "java.util.Date":
                        row.createCell(cellId).setCellValue((Date)cellSpec);
                        break;
                    case "java.util.Calendar":
                        row.createCell(cellId).setCellValue((Calendar)cellSpec);
                        break;
                    case "org.apache.poi.ss.formula.Formula":
                        row.createCell(cellId).setCellType(Cell.CELL_TYPE_FORMULA);
                        Cell cell = row.getCell(row.getLastCellNum());
                        cell.setCellFormula(((Formula)cellSpec).toString());
                        break;
                    default:
                        row.createCell(cellId).setCellType(Cell.CELL_TYPE_BLANK);
                }
            }
        }
        return workbook;
    }
}
