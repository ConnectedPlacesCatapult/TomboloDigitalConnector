package uk.org.tombolo.importer.utils;

import org.junit.Before;
import org.junit.Test;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JSONReaderTest {

    private JSONReader reader;
    private ArrayList<LinkedHashMap<String, List<String>>> sections;

    @Before
    public void setUp() throws IOException {
        String resourcePath = "executions/air_quality_test_data.json";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        reader = new JSONReader(file);
        sections = reader.getData();
    }

    @Test
    public void testGetData() {

        assertEquals ("BG1", sections.get(0).get("@SiteCode").get(0));
    }

    @Test
    public void testGetTagValueFromAllSections() {

        assertEquals("BL0", reader.getTagValueFromAllSections("@SiteCode").get(3));
    }

    @Test
    public void testContainsMoreThanOneValues() {

        assertEquals(true, reader.containsMoreThanOneValues("@SpeciesCode"));
    }

    @Test
    public void testGetTagValueOfSpecificSection() {

        assertEquals("Urban Background", reader.getTagValueOfSpecificSection("@SiteType", 3).get(0));

    }

    @Test
    public void testAllUniqueKeys() {

        assertEquals("@Latitude", reader.allUniquekeys().get(3));

    }

    @Test
    public void testConditionalResults() {

        assertEquals("Suburban", reader.conditionalResults("@SiteType", "@SiteCode", "BG1").get(0).get(0));

    }
}
