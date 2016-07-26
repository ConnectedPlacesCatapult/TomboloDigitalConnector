package uk.org.tombolo.core.utils;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.ImportCacheMarker;

import static org.junit.Assert.assertEquals;

public class ImportCacheMarkerUtilsTest extends AbstractTest {
    @Test
    public void testMarkCachedReturnsFalseWhenUncached() throws Exception {
        assertEquals(false, ImportCacheMarkerUtils.isCached(new ImportCacheMarker.ImportCacheMarkerId("com.example.Importer", "hello")));
    }

    @Test
    public void testMarkCachedReturnsTrueWhenCached() throws Exception {
        ImportCacheMarkerUtils.markCached(new ImportCacheMarker.ImportCacheMarkerId("com.example.Importer", "hello"));
        assertEquals(true, ImportCacheMarkerUtils.isCached(new ImportCacheMarker.ImportCacheMarkerId("com.example.Importer", "hello")));
    }
}