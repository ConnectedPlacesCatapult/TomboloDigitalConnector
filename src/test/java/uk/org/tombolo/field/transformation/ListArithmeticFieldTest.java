package uk.org.tombolo.field.transformation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldBuilder;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.recipe.RecipeDeserializer;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Test the ListArithmeticField for sum and multiplication.
 */
public class ListArithmeticFieldTest extends AbstractTest {

        @Rule
        public ExpectedException thrown = ExpectedException.none();

        @Test
        public void testValueForSubjectAddition() throws Exception {
            Subject subject = TestFactory.makeNamedSubject("E01000001");
            ListArithmeticField field = new ListArithmeticField("aLabel", ListArithmeticField.Operation.add,
                    Arrays.asList(makeFieldSpec("fixed1", "1"), makeFieldSpec("fixed2", "2"),
                            makeFieldSpec("fixed3", "3"), makeFieldSpec("fixed4", "4")));
            assertEquals(field.valueForSubject(subject, true), "10.0");
        }

        @Test
        public void testValueForSubjectMultiplication() throws Exception {
            Subject subject = TestFactory.makeNamedSubject("E01000001");
            ListArithmeticField field = new ListArithmeticField("aLabel", ListArithmeticField.Operation.mul,
                    Arrays.asList(makeFieldSpec("fixed1", "1"), makeFieldSpec("fixed2", "2"),
                            makeFieldSpec("fixed3", "3"), makeFieldSpec("fixed4", "4")));
            assertEquals(field.valueForSubject(subject, null), "24.0");
        }

        private FieldRecipe makeFieldSpec(String label, String value) {
            return RecipeDeserializer.fromJson(
                    FieldBuilder.fixedAnnotationField(label, value).toJSONString(),
                    FieldRecipe.class);
        }
}
