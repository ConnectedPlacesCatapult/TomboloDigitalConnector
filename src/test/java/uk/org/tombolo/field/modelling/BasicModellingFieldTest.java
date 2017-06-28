package uk.org.tombolo.field.modelling;

import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.execution.spec.DatasourceSpecification;
import uk.org.tombolo.importer.ons.AbstractONSImporter;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class BasicModellingFieldTest extends AbstractTest {
    private static final String RECIPE = "ModellingFieldTest";
    BasicModellingField field = new BasicModellingField("test_label", RECIPE);

    Subject subject;

    @Before
    public void setUp() throws Exception {
        SubjectType lsoa = TestFactory.makeNamedSubjectType("lsoa");
        subject = TestFactory.makeNamedSubject("E01002766");
        Attribute population = TestFactory.makeAttribute(AbstractONSImporter.PROVIDER, "CL_0000053_1");
        Attribute oldies = TestFactory.makeAttribute(AbstractONSImporter.PROVIDER, "CL_0000053_82");
        TestFactory.makeTimedValue(lsoa, "E01002766", population, "2011-01-01T00:00:00", 100d);
        TestFactory.makeTimedValue(lsoa, "E01002766", oldies, "2011-01-01T00:00:00", 40d);
    }

    @Test
    public void getDatasourceSpecifications() throws Exception {

        List<DatasourceSpecification> datasources = field.getDatasourceSpecifications();

        assertEquals(2, datasources.size());

        DatasourceSpecification ds1 = datasources
                .stream().filter(e -> e.getDatasourceId().equals("lsoa")).findAny().orElse(null);
        assertEquals("uk.org.tombolo.importer.ons.OaImporter", ds1.getImporterClass());

        DatasourceSpecification ds2 = datasources
                .stream().filter(e -> e.getDatasourceId().equals("QS103EW")).findAny().orElse(null);
        assertEquals("uk.org.tombolo.importer.ons.ONSCensusImporter", ds2.getImporterClass());
    }

    @Test
    public void jsonValueForSubject() throws Exception {
        String jsonString = field.jsonValueForSubject(subject).toJSONString();

        JSONAssert.assertEquals("{" +
                "  test_label: [" +
                "    {" +
                "      value: 0.4" +
                "    }" +
                "  ]" +
                "}", jsonString, false);

    }

    @Test
    public void getLabel() throws Exception {
        assertEquals("test_label", field.getLabel());
    }
}