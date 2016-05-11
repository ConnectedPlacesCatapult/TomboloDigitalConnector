package uk.org.tombolo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.utils.GeographyUtils;
import uk.org.tombolo.core.utils.HibernateUtil;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.execution.spec.DataExportSpecification;
import uk.org.tombolo.execution.spec.DatasourceSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;
import uk.org.tombolo.execution.spec.TransformSpecification;
import uk.org.tombolo.exporter.Exporter;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.transformer.Transformer;

public class DataExportEngine implements ExecutionEngine{

	private String outputFile;
	
	private static final Logger log = LoggerFactory.getLogger(DataExportEngine.class);
	
	public static void main(String[] args) {
		
		if (args.length != 3){
			log.error("Use: {} {} {} {}",
					DataExportEngine.class.getCanonicalName(),
					"dataExportSpecFile",
					"outputFile",
					"forceImport"
					);
			System.exit(1);
		}
		
		String executionSpecPath = args[0];
		String outputFile = args[1];
		boolean forceImport = Boolean.parseBoolean(args[2]);

		File file = new File(executionSpecPath);
		if (!file.exists()){
			log.error("File not found: " + executionSpecPath);
			System.exit(1);
		}

		DataExportEngine engine = new DataExportEngine(outputFile);
		try{
			engine.execute(file, forceImport);
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			// Closing the Hibernate Session Factory
			HibernateUtil.shutdown();
		}
		log.info("exit");
	}
	
	public DataExportEngine(String outputFile){
		this.outputFile = outputFile;
	}

	public void executeResource(String resourcePath, boolean forceImport) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(resourcePath).getFile());
		execute(file, forceImport);
	}
	
	public void execute(File specification, boolean forceImport) throws Exception {
		
		// Read specification file
		DataExportSpecification dataExportSpec = DataExportSpecification.fromJsonFile(specification);
		
		// Import data
		if (forceImport){
			for (DatasourceSpecification datasourceSpec : dataExportSpec.getDatasetSpecification().getDatasourceSpecification()){
				log.info("Importing {} {}", 
						datasourceSpec.getImporterClass(),
						datasourceSpec.getDatasourceId());
				Importer importer = (Importer) Class.forName(datasourceSpec.getImporterClass()).newInstance();
				int count = importer.importDatasource(datasourceSpec.getDatasourceId());
				log.info("Imported {} values", count);
			}
		}

		for (TransformSpecification transformSpec : dataExportSpec.getDatasetSpecification().getTransformSpecification()) {
			Transformer transformer = (Transformer) Class.forName(transformSpec.getTransformClass()).newInstance();
			transformer.setTimedValueUtils(new TimedValueUtils());
			List<GeographySpecification> geographySpecList = dataExportSpec.getDatasetSpecification().getGeographySpecification();
			for (GeographySpecification geographySpec : geographySpecList ) {
				List<Geography> geographies = GeographyUtils.getGeographyBySpecification(geographySpec);
				transformer.transform(geographies, transformSpec.getInputAttributes(), transformSpec.getOutputAttribute());
			}
		}
		
		// Export data
		log.info("Exporting ...");
		Writer writer = null;
		try {
			writer = new FileWriter(outputFile);
		} catch (IOException e) {
			log.error("Error initilising output writer: {}", outputFile);
		}
		Exporter exporter = (Exporter) Class.forName(dataExportSpec.getExporterClass()).newInstance();
		exporter.write(writer, dataExportSpec.getDatasetSpecification());
		writer.flush();
		writer.close();
	}
}
