package uk.org.tombolo.field.transformation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldSpecificationBuilder;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.recipe.RecipeDeserializer;
import uk.org.tombolo.field.IncomputableFieldException;

import static org.junit.Assert.assertEquals;

public class ArithmeticFieldTest extends AbstractTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testValueForSubjectDivision() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E01000001");
        ArithmeticField field = new ArithmeticField("aLabel", ArithmeticField.Operation.div, makeFieldSpec("fixed1", "1"), makeFieldSpec("fixed2", "2"));
        assertEquals(field.valueForSubject(subject, true), "0.5");
    }

    @Test
    public void testValueForSubjectDivisionByZero() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E01000001");
        ArithmeticField field = new ArithmeticField("aLabel", ArithmeticField.Operation.div, makeFieldSpec("fixed1", "1"), makeFieldSpec("fixed2", "0"));

        thrown.expect(IncomputableFieldException.class);
        thrown.expectMessage("Arithmetic operation div returned Infinity (possible division by zero?)");

        field.valueForSubject(subject, true);
    }

    @Test
    public void testValueForSubjectAddition() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E01000001");
        ArithmeticField field = new ArithmeticField("aLabel", ArithmeticField.Operation.add, makeFieldSpec("fixed1", "1"), makeFieldSpec("fixed2", "2"));
        assertEquals(field.valueForSubject(subject, true), "3.0");
    }

    @Test
    public void testValueForSubjectSubtraction() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E01000001");
        ArithmeticField field = new ArithmeticField("aLabel", ArithmeticField.Operation.sub, makeFieldSpec("fixed1", "0"), makeFieldSpec("fixed2", "2"));
        assertEquals(field.valueForSubject(subject, false), "-2.0");
    }

    @Test
    public void testValueForSubjectMultiplication() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E01000001");
        ArithmeticField field = new ArithmeticField("aLabel", ArithmeticField.Operation.mul, makeFieldSpec("fixed1", "3"), makeFieldSpec("fixed2", "2"));
        assertEquals(field.valueForSubject(subject, null), "6.0");
    }

    private FieldRecipe makeFieldSpec(String label, String value) {
        return RecipeDeserializer.fromJson(
                FieldSpecificationBuilder.fixedAnnotationField(label, value).toJSONString(),
                FieldRecipe.class);
    }
}