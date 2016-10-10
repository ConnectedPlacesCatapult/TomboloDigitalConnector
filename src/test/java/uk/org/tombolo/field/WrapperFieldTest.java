package uk.org.tombolo.field;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldSpecificationBuilder;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WrapperFieldTest extends AbstractTest {
    private WrapperField field = new WrapperField("aLabel", makeFieldSpec());
    private Subject subject = new Subject();

    @Test
    public void testJsonValueForSubject() throws Exception {
        String jsonString = field.jsonValueForSubject(subject).toJSONString();
        JSONAssert.assertEquals("{aLabel: {anotherLabel: 'aValue'}}", jsonString, false);
    }

    @Test
    public void testGetLabel() throws Exception {
        assertEquals("aLabel", field.getLabel());
    }

    private List<FieldSpecification> makeFieldSpec() {
        return Collections.singletonList(
                SpecificationDeserializer.fromJson(FieldSpecificationBuilder.fixedAnnotationField("anotherLabel", "aValue").toJSONString(), FieldSpecification.class));
    }
}