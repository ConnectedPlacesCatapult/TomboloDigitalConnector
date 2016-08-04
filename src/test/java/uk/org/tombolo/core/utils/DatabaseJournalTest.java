package uk.org.tombolo.core.utils;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.DatabaseJournalEntry;

import static org.junit.Assert.assertEquals;

public class DatabaseJournalTest extends AbstractTest {
    @Test
    public void testMarkCachedReturnsFalseWhenUncached() throws Exception {
        assertEquals(false, DatabaseJournal.journalHasEntry(new DatabaseJournalEntry("com.example.Importer", "hello")));
    }

    @Test
    public void testMarkCachedReturnsTrueWhenCached() throws Exception {
        DatabaseJournal.addJournalEntry(new DatabaseJournalEntry("com.example.Importer", "hello"));
        assertEquals(true, DatabaseJournal.journalHasEntry(new DatabaseJournalEntry("com.example.Importer", "hello")));
    }
}