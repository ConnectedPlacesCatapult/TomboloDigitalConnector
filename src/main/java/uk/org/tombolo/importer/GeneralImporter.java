package uk.org.tombolo.importer;

public abstract class GeneralImporter extends AbstractImporter{
    protected Config config;

    public GeneralImporter(Config config) {
        this.config = config;
    }

    public void setConfig(Config config) { this.config = config; }
}
