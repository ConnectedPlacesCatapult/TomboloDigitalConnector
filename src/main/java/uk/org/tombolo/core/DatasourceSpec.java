package uk.org.tombolo.core;

import uk.org.tombolo.importer.Importer;

/*
    Details class for datasource
 */
public class DatasourceSpec {
    private Class<? extends Importer> importerClass;
    private String id;
    private String name;
    private String description;
    private String url;					// Url of the datasource for that series

    public DatasourceSpec(Class<? extends Importer> importerClass, String id, String name, String description, String url){
        this.importerClass = importerClass;
        this.id = id;
        this.name = name;
        this.description = description;
        this.url = url;
    }

    public Class<? extends Importer> getImporterClass() {
        return importerClass;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }
}
