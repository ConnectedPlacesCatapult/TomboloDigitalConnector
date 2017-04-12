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
    private String localDataFile;

    public DataSourceID(String label, String name, String description, String url, String remoteDataFile, String localDataFile) {
        this.label = label;
        this.name = name;
        this.description = description;
        this.url = url;
        this.remoteDataFile = remoteDataFile;
        this.localDataFile = localDataFile;
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

    public String getLocalDataFile() { return localDataFile; }
}
