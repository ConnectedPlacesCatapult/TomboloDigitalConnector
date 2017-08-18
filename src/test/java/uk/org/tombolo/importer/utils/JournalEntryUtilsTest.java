package uk.org.tombolo.importer.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import uk.org.tombolo.core.DatabaseJournalEntry;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class JournalEntryUtilsTest {

    @Test
    public void getJournalEntryForDatasourceIdNull() throws Exception {

        DatabaseJournalEntry entry = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                null,
                null,
                null);
        assertEquals("class.name", entry.getClassName());
        assertEquals("datasource-id", entry.getKey());
    }

    @Test
    public void getJournalEntryForDatasourceIdEmpty() throws Exception {

        DatabaseJournalEntry entry = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                Collections.emptyList(),
                Collections.emptyList(),
                null);
        assertEquals("class.name", entry.getClassName());
        assertEquals("datasource-id", entry.getKey());
    }

    @Test
    public void getJournalEntryForDatasourceIdEmptyNull() throws Exception {

        DatabaseJournalEntry entry = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                Collections.emptyList(),
                null,
                null);
        assertEquals("class.name", entry.getClassName());
        assertEquals("datasource-id", entry.getKey());
    }

    @Test
    public void getJournalEntryForDatasourceIdGeoNull() throws Exception {

        DatabaseJournalEntry entry = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                Arrays.asList("geo"),
                null,
                null);
        assertEquals("class.name", entry.getClassName());
        assertEquals("datasource-id:"+ DigestUtils.md5Hex("geo||"), entry.getKey());
    }

    @Test
    public void getJournalEntryForDatasourceIdEmptyTime() throws Exception {

        DatabaseJournalEntry entry = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                Collections.emptyList(),
                Arrays.asList("time"),
                null);
        assertEquals("class.name", entry.getClassName());
        assertEquals("datasource-id:" + DigestUtils.md5Hex("|time|"), entry.getKey());
    }

    @Test
    public void getJournalEntryForDatasourceIdGeoTime() throws Exception {

        DatabaseJournalEntry entry = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                Arrays.asList("geo"),
                Arrays.asList("time"),
                null);
        assertEquals("class.name", entry.getClassName());
        assertEquals("datasource-id:"+ DigestUtils.md5Hex("geo|time|"), entry.getKey());
    }

    @Test
    public void getJournalEntryForDatasourceIdGeoTimeData() throws Exception {

        DatabaseJournalEntry entry = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                Arrays.asList("geo"),
                Arrays.asList("time"),
                Arrays.asList("data"));
        assertEquals("class.name", entry.getClassName());
        assertEquals("datasource-id:"+ DigestUtils.md5Hex("geo|time|data"), entry.getKey());
    }

    @Test
    public void getJournalEntryForDatasourceIdOneTwoThree() throws Exception {

        DatabaseJournalEntry entry1 = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                Arrays.asList("one", "two"),
                Arrays.asList("three"),
                Arrays.asList("four"));
        assertEquals("class.name", entry1.getClassName());
        assertEquals("datasource-id:" + DigestUtils.md5Hex("one\ttwo|three|four"), entry1.getKey());

        // Here we test that the arrays are sorted before joining
        DatabaseJournalEntry entry2 = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                Arrays.asList("one"),
                Arrays.asList("two", "three"),
                Arrays.asList("four"));
        assertEquals("class.name", entry2.getClassName());
        assertEquals("datasource-id:" + DigestUtils.md5Hex("one|three\ttwo|four"), entry2.getKey());

        DatabaseJournalEntry entry3 = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                Arrays.asList("one"),
                Arrays.asList("three", "two"),
                Arrays.asList("four"));
        assertEquals("class.name", entry3.getClassName());
        assertEquals("datasource-id:" + DigestUtils.md5Hex("one|three\ttwo|four"), entry3.getKey());

        assertEquals(entry2.getKey(), entry3.getKey());

        // This one is to make sure that we use a different separator intra-scope and inter-scope
        assertNotSame(entry1.getKey(), entry2.getKey());
    }
}