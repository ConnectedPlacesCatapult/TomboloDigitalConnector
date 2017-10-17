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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Get a list of datasources for an importer
 */
public class CatalogueExportRunner extends AbstractRunner {
    static Logger log = LoggerFactory.getLogger(CatalogueExportRunner.class);

    public static void main(String[] args) throws Exception {
        validateArguments(args);
        JsonWriter writer = new JsonWriter(getOutputWriter(args[0]));
        Stream<Class<? extends Importer>> importers = getImporterClasses();

        writer.beginArray();

        importers.flatMap(CatalogueExportRunner::getDatasources).forEach(dataSource -> {

            try {
                if (null != dataSource.getDatasourceSpec())
                dataSource.writeJSON(writer);
            } catch (IOException e) {
                log.warn(String.format("Could not generate JSON for datasource %s", dataSource.getDatasourceSpec().getId()), e);
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });

        writer.endArray();
        writer.close();

        Files.copy(new File(args[0]).toPath(), new File(args[1] + "/src/main/resources/catalogue.json").toPath(),
                                                                                    StandardCopyOption.REPLACE_EXISTING);
    }

    private static Stream<Class<? extends Importer>> getImporterClasses() {
        Reflections reflections = new Reflections("uk.org.tombolo");
        return reflections.getSubTypesOf(Importer.class).stream().filter(importerClass -> {
            return !Modifier.isAbstract(importerClass.getModifiers());
        });
    }

    protected static Stream<Datasource> getDatasources(Class<? extends Importer> importerClass) {
        try {
            log.info(String.format("Getting datasources for %s", importerClass.getCanonicalName()));
            Config DEFAULT_CONFIG = new Config.Builder(0, "", "", "",
                    new SubjectType(new Provider("", ""), "", "")).build();

            Class<?> theClass = Class.forName(importerClass.getCanonicalName());
            Constructor<?> constructor = theClass.getConstructor(Config.class);
            Importer importer = (Importer) constructor.newInstance(DEFAULT_CONFIG);
            importer.setDownloadUtils(initialiseDowloadUtils());
            importer.configure(loadApiKeys());

            List<Datasource> datasources = new ArrayList<Datasource>();

            for (String datasourceId: importer.getDatasourceIds())
                datasources.add(importer.getDatasource(datasourceId));

            return datasources.stream();
        } catch (Exception e) {
            log.warn(String.format("Could not get datasources for class %s", importerClass.toString()), e);
            return Stream.empty();
        }
    }

    private static void validateArguments(String[] args) {
        if (args.length != 2){
            log.error("Must provide filename to export to");
            System.exit(1);
        }
    }

    private static Writer getOutputWriter(String path) {
        try {
            return new FileWriter(path);
        } catch (IOException e) {
            log.error("Error initialising output writer: %s", path);
            System.exit(1);
            return null;
        }
    }
}
