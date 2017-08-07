package uk.org.tombolo.field.aggregation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldSpecificationBuilder;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.FixedValue;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.FixedValueUtils;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.field.IncomputableFieldException;

import java.util.Arrays;

import static org.junit.Assert.*;

public class BackOffFieldTest extends AbstractTest {
    private static final String ATTRIBUTE_LABEL = "testAttribute";

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void valueForSubject() throws Exception {

        SubjectType point = TestFactory.makeSubjectType(TestFactory.DEFAULT_PROVIDER, "point", "Point");
        SubjectType square = TestFactory.makeSubjectType(TestFactory.DEFAULT_PROVIDER, "square", "Square");

        // Point A and point C are outside square B
        // Point B is inside square B
        Subject pointA = TestFactory.makeSubject(point, "pointA", "Point A", TestFactory.makePointGeometry(0.0d,0.0d));
        Subject pointB = TestFactory.makeSubject(point, "pointB", "Point B", TestFactory.makePointGeometry(1.5d,1.5d));
        Subject squareB = TestFactory.makeSubject(square, "squareB", "Square B", TestFactory.makeSquareGeometry(1.0d, 1.0d, 1.0d));
        Subject pointC = TestFactory.makeSubject(point, "pointC", "Point C", TestFactory.makePointGeometry(2.5d, 2.5d));

        Attribute testAttribute = new Attribute(TestFactory.DEFAULT_PROVIDER,ATTRIBUTE_LABEL, "", "", Attribute.DataType.string);
        AttributeUtils.save(testAttribute);

        FixedValueUtils.save(new FixedValue(pointA,testAttribute, "pointAvalue"));
        FixedValueUtils.save(new FixedValue(squareB, testAttribute, "squareBvalue"));

        // Field for returning the point's attribute
        FieldRecipe attributeValueField
                = FieldSpecificationBuilder.fixedValueField(TestFactory.DEFAULT_PROVIDER.getLabel(), ATTRIBUTE_LABEL)
                .setLabel("fixed")
                .build();

        // Field for returning the point's square parent's value
        FieldRecipe containingSubjectField = FieldSpecificationBuilder.mapToContainingSubjectField(
                "mapped",
                TestFactory.DEFAULT_PROVIDER.getLabel(),
                "square",
                FieldSpecificationBuilder.fixedValueField(TestFactory.DEFAULT_PROVIDER.getLabel(), ATTRIBUTE_LABEL)).build();

        // Field for first trying to return the point's value but if that is not available then return the point's square parent's value
        BackOffField backOffField  = new BackOffField("backoff", Arrays.asList(attributeValueField, containingSubjectField));

        // Point A has value so that value is returned
        String pointAvalue = backOffField.valueForSubject(pointA);
        assertEquals("pointAvalue", pointAvalue);

        // Point B has no value so we back-off to the surrounding square
        String pointBvalue = backOffField.valueForSubject(pointB);
        assertEquals("squareBvalue", pointBvalue);

        // Point C has no value nor surrounding square so we throw an exception
        thrown.expect(IncomputableFieldException.class);
        backOffField.valueForSubject(pointC);
    }
}