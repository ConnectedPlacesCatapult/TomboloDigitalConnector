package uk.org.tombolo.field;

import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldSpecificationBuilder;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;

import java.util.Collections;
import java.util.List;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class WrapperFieldTest extends AbstractTest {
    private WrapperField field = new WrapperField("aLabel", makeFieldSpec());
    private Subject subject = new Subject();

    @Test
    public void testJsonValueForSubject() throws Exception {
        String jsonString = field.jsonValueForSubject(subject).toJSONString();
        assertThat(jsonString, hasJsonPath("$.aLabel.anotherLabel", equalTo("aValue")));
    }

    @Test
    public void testGetLabel() throws Exception {
        assertEquals("aLabel", field.getLabel());
    }

    @Test
    public void testGetHumanReadableName() throws Exception {
        assertEquals("aLabel", field.getHumanReadableName());
    }

    private List<FieldSpecification> makeFieldSpec() {
        return Collections.singletonList(
                SpecificationDeserializer.fromJson(FieldSpecificationBuilder.fixedAnnotationField("anotherLabel", "aValue").toJSONString(), FieldSpecification.class));
    }
}