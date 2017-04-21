package uk.org.tombolo.importer.generalcsv;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.importer.GeneralCSVImporter;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by lqendro on 19/04/2017.
 */
public class GeneralCSVImporterTest extends AbstractTest {
    private GeneralCSVImporter importer;

    @Before
    public void before(){
        importer = new GeneralCSVImporter();
        mockDownloadUtils(importer);
    }

    @Test
    public void testImportDatasource() throws Exception {
        importer.importDatasource("mydatasource");

        List<Subject> subjects = SubjectUtils.getSubjectByTypeAndLabelPattern(SubjectTypeUtils.getSubjectTypeByProviderAndLabel("uk.gov.education","schools"),"uk.gov.education");
        assertEquals(1, subjects.size());
        Subject subject = subjects.get(0);
        assertEquals("Sir John Cass's Foundation Primary School", subject.getName());

        String header = "LSOA,LSOA name,attribute1,attribute2,attribute3,attribute5,attribute6";
        String value = "E01000001,Andur,value01,value10,value11,value100,value101,value110";

        String[] headers = header.split(",");
        String[] values = value.split(",");

        for (int i = 0; i < headers.length; i++) {
            testFixedValue(subject, AttributeUtils.nameToLabel(headers[i]), values[i]);
        }
    }

    private void testFixedValue(Subject subject, String attributeLabel, String value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        FixedValue fixedValue = FixedValueUtils.getBySubjectAndAttribute(subject, attribute);
        assertEquals("Value for key (" + subject.getLabel() + "," + attributeLabel + ")", value, fixedValue.getValue());
    }
}
