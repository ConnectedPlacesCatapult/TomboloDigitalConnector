package uk.org.tombolo.core;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="import_cache_marker")
public class ImportCacheMarker {
    @EmbeddedId
    private ImportCacheMarkerId id;

    public ImportCacheMarker() {}
    public ImportCacheMarker(ImportCacheMarkerId id) {
        this.id = id;
    }

    public ImportCacheMarkerId getId() {
        return id;
    }

    public void setId(ImportCacheMarkerId id) {
        this.id = id;
    }


    @Embeddable
    public static class ImportCacheMarkerId implements Serializable {
        private String key;
        private String importer;

        public ImportCacheMarkerId() {}

        public ImportCacheMarkerId(String key, String importer) {
            this.key = key;
            this.importer = importer;
        }

        @Column(name="key")
        public final String getKey() {
            return key;
        }
        public final void setKey(String key) {
            this.key = key;
        }

        @Column(name="importer")
        public final String getImporter() {
            return importer;
        }
        public final void setImporter(String importer) {
            this.importer = importer;
        }
    }
}
