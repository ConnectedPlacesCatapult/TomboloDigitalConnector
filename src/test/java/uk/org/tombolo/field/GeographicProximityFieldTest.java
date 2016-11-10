package uk.org.tombolo.field;

import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldSpecificationBuilder;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;

import java.util.Collections;

public class GeographicProximityFieldTest extends AbstractTest {
    private Subject subject;
    private Subject nearbySubject;

    @Before
    public void setUp() {
        nearbySubject = TestFactory.makeNamedSubject("E09000001");
        subject = TestFactory.makeNamedSubject("E01000001");
        Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr_label");
        TestFactory.makeTimedValue("E09000001", attribute, "2011-01-01T00:00:00", 100d);
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        nearbySubject.setShape(TestFactory.makePointGeometry(0.09d, 0d)); // Just inside the given radius
        SubjectUtils.save(Collections.singletonList(nearbySubject));

        GeographicProximityField field = new GeographicProximityField("aLabel", "localAuthority", 0.1d, makeFieldSpec());
        String jsonString = field.jsonValueForSubject(subject).toJSONString();
        JSONAssert.assertEquals("{" +
                "  aLabel: {" +
                "    attr_label: [" +
                "      {" +
                "        value: 100.0" +
                "      }" +
                "    ]" +
                "  }"+
                "}", jsonString,false);
    }

    @Test
    public void testJsonValueForSubjectWithNullMaxRadius() throws Exception {
        nearbySubject.setShape(TestFactory.makePointGeometry(0.0009d, 0d)); // Just inside the default radius
        SubjectUtils.save(Collections.singletonList(nearbySubject));

        GeographicProximityField field = new GeographicProximityField("aLabel", "localAuthority", null, makeFieldSpec());
        String jsonString = field.jsonValueForSubject(subject).toJSONString();
        JSONAssert.assertEquals("{" +
                "  aLabel: {" +
                "    attr_label: [" +
                "      {" +
                "        value: 100.0" +
                "      }" +
                "    ]" +
                "  }"+
                "}", jsonString,false);
    }

    private FieldSpecification makeFieldSpec() {
        return SpecificationDeserializer.fromJson(
                FieldSpecificationBuilder.latestValue("default_provider_label", "attr_label").toJSONString(),
                FieldSpecification.class);
    }
}