package uk.org.tombolo;

import java.io.File;

import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.DatasourceSpecification;
import uk.org.tombolo.importer.Importer;

public class DataExportEngine implements ExecutionEngine{

	String localDataPath;
	
	public static void main(String[] args) {
		
		String executionSpecPath = "executions/tombolo/obesity_uk.json";
				
		DataExportEngine engine = new DataExportEngine();
		try{
			engine.executeResource(executionSpecPath);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public DataExportEngine(){
	}

	public void executeResource(String resourcePath) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(resourcePath).getFile());
		execute(file);
	}
	
	public void execute(File specification) throws Exception {
		
		// Read specification file
		DatasetSpecification datasetSpec = DatasetSpecification.fromJsonFile(specification);
		
		// Import data
		for (DatasourceSpecification datasourceSpec : datasetSpec.getDatasourceSpecification()){
			System.err.println("Importing " 
					+ datasourceSpec.getImporterClass() 
					+ " " + datasourceSpec.getDatasourceId());
			Importer importer = (Importer) Class.forName(datasourceSpec.getImporterClass()).newInstance();
			importer.importDatasource(datasourceSpec.getDatasourceId());
		}
		
		// FIXME: Export data
		System.err.println("Exporting ...");
		
	}
	
}
