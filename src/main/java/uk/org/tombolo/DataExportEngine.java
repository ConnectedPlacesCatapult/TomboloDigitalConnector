package uk.org.tombolo;

import java.io.File;
import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public class DataExportEngine implements ExecutionEngine{

	String localDataPath;
	
	public static void main(String[] args) {
		
		String executionSpecPath = "executions/obesity_uk.json";
				
		DataExportEngine engine = new DataExportEngine();
		try{
			engine.executeResource(executionSpecPath);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public DataExportEngine(){
	}

	public void executeResource(String resourcePath) throws IOException, EncryptedDocumentException, InvalidFormatException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(resourcePath).getFile());
		execute(file);
	}
	
	public void execute(File specification) throws IOException, EncryptedDocumentException, InvalidFormatException{
		
		// FIXME: Read specification file
		
		// FIXME: Import data
		
		// FIXME: Export data
		
	}
	
}
