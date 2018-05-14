package uk.org.tombolo.importer.nhschoices;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by tbantis on 16/02/2018.
 */
@Ignore ("Until redirected to updated source")
public class NHSAdmissionsImporterTest extends AbstractTest {
    private static NHSAdmissionsImporter importer;
    Subject islington;
    Subject barking;

    @Before
    public void before(){
        importer = new NHSAdmissionsImporter();
        mockDownloadUtils(importer);
        barking = TestFactory.makeNamedSubject("E09000002");
        islington = TestFactory.makeNamedSubject("E09000019");
    }

    @Test
    public void testGetProvider(){
        Provider provider = importer.getProvider();
        assertEquals("uk.digital.nhs", provider.getLabel());
        assertEquals("NHS Digital", provider.getName());
    }

    @Test
    public void testGetDatasourceIds() throws Exception {
        List<String> datasources = importer.getDatasourceIds();
        assertEquals(1,datasources.size());
    }

    @Test
    public void testGetDatasource() throws Exception {
        Datasource datasource = importer.getDatasource("NHSAdmissionsObese");
        assertEquals("https://digital.nhs.uk/media/28729/Statistics-on-Obesity-Physical-Activity-and-Diet-England-2016-Tables/Any/obes-phys-acti-diet-eng-2016-tab", datasource.getDatasourceSpec().getUrl());
    }

    @Test
    public void testImportDatasource() throws Exception {
        importer.importDatasource("NHSAdmissionsObese", null, null, null);
        System.out.println(importer.getTimedValueCount());

        testTimedValue(barking, "admissions_all", 618.273876163676);
        testTimedValue(barking, "admissions_male", 447.796613667083);
        testTimedValue(barking, "admissions_female", 781.257706535142);

        testTimedValue(islington, "admissions_all", 754.196262950731);
        testTimedValue(islington, "admissions_male", 478.860185546964);
        testTimedValue(islington, "admissions_female", 1027.23987853339);

    }

    private void testTimedValue(Subject subject, String attributeLabel, Double value) {
        Attribute attribute = AttributeUtils.getByProviderAndLabel(importer.getProvider(), attributeLabel);
        TimedValue val = TimedValueUtils.getLatestBySubjectAndAttribute(subject, attribute);
        assertEquals("Value for key (" + subject.getLabel() + "," + attributeLabel + ")", value, val.getValue(),0.0001d);
    }

}
