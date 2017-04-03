package uk.org.tombolo.importer.utils;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.importer.DownloadUtils;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.utils.extraction.BlankCellException;
import uk.org.tombolo.importer.utils.extraction.ExtractorException;
import uk.org.tombolo.importer.utils.extraction.TimedValueExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelUtils {
	Logger log = LoggerFactory.getLogger(ExcelUtils.class);
	private DownloadUtils downloadUtils;

	public ExcelUtils(DownloadUtils downloadUtils) {
		this.downloadUtils = downloadUtils;
	}
	
	public Workbook getWorkbook(Datasource datasource) throws MalformedURLException, IOException, EncryptedDocumentException, InvalidFormatException{
		File localDatafile = downloadUtils.getDatasourceFile(datasource);
		return getWorkbook(localDatafile);
	}

	public Workbook getWorkbook(File file) throws MalformedURLException, IOException, EncryptedDocumentException, InvalidFormatException{
		return WorkbookFactory.create(file,null,true);
	}

	public Workbook getWorkbook(InputStream is) throws IOException, InvalidFormatException {
		return WorkbookFactory.create(is);
	}

	public int extractTimedValues(Sheet sheet, Importer importer, List<TimedValueExtractor> extractors, int timedValueBufferSize){
		int valueCount = 0;
		List<TimedValue> timedValueBuffer = new ArrayList<>();

		// Extract timed values
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			for (TimedValueExtractor extractor : extractors) {
				extractor.setRow(row);
				try {
					TimedValue timedValue = extractor.extract();
					timedValueBuffer.add(timedValue);
					valueCount++;
					if (valueCount % timedValueBufferSize == 0) {
						// Buffer is full ... we write values to db
						importer.saveBuffer(timedValueBuffer, valueCount);
					}
				}catch (BlankCellException e){
					// We ignore this since there may be multiple blank cells in the data without having to worry
				}catch (ExtractorException e){
					log.warn("Could not extract value: {}",e.getMessage());
				}
			}
		}
		importer.saveBuffer(timedValueBuffer, valueCount);

		return valueCount;
	}
	
}
