package uk.org.tombolo.importer.utils.extraction;

import org.apache.poi.ss.usermodel.Row;

import static org.apache.poi.ss.usermodel.Cell.*;

public class RowCellExtractor implements SingleValueExtractor {
    private int columnId;
    private Integer cellType;
    private Row row;

    public RowCellExtractor(int columnId, int cellType){
        this.columnId = columnId;
        this.cellType = cellType;
    }

    public void setRow(Row row){
        this.row = row;
    }

    @Override
    public String extract() throws ExtractorException {
        if (row.getCell(columnId) == null)
            throw new ExtractorException("Column with index "+columnId+" does not exit");
        if (row.getCell(columnId).getCellType() == CELL_TYPE_BLANK)
            throw new ExtractorException("Empty cell value");
        try{
            switch (cellType) {
                case CELL_TYPE_BOOLEAN:
                    return String.valueOf(row.getCell(columnId).getBooleanCellValue());
                case CELL_TYPE_NUMERIC:
                    return String.valueOf(row.getCell(columnId).getNumericCellValue());
                case CELL_TYPE_STRING:
                    return String.valueOf(row.getCell(columnId).getStringCellValue());
                default:
                    throw new ExtractorException("Unhandled cell type: "+cellType);
            }
        }catch (IllegalStateException e){
            // Most likely trying to read a non-numeric value like '--'
            throw new ExtractorException("Could not extract value", e);
        }
    }
}
