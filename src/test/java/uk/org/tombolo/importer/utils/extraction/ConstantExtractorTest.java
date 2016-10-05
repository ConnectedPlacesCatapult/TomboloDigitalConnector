package uk.org.tombolo.importer.utils.extraction;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConstantExtractorTest {

    @Test
    public void testExtract() throws Exception {
        ConstantExtractor extractor = new ConstantExtractor("myLittleConstant");
        assertEquals("myLittleConstant", extractor.extract());
    }
}