package uk.org.tombolo.importer.utils;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test class for coordinate utilities.
 */
public class CoordinateUtilsTest extends AbstractTest {
    @Test
    public void testPostcodeToLatLong() throws Exception {
        // Setting the provider to dfe so we don't need to download the file and use
        // the short version present in resources
        Map<String, LatLong> testMap =
                CoordinateUtils.postcodeToLatLong("uk.gov.education", makeTestDownloadUtils());
        assertEquals("54.972045", testMap.get("NE98").getLatitude());
        assertEquals("-1.598706", testMap.get("NE98").getLongitude());
    }
}
