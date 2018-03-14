package uk.org.tombolo;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.importer.Importer;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Get a list of datasources for an importer
 */
public class ImporterInfoRunner extends AbstractRunner {
    private static final Logger log = LoggerFactory.getLogger(ImporterInfoRunner.class);
    private static final ImporterInfoRunner runner = new ImporterInfoRunner();

    public static void main(String[] args) throws Exception {

        boolean subjects = Boolean.parseBoolean(args[1]);
        boolean attribute = Boolean.parseBoolean(args[2]);
        boolean provider = Boolean.parseBoolean(args[3]);
        String importerClass = args[0];
        StringBuilder info = new StringBuilder("\n\n");

        if (importerClass.equals("None")) {
            CatalogueExportRunner cer = new CatalogueExportRunner();
            List<Class<? extends Importer>> dcImporters = cer.getImporterClasses();
            List<String> importerNames = dcImporters.stream()
                                        .filter(i -> !i.getCanonicalName().endsWith("PythonImporter") && !i.getCanonicalName().endsWith("GeneralCSVImporter"))
                                        .map(Class::getCanonicalName).sorted().collect(Collectors.toList());

            log.info("\n\n" + "List of Digital Connector Importers: \n\n" +
                     String.join("\n", importerNames));
            return;
        }

        Importer importer = runner.getImporter(importerClass);
        List<String> datasources = runner.getDatasourceIds(importer);
        
        for (String d : datasources) {
            info.setLength(0);
            Datasource dSource = runner.getDatasource(d, importer);
            if (null != dSource.getDatasourceSpec()) {
                info.append("\n\nDatasource Id: ")
                    .append(dSource.getDatasourceSpec().getId())
                    .append("\nImporter Class: ")
                    .append(dSource.getDatasourceSpec().getImporterClass().getCanonicalName())
                    .append("\nData Url: ")
                    .append(dSource.getDatasourceSpec().getUrl()).append("\n");
                
                if (provider) {
                    info.append("Provider: ")
                        .append(runner.getProvider(importer)).append("\n");
                }       

                if (subjects) {
                    info.append("SubjectTypes: ")
                        .append(String.join("\n\t\t", runner.getSubjectType(dSource))).append("\n");

                }
                if (attribute) {
                    info.append("Timed Value Attributes: ")
                        .append(String.join("\n\t\t\t", runner.getTimedValueAttributes(dSource)))
                        .append("\nFixed Value Attributes: ")
                        .append(String.join("\n\t\t\t", runner.getFixedValueAttributes(dSource)))
                        .append("\n");
                }
                log.info(info.toString());
            }
        }

    }

    public Importer getImporter(String importerClass) {
        Importer importer = null;
        try {

            Class<?> theClass = Class.forName(importerClass);
            Constructor<?> constructor = theClass.getConstructor();
            importer = (Importer) constructor.newInstance();
            importer.setDownloadUtils(initialiseDowloadUtils());
            importer.configure(loadApiKeys());
        } catch (Exception e) {
            log.error("\n" + DataExportRunner.BRIGHT_RED+ "-----> TASK FAILED: " + e.getMessage()  + "<-----\nCaused by " +
            e.getCause() + "\n\n" + DataExportRunner.RED + ExceptionUtils.getStackTrace(e) + DataExportRunner.END);
        }

        return importer;
    }

    public List<String> getDatasourceIds(Importer importer) {
        return importer.getDatasourceIds();
    }

    public Datasource getDatasource(String dataSourceId, Importer importer) throws Exception {
        return importer.getDatasource(dataSourceId);
    }

    public List<String> getTimedValueAttributes(Datasource datasource) {
        return datasource.getTimedValueAttributes().stream()
                .map(Attribute::getLabel).collect(Collectors.toList());
    }

    public List<String> getFixedValueAttributes(Datasource datasource) {
        return datasource.getFixedValueAttributes().stream()
                .map(Attribute::getLabel).collect(Collectors.toList());
    }

    public List<String> getSubjectType(Datasource datasource) {
        return datasource.getSubjectTypes().stream()
                .map(SubjectType::getLabel).collect(Collectors.toList());
    }

    public String getProvider(Importer importer) {
        return importer.getProvider().getLabel();
    }

}
