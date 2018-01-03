package uk.org.tombolo.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.AbstractRunner;
import uk.org.tombolo.DataExportRunner;
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
import uk.org.tombolo.importer.utils.JSONReader;
import uk.org.tombolo.recipe.DataExportRecipe;
import uk.org.tombolo.recipe.DatasourceRecipe;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.recipe.SubjectRecipe;

import java.io.File;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

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

	public void execute(DataExportRecipe dataExportSpec, Writer writer) throws Exception {
		execute(dataExportSpec, writer, new ImporterMatcher(""));
	}

	public void execute(DataExportRecipe dataExportSpec, Writer writer, ImporterMatcher forceImports) throws Exception {

		//Check for Valid Providers
		String vProvider = verifyProvider();
		if (null != vProvider) {
			throw new Error(vProvider + " is not recognised as a valid Provider Name");
		}

		// Import datasources that are in the global dataset specification
		for (DatasourceRecipe datasourceSpec : dataExportSpec.getDataset().getDatasources()) {
			importDatasource(forceImports, datasourceSpec, dataExportSpec.getDataset().getSubjects());
		}

		// Generate fields
		List<FieldRecipe> fieldSpecs = dataExportSpec.getDataset().getFields();
		List<Field> fields = new ArrayList<>();
		for (FieldRecipe fieldSpec : fieldSpecs) {
			Field field = fieldSpec.toField();
			field.setFieldCache(fieldCache);
			fields.add(field);
		}

		prepareFields(fields, dataExportSpec.getDataset().getSubjects(), forceImports);

		// Use the new fields method
		log.info("Exporting ...");
		List<SubjectRecipe> subjectSpecList = dataExportSpec.getDataset().getSubjects();
		Exporter exporter = (Exporter) Class.forName(dataExportSpec.getExporter()).newInstance();
		List<Subject> subjects = SubjectUtils.getSubjectBySpecifications(subjectSpecList);
		exporter.write(writer, subjects, fields, dataExportSpec.getTimeStamp());
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


	private void importDatasource(ImporterMatcher forceImports, DatasourceRecipe datasourceSpec, List<SubjectRecipe> subjectRecipes) throws Exception {

		Importer importer = initialiseImporter(datasourceSpec.getImporterClass(), datasourceSpec.getConfigFile());
		importer.configure(apiKeys);
		importer.setDownloadUtils(downloadUtils);
		importer.importDatasource(
				datasourceSpec.getDatasourceId(),
				datasourceSpec.getGeographyScope(),
				datasourceSpec.getTemporalScope(),
				datasourceSpec.getLocalData(),
				subjectRecipes,
				forceImports.doesMatch(datasourceSpec.getImporterClass())
		);
	}

	private Importer initialiseImporter(String importerClass, String configFile) throws Exception {
		Config importerConfiguration = null;
		if (configFile != null && !"".equals(configFile)) {
			importerConfiguration = ConfigUtils.loadConfig(
					AbstractRunner.loadProperties("Configuration file", configFile));

		}
		return (Importer) Class.forName(importerClass).getDeclaredConstructor(Config.class).newInstance(importerConfiguration);
	}

	private String verifyProvider() throws Exception {
		String validProvider = null;

		File specificationFile = new File(DataExportRunner.executionSpecPath);
		if (!specificationFile.exists()) return validProvider;
		JSONReader reader = new JSONReader(specificationFile, new ArrayList<>(Arrays.asList("importerClass", "provider")));
		reader.getData();
		List<String> importers = reader.getTagValueFromAllSections("importerClass").stream().distinct().collect(Collectors.toList());
		List<String> providers = reader.getTagValueFromAllSections("provider").stream().distinct().collect(Collectors.toList());

		mainLoop:
		for (String importer : importers) {
			if (providers.isEmpty()) break mainLoop;

			for (int i = 0; i < providers.size(); i++) {
				Importer imp = initialiseImporter(importer, "");
				if (providers.get(i).equals(imp.getProvider().getLabel())) {
					providers.remove(i);
					i = i - 1;
				}
			}
		}

		if (providers.size() > 0) validProvider = String.join(", ", providers);

		return validProvider;
	}
}
