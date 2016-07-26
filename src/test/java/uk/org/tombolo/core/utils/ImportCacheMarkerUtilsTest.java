package uk.org.tombolo.core.utils;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.utils.ImportCacheMarkerUtils;

import static org.junit.Assert.*;

public class ImportCacheMarkerUtilsTest extends AbstractTest {
    @Test
    public void testMarkCachedReturnsFalseWhenUncached() throws Exception {
        assertEquals(false, ImportCacheMarkerUtils.isCached("hello"));
    }

    @Test
    public void testMarkCachedReturnsTrueWhenCached() throws Exception {
        ImportCacheMarkerUtils.markCached("hello");
        assertEquals(true, ImportCacheMarkerUtils.isCached("hello"));
    }
}