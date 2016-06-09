package uk.org.tombolo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.execution.spec.*;
import uk.org.tombolo.exporter.Exporter;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.importer.DownloadUtils;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.transformer.Transformer;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class DataExportEngine implements ExecutionEngine{
	private static final Logger log = LoggerFactory.getLogger(DataExportEngine.class);
	private static DownloadUtils downloadUtils;

	DataExportEngine(DownloadUtils downloadUtils) {
		this.downloadUtils = downloadUtils;
	}
	
	public void execute(DataExportSpecification dataExportSpec, Writer writer, boolean forceImport) throws Exception {
		// Import data
		if (forceImport) {
			for (DatasourceSpecification datasourceSpec : dataExportSpec.getDatasetSpecification().getDatasourceSpecification()) {
				log.info("Importing {} {}",
						datasourceSpec.getImporterClass(),
						datasourceSpec.getDatasourceId());
				Importer importer = (Importer) Class.forName(datasourceSpec.getImporterClass()).newInstance();
				importer.setDownloadUtils(downloadUtils);
				int count = importer.importDatasource(datasourceSpec.getDatasourceId());
				log.info("Imported {} values", count);
			}
		}

		// Generate fields
		List<FieldSpecification> fieldSpecs = dataExportSpec.getDatasetSpecification().getFieldSpecification();
		List<Field> fields = new ArrayList<>();
		for (FieldSpecification fieldSpec : fieldSpecs) {
			fields.add((Field) Class.forName(fieldSpec.getFieldClass()).newInstance());
		}


		if (fields.size() > 0) {
			// Use the new fields method
			log.info("Exporting ...");
			Exporter exporter = (Exporter) Class.forName(dataExportSpec.getExporterClass()).newInstance();
			List<SubjectSpecification> subjectSpecList = dataExportSpec.getDatasetSpecification().getSubjectSpecification();
			List<Subject> subjects = SubjectUtils.getSubjectBySpecification(subjectSpecList.get(0));
			exporter.write(writer, subjects, fields);
		} else {
			// Run transforms over subjects
			List<SubjectSpecification> subjectSpecList = dataExportSpec.getDatasetSpecification().getSubjectSpecification();
			for (SubjectSpecification subjectSpec : subjectSpecList) {
				List<Subject> subjects = SubjectUtils.getSubjectBySpecification(subjectSpec);
				for (TransformSpecification transformSpec : dataExportSpec.getDatasetSpecification().getTransformSpecification()) {
					log.info("Running transformation to generate {}", transformSpec.getOutputAttribute().getName());
					Transformer transformer = (Transformer) Class.forName(transformSpec.gettransformerClass()).newInstance();
					transformer.setTimedValueUtils(new TimedValueUtils());
					transformer.transformBySpecification(subjects, transformSpec);
				}
			}

			// Export data
			log.info("Exporting ...");
			Exporter exporter = (Exporter) Class.forName(dataExportSpec.getExporterClass()).newInstance();
			exporter.write(writer, dataExportSpec.getDatasetSpecification());
		}
	}
}
