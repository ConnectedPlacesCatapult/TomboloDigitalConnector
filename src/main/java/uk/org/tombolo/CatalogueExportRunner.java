package uk.org.tombolo;

import com.google.gson.stream.JsonWriter;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.Importer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Get a list of datasources for an importer
 */
public class CatalogueExportRunner extends AbstractRunner {
    static Logger log = LoggerFactory.getLogger(CatalogueExportRunner.class);

    public static void main(String[] args) throws Exception {
        validateArguments(args);
        CatalogueExportRunner exportRunner = new CatalogueExportRunner();
        JsonWriter writer = new JsonWriter(exportRunner.getOutputWriter(args[0]));
        List<Class<? extends Importer>> importers = exportRunner.getImporterClasses();

        writer.beginArray();

        for (Class<? extends Importer> i : importers) {

            if (!i.getCanonicalName().equals("uk.org.tombolo.importer.generalcsv.GeneralCSVImporter")) {
                Importer importer = exportRunner.getImporter(i);

                List<String> datasources = exportRunner.getDatasourceIds(importer);

                for (String d : datasources) {
                    if (datasources.size() > 0) importer = exportRunner.getImporter(i);
                    Datasource datasource = exportRunner.getDatasource(d, importer);

                    if (null != datasource.getDatasourceSpec()) datasource.writeJSON(writer);
                }
            }
        }

        writer.endArray();
        writer.close();

        Files.copy(new File(args[0]).toPath(), new File(args[1] + "/src/main/resources/catalogue.json").toPath(),
                                                                                    StandardCopyOption.REPLACE_EXISTING);
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
            Config DEFAULT_CONFIG = new Config.Builder(0, "", "", "",
                    new SubjectType(new Provider("", ""), "", "")).build();

            Class<?> theClass = Class.forName(importerClass.getCanonicalName());
            Constructor<?> constructor = theClass.getConstructor(Config.class);
            importer = (Importer) constructor.newInstance(DEFAULT_CONFIG);
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

    private static void validateArguments(String[] args) {
        if (args.length != 2){
            log.error("Must provide filename to export to");
            System.exit(1);
        }
    }

    private Writer getOutputWriter(String path) {
        try {
            return new FileWriter(path);
        } catch (IOException e) {
            log.error("Error initialising output writer: %s", path);
            System.exit(1);
            return null;
        }
    }
}
