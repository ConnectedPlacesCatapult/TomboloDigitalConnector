package uk.org.tombolo;

import com.google.gson.stream.JsonWriter;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.importer.Importer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Get a list of datasources for an importer
 */
public class CatalogueExportRunner extends AbstractRunner {
    private static final Logger log = LoggerFactory.getLogger(CatalogueExportRunner.class);
    private static final CatalogueExportRunner runner = new CatalogueExportRunner();

    public static void main(String[] args) throws Exception {
        runner.validateArguments(args);

        JsonWriter writer = new JsonWriter(runner.getOutputWriter(args[0]));
        List<Class<? extends Importer>> importers = runner.getImporterClasses();

        writer.beginArray();

        for (Class<? extends Importer> i : importers) {

            if (!i.getCanonicalName().equals("uk.org.tombolo.importer.generalcsv.GeneralCSVImporter")
                    && !i.getCanonicalName().equals("uk.org.tombolo.importer.PythonImporter")) {
                Importer importer = runner.getImporter(i);

                List<String> datasources = runner.getDatasourceIds(importer);

                for (String d : datasources) {
                    Datasource datasource = runner.getDatasource(d, importer);

                    if (null != datasource.getDatasourceSpec()) datasource.writeJSON(writer);
                }
            }
        }

        writer.endArray();
        writer.close();

    }

    private List<Class<? extends Importer>> getImporterClasses() {
        Reflections reflections = new Reflections("uk.org.tombolo");
        List<Class<? extends Importer>> toReturn;
        Set<Class<? extends  Importer>> data = reflections.getSubTypesOf(Importer.class);
        toReturn = data.stream().filter(d -> !Modifier.isAbstract(d.getModifiers())).collect(Collectors.toList());
        return toReturn;
    }

    public Importer getImporter(Class<? extends Importer> importerClass) {
        Importer importer = null;
        try {

            Class<?> theClass = Class.forName(importerClass.getCanonicalName());
            Constructor<?> constructor = theClass.getConstructor();
            importer = (Importer) constructor.newInstance();
            importer.setDownloadUtils(initialiseDowloadUtils());
            importer.configure(loadApiKeys());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return importer;
    }

    public List<String> getDatasourceIds(Importer importer) {
        return importer.getDatasourceIds();
    }

    private Datasource getDatasource(String dataSourceId, Importer importer) throws Exception {
        return importer.getDatasource(dataSourceId);
    }

    protected void validateArguments(String[] args) {
        if (args.length != 1){
            log.error("Must provide filename to export to");
            System.exit(1);
        }
    }
}
