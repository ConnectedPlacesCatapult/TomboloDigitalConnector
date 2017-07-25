package uk.org.tombolo.importer.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.utils.extraction.CSVExtractor;
import uk.org.tombolo.importer.utils.extraction.ExtractorException;
import uk.org.tombolo.importer.utils.extraction.TimedValueExtractor;
import uk.org.tombolo.importer.utils.extraction.UnknownSubjectLabelException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities class for importing CSV files
 */
public class CSVUtils {

    public static void extractAndSaveTimedValues(List<TimedValueExtractor> extractors, Importer importer, File localFile)
            throws IOException, ExtractorException {

        String line = null;
        BufferedReader br = new BufferedReader(new FileReader(localFile));
        List<TimedValue> timedValueBuffer = new ArrayList<>();
        while ((line = br.readLine())!=null) {
            CSVParser parser = CSVParser.parse(line, CSVFormat.DEFAULT);
            List<CSVRecord> records = parser.getRecords();
            for(TimedValueExtractor extractor: extractors){
                if (extractor.getSubjectLabelExtractor() instanceof CSVExtractor)
                    ((CSVExtractor) extractor.getSubjectLabelExtractor()).setCsvRecord(records.get(0));
                if (extractor.getAttributeLabelExtractor() instanceof CSVExtractor)
                    ((CSVExtractor) extractor.getAttributeLabelExtractor()).setCsvRecord(records.get(0));
                if (extractor.getTimestampExtractor() instanceof CSVExtractor)
                    ((CSVExtractor) extractor.getTimestampExtractor()).setCsvRecord(records.get(0));
                if (extractor.getValueExtractor() instanceof CSVExtractor)
                    ((CSVExtractor) extractor.getValueExtractor()).setCsvRecord(records.get(0));
                try {
                    timedValueBuffer.add(extractor.extract());
                }catch (UnknownSubjectLabelException e){
                    // No reason to panic even if Subject does not exist and no reason to run the rest of the extractors
                    // Keep Calm and Break
                    break;
                }
            }
        }
        br.close();
        importer.saveAndClearTimedValueBuffer(timedValueBuffer);
    }
}
