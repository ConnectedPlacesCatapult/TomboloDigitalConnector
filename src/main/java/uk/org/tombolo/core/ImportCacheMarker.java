package uk.org.tombolo.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="import_cache_marker")
public class ImportCacheMarker {
    private String key;

    public ImportCacheMarker() {}

    public ImportCacheMarker(String key) {
        this.key = key;
    }

    @Id
    @Column(name="key")
    public final String getKey() {
        return key;
    }
    public final void setKey(String key) {
        this.key = key;
    }
}
