package uk.org.tombolo.importer;

/**
 * Class representing the datasourceID
 */

public class DataSourceID {
    private String label;
    private String name;
    private String description;
    private String url;
    private String remoteDataFile;

    public DataSourceID(String label, String name, String description, String url, String remoteDataFile) {
        this.label = label;
        this.name = name;
        this.description = description;
        this.url = url;
        this.remoteDataFile = remoteDataFile;
    }

    public String getLabel() {
        return label;
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

    public String getRemoteDataFile() {
        return remoteDataFile;
    }

}
