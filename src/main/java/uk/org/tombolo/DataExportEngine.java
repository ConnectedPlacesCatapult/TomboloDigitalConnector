package uk.org.tombolo;

import java.io.Writer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.*;
import uk.org.tombolo.execution.spec.DataExportSpecification;
import uk.org.tombolo.execution.spec.DatasourceSpecification;
import uk.org.tombolo.execution.spec.SubjectSpecification;
import uk.org.tombolo.execution.spec.TransformSpecification;
import uk.org.tombolo.exporter.Exporter;
import uk.org.tombolo.importer.DownloadUtils;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.transformer.Transformer;

public class DataExportEngine implements ExecutionEngine{
	private static final Logger log = LoggerFactory.getLogger(DataExportEngine.class);
	private static DownloadUtils downloadUtils;

	DataExportEngine(DownloadUtils downloadUtils) {
		this.downloadUtils = downloadUtils;
	}
	
	public void execute(DataExportSpecification dataExportSpec, Writer writer, boolean forceImport) throws Exception {
		// Import data
		if (forceImport){
			for (DatasourceSpecification datasourceSpec : dataExportSpec.getDatasetSpecification().getDatasourceSpecification()){
				log.info("Importing {} {}",
						datasourceSpec.getImporterClass(),
						datasourceSpec.getDatasourceId());
				Importer importer = (Importer) Class.forName(datasourceSpec.getImporterClass()).newInstance();
				importer.setDownloadUtils(downloadUtils);
				int count = importer.importDatasource(datasourceSpec.getDatasourceId());
				log.info("Imported {} values", count);
			}
		}

		// Run transforms over geographies
		List<SubjectSpecification> subjectSpecList = dataExportSpec.getDatasetSpecification().getSubjectSpecification();
		for (SubjectSpecification subjectSpec : subjectSpecList) {
			List<Subject> geographies = SubjectUtils.getSubjectBySpecification(subjectSpec);
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
