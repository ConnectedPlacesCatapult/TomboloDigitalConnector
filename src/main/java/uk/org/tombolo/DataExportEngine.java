package uk.org.tombolo;

import java.io.Writer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.utils.*;
import uk.org.tombolo.execution.spec.DataExportSpecification;
import uk.org.tombolo.execution.spec.DatasourceSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;
import uk.org.tombolo.execution.spec.TransformSpecification;
import uk.org.tombolo.exporter.Exporter;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.transformer.Transformer;

public class DataExportEngine implements ExecutionEngine{
	private static final Logger log = LoggerFactory.getLogger(DataExportEngine.class);
	
	public void execute(DataExportSpecification dataExportSpec, Writer writer, boolean forceImport) throws Exception {
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

		// Run transforms over geographies
		List<GeographySpecification> geographySpecList = dataExportSpec.getDatasetSpecification().getGeographySpecification();
		for (GeographySpecification geographySpec : geographySpecList) {
			List<Geography> geographies = GeographyUtils.getGeographyBySpecification(geographySpec);
			for (TransformSpecification transformSpec : dataExportSpec.getDatasetSpecification().getTransformSpecification()) {
				log.info("Running transformation to generate {}", transformSpec.getOutputAttribute().getName());
				Transformer transformer = (Transformer) Class.forName(transformSpec.gettransformerClass()).newInstance();
				transformer.setTimedValueUtils(new TimedValueUtils());
				transformer.transformBySpecification(geographies, transformSpec);
			}
		}

		// Export data
		log.info("Exporting ...");
		Exporter exporter = (Exporter) Class.forName(dataExportSpec.getExporterClass()).newInstance();
		exporter.write(writer, dataExportSpec.getDatasetSpecification());
	}
}
