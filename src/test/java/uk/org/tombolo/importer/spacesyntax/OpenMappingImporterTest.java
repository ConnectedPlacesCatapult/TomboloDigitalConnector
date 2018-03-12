package uk.org.tombolo.importer.spacesyntax;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.*;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Using b5cb7394-e18a-3060-affe-ac36c73192df.zip
 */
public class OpenMappingImporterTest extends AbstractTest {
    private static OpenMappingImporter importer;

    @Before
    public void before(){
        importer = new OpenMappingImporter();
        mockDownloadUtils(importer);
    }

    @Test
    public void testGetProvider(){
        Provider provider = importer.getProvider();
        assertEquals("com.spacesyntax", provider.getLabel());
        assertEquals("Space Syntax", provider.getName());
    }

    @Test
    public void testGetDatasourceIds() throws Exception {
        List<String> datasources = importer.getDatasourceIds();
        assertEquals(1,datasources.size());
    }

    @Test
    public void testImportDatasource() throws Exception {
        importer.importDatasource("SpaceSyntaxOpenMapping", null, null, null);

        List<Subject> subjects = SubjectUtils.getSubjectByTypeAndLabelPattern(SubjectTypeUtils.getSubjectTypeByProviderAndLabel("com.spacesyntax","space_syntax"),"com.spacesyntax_5470");
        assertEquals(1, subjects.size());
        Subject subject = subjects.get(0);
        assertEquals("5470", subject.getName());
        testTimedValue(subject, "choice2km", 20460.);
        testFixedValue(subject, "meridian_road_name", "STENTIFORD HILL ROAD");
    }
    private void testFixedValue(Subject subject, String attributeLabel, String value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        FixedValue school = FixedValueUtils.getBySubjectAndAttribute(subject, attribute);
        assertEquals("Value for key (" + subject.getLabel() + "," + attributeLabel + ")", value, school.getValue());
    }

    private void testTimedValue(Subject subject, String attributeLabel, Double value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        TimedValue val = TimedValueUtils.getLatestBySubjectAndAttribute(subject, attribute);
        assertEquals(value, val.getValue(),0.00001);
    }
}
