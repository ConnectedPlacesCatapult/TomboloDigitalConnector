package uk.org.tombolo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.DatasourceSpec;
import uk.org.tombolo.importer.Importer;

import java.util.List;
import java.util.Properties;

/**
 * Get a list of datasources for an importer
 */
public class DataCatalogueRunner extends AbstractRunner {
    static Logger log = LoggerFactory.getLogger(DataCatalogueRunner.class);

    public static void main(String[] args) throws Exception {
        validateArguments(args);
        String className = args[0];
        String datasetId = args[1];

        // Load API keys
        Properties apiKeys = loadApiKeys();

        // Initialise Importer
        Importer importer = (Importer)Class.forName(className).newInstance();
        importer.configure(apiKeys);
        importer.setDownloadUtils(initialiseDowloadUtils());

        // Output catalogue
        if (datasetId == null || "".equals(datasetId)) {
            // datasetId is null and hence we print a list of datasets
            List<String> datasourceList = importer.getDatasourceIds();
            for (String datasourceId : datasourceList) {
                DatasourceSpec datasourceSpec = importer.getDatasourceSpec(datasourceId);
                System.out.println(
                        datasourceSpec.getId()
                        + "\t" + datasourceSpec.getName()
                        + "\t" + datasourceSpec.getDescription()
                );
            }
        }else{
            // datasetId is specified and hence we print a list of attributes provided by the datasource
            Datasource datasource = importer.getDatasource(datasetId);
            for(Attribute attribute : datasource.getTimedValueAttributes()) {
                System.out.println(
                        attribute.getLabel()
                        + "\t" + attribute.getDescription()
                );
            }
        }
    }

    private static void validateArguments(String[] args){
        // Parse input arguments
        if (args.length < 2){
            log.error("Usage: gradle runCatalogue -PimporterClassName='importerClassName' -PdatasetId='datasetId'");
            System.exit(1);
        }

    }
}
