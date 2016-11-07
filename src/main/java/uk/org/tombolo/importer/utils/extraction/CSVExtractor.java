package uk.org.tombolo.importer.utils.extraction;

import org.apache.commons.csv.CSVRecord;

/**
 * Extractor for extracting a value from a CSV row
 */
public class CSVExtractor implements SingleValueExtractor {
    private int columnId;
    private CSVRecord csvRecord;

    public CSVExtractor(int columnId){
        this.columnId = columnId;
    }

    public void setCsvRecord(CSVRecord csvRecord){
        this.csvRecord = csvRecord;
    }

    @Override
    public String extract() throws ExtractorException {
        return csvRecord.get(columnId);
    }
}
