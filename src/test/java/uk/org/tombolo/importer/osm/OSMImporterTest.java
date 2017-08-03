package uk.org.tombolo.importer.osm;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 *
 */
public class OSMImporterTest extends AbstractTest {



    @Test
    public void getProvider() throws Exception {
    }

    @Test
    public void getDatasource() throws Exception {

    }

    @Test
    public void getSubjectTypes() throws Exception {
    }

    @Test
    public void getFixedValuesAttributes() throws Exception {
    }

    @Test
    public void setupUtils() throws Exception {
    }

    @Test
    public void importDatasource() throws Exception {
        OSMImporter importer = new GreenSpaceImporter(null);
        mockDownloadUtils(importer);
        importer.importDatasource("osm");
    }
}