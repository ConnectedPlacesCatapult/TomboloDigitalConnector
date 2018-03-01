package uk.org.tombolo.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.AbstractRunner;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.exporter.Exporter;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.ParentField;
import uk.org.tombolo.field.modelling.ModellingField;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.DownloadUtils;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.ImporterMatcher;
import uk.org.tombolo.importer.utils.ConfigUtils;
import uk.org.tombolo.recipe.DataExportRecipe;
import uk.org.tombolo.recipe.DatasourceRecipe;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.recipe.SubjectRecipe;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DataExportEngine implements ExecutionEngine {
	private static final Logger log = LoggerFactory.getLogger(DataExportEngine.class);
	private static DownloadUtils downloadUtils;
	private static Properties apiKeys;
	private FieldCache fieldCache;

	public DataExportEngine(Properties apiKeys, DownloadUtils downloadUtils) {
		this.apiKeys = apiKeys;
		this.downloadUtils = downloadUtils;
		fieldCache = new FieldCache();
	}

	public void execute(DataExportRecipe dataExportRecipe, Writer writer, ImporterMatcher forceImports) throws Exception {
		List<SubjectRecipe> subjectRecipes = dataExportRecipe.getDataset().getSubjects();
		// Import datasources that are in the global dataset specification
		for (DatasourceRecipe datasourceSpec : dataExportRecipe.getDataset().getDatasources()) {
			if (!datasourceSpec.getImporterClass().isEmpty()) {
				importDatasource(forceImports, datasourceSpec, subjectRecipes);
			}
		}

		// Generate fields
		List<FieldRecipe> fieldSpecs = dataExportRecipe.getDataset().getFields();
		List<Field> fields = new ArrayList<>();
		for (FieldRecipe fieldSpec : fieldSpecs) {
			Field field = fieldSpec.toField();
			field.setFieldCache(fieldCache);
			fields.add(field);
		}

		prepareFields(fields, dataExportRecipe.getDataset().getSubjects(), forceImports);

		// Use the new fields method
		log.info("Exporting ...");
		List<SubjectRecipe> subjectSpecList = subjectRecipes;
		Exporter exporter = (Exporter) Class.forName(dataExportRecipe.getExporter()).newInstance();
		List<Subject> subjects = SubjectUtils.getSubjectBySpecifications(subjectSpecList);
		exporter.write(writer, subjects, fields, dataExportRecipe.getTimeStamp());
	}

	private void prepareFields(List<Field> fields, List<SubjectRecipe> subjectRecipes, ImporterMatcher forceImports) throws Exception {
		// Import datasources that are specified as part of a predefined field
		for (Field field : fields) {
			if (field instanceof ModellingField) {
				// This is a predefined field and hence we need to import the appropriate datasources
				for (DatasourceRecipe datasourceRecipe : ((ModellingField) field).getDatasources()) {
					importDatasource(forceImports, datasourceRecipe, subjectRecipes);
				}
			}

			if (field instanceof ParentField) {
				// This is a parent field and hence we need to prepare its children
				prepareFields(((ParentField) field).getChildFields(), subjectRecipes, forceImports);
			}
		}
	}


	private void importDatasource(ImporterMatcher forceImports, DatasourceRecipe datasourceRecipe, List<SubjectRecipe> subjectRecipes) throws Exception {

		Importer importer = initialiseImporter(datasourceRecipe.getImporterClass(), datasourceRecipe.getConfigFile());
		importer.configure(apiKeys);
		importer.setDownloadUtils(downloadUtils);
		importer.setSubjectRecipes(subjectRecipes);
		importer.importDatasource(
				datasourceRecipe.getDatasourceId(),
				datasourceRecipe.getGeographyScope(),
				datasourceRecipe.getTemporalScope(),
				datasourceRecipe.getLocalData(),
				forceImports.doesMatch(datasourceRecipe.getImporterClass())
		);
	}

	private Importer initialiseImporter(String importerClass, String configFile) throws Exception {
		if (configFile != null && !"".equals(configFile)) {
			Config importerConfiguration = ConfigUtils.loadConfig(
					AbstractRunner.loadProperties("Configuration file", configFile));
			return (Importer) Class.forName(importerClass).getDeclaredConstructor(Config.class).newInstance(importerConfiguration);

		}
		return (Importer) Class.forName(importerClass).getDeclaredConstructor().newInstance();
	}
}
