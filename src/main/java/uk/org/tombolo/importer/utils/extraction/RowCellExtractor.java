package uk.org.tombolo.importer.utils.extraction;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

public class RowCellExtractor implements SingleValueExtractor {
    private int columnId;
    private CellType cellType;
    private Row row;

    public RowCellExtractor(int columnId, CellType cellType){
        this.columnId = columnId;
        this.cellType = cellType;
    }

    public void setRow(Row row){
        this.row = row;
    }

    @Override
    public String extract() throws ExtractorException {
        if (row == null)
            throw new BlankCellException("Empty row");
        if (row.getCell(columnId) == null)
            throw new ExtractorException("Column with index "+columnId+" does not exit");
        if (row.getCell(columnId).getCellTypeEnum() == CellType.BLANK)
            throw new BlankCellException("Empty cell value");
        try{
            switch (cellType) {
                case BOOLEAN:
                    return String.valueOf(row.getCell(columnId).getBooleanCellValue());
                case NUMERIC:
                    return String.valueOf(row.getCell(columnId).getNumericCellValue());
                case STRING:
                    return String.valueOf(row.getCell(columnId).getStringCellValue());
                default:
                    throw new ExtractorException("Unhandled cell type: "+cellType);
            }
        }catch (IllegalStateException e){
            // Most likely trying to read a non-numeric value like '--'
            // Hence we treat this as a blank cell
            throw new BlankCellException("Could not extract value", e);
        }
    }
}
