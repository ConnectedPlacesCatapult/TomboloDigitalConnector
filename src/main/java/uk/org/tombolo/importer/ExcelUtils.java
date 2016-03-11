package uk.org.tombolo.importer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import uk.org.tombolo.core.Datasource;

public class ExcelUtils {
	private DownloadUtils downloadUtils = new DownloadUtils();
	
	public Workbook getWorkbook(Datasource datasource) throws MalformedURLException, IOException, EncryptedDocumentException, InvalidFormatException{
		File localDatafile = downloadUtils.getDatasourceFile(datasource);		
		Workbook wb = null;
		wb = WorkbookFactory.create(localDatafile);
		return wb;
	}

	
}
