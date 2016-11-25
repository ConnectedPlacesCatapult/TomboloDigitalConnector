package uk.org.tombolo.field.aggregation;

import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldSpecificationBuilder;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;

import static org.junit.Assert.assertEquals;

public class MapToContainingSubjectFieldTest extends AbstractTest {
    private Subject subject;
    private MapToContainingSubjectField field;

    @Before
    public void setUp() {
        field = new MapToContainingSubjectField("aLabel", "localAuthority", makeFieldSpec());
        TestFactory.makeNamedSubject("E09000001"); // Subject that contains subject below
        subject = TestFactory.makeNamedSubject("E01000001");
        Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr_label");
        TestFactory.makeTimedValue("E09000001", attribute, "2011-01-01T00:00:00", 100d);
    }

    @Test
    public void testValueForSubject() throws Exception {
        String value = field.valueForSubject(subject);
        assertEquals("100.0", value);
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        String jsonString = field.jsonValueForSubject(subject).toJSONString();
        JSONAssert.assertEquals("{" +
                "  aLabel: {" +
                "    attr_label: [" +
                "      {" +
                "        value: 100.0" +
                "      }" +
                "    ]" +
                "  }"+
                "}",jsonString,false);
    }

    private FieldSpecification makeFieldSpec() {
        return SpecificationDeserializer.fromJson(
                FieldSpecificationBuilder.latestValue("default_provider_label", "attr_label").toJSONString(),
                FieldSpecification.class);
    }
}