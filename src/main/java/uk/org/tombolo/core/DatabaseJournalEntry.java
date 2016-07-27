package uk.org.tombolo.core;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="database_journal")
public class DatabaseJournalEntry {
    @EmbeddedId
    private DatabaseJournalEntryId id;

    public DatabaseJournalEntry() {}
    public DatabaseJournalEntry(String className, String key) {
        this.id = new DatabaseJournalEntryId(className, key);
    }

    public final DatabaseJournalEntryId getId() {
        return id;
    }

    public final void setId(DatabaseJournalEntryId id) {
        this.id = id;
    }

    @Embeddable
    private static class DatabaseJournalEntryId implements Serializable {
        private String key;
        private String className;

        public DatabaseJournalEntryId() {}
        public DatabaseJournalEntryId(String className, String key) {
            this.key = key;
            this.className = className;
        }

        @Column(name="key")
        public final String getKey() {
            return key;
        }
        public final void setKey(String key) {
            this.key = key;
        }

        @Column(name="className")
        public final String getClassName() {
            return className;
        }
        public final void setClassName(String className) {
            this.className = className;
        }
    }
}
