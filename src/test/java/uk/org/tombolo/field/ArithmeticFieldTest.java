package uk.org.tombolo.field;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldSpecificationBuilder;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;

import static org.junit.Assert.*;

public class ArithmeticFieldTest extends AbstractTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testValueForSubject() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E01000001");
        ArithmeticField field = new ArithmeticField("aLabel", "div", makeFieldSpec("fixed1", "1"), makeFieldSpec("fixed2", "2"));
        assertEquals(field.valueForSubject(subject), "0.5");
    }

    @Test
    public void testValueForSubjectDivisionByZero() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E01000001");
        ArithmeticField field = new ArithmeticField("aLabel", "div", makeFieldSpec("fixed1", "1"), makeFieldSpec("fixed2", "0"));

        thrown.expect(IncomputableFieldException.class);
        thrown.expectMessage("Arithmetic operation div returned Infinity (possible division by zero?)");

        field.valueForSubject(subject);
    }

    private FieldSpecification makeFieldSpec(String label, String value) {
        return SpecificationDeserializer.fromJson(
                FieldSpecificationBuilder.fixedAnnotationField(label, value).toJSONString(),
                FieldSpecification.class);
    }
}