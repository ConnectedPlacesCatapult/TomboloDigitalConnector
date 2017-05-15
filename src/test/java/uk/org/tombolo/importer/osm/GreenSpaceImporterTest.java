package uk.org.tombolo.importer.osm;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test for green space importer from open street map
 */
public class GreenSpaceImporterTest extends AbstractTest {
    private static GreenSpaceImporter importer;

    @Before
    public void before(){
        importer = new GreenSpaceImporter(TestFactory.DEFAULT_CONFIG);
        mockDownloadUtils(importer);
    }

    @Test
    public void testImportDatasource() throws Exception {
        importer.importDatasource("OSMGreenSpace");

        List<Subject> subjects = SubjectUtils.getSubjectByTypeAndLabelPattern(SubjectTypeUtils.getSubjectTypeByProviderAndLabel("de.overpass-api","OSMEntity"),"osm34597927");
        assertEquals(1, subjects.size());
        Subject subject = subjects.get(0);
        assertEquals("Whitegates-South", subject.getName());

        String header = "category,value\n";
        String value = "leisure,dog_park\n";
        String[] headers = header.split("[,\n]");
        String[] values = value.split("[,\n]");

        for (int i = 0; i < headers.length; i++) {
            testFixedValue(subject, headers[i], values[i]);
        }
    }

    private void testFixedValue(Subject subject, String attributeLabel, String value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, attribute);
        assertEquals("Value for key (" + subject.getLabel() + "," + attributeLabel + ")", value, fixedValue.getValue());
    }
}
