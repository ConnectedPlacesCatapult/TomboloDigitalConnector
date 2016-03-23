package uk.org.tombolo;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.tombolo.core.utils.HibernateUtil;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.DatasourceSpecification;
import uk.org.tombolo.importer.Importer;

public class DataExportEngine implements ExecutionEngine{

	String localDataPath;
	
	private static final Logger log = LoggerFactory.getLogger(DataExportEngine.class);
	
	public static void main(String[] args) {
		
		String executionSpecPath = args[0];
				
		DataExportEngine engine = new DataExportEngine();
		try{
			File file = new File(executionSpecPath);
			if (file.exists()){
				engine.execute(file);
			}else{
				log.error("File not found: " + executionSpecPath);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		log.info("exit");
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
			log.info("Importing {} {}", 
					datasourceSpec.getImporterClass(),
					datasourceSpec.getDatasourceId());
			Importer importer = (Importer) Class.forName(datasourceSpec.getImporterClass()).newInstance();
			int count = importer.importDatasource(datasourceSpec.getDatasourceId());
			log.info("Imported {} values", count);
		}
		
		// FIXME: Export data
		log.info("Exporting ...");
		
		// Closing the Hibernate Session Factory
		HibernateUtil.shutdown();
	}
	
}
