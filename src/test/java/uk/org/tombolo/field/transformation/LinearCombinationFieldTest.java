package uk.org.tombolo.field.transformation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldBuilder;

import java.util.Arrays;

import static org.junit.Assert.*;

public class LinearCombinationFieldTest extends AbstractTest {

    private LinearCombinationField linearCombinationField = new LinearCombinationField(
            "aLabel",
            Arrays.asList(0.6f, 0.2f, 0.1f, 0.1f),
            Arrays.asList(
                    FieldBuilder.constantField("field1", "1").build(),
                    FieldBuilder.constantField("field2", "2").build(),
                    FieldBuilder.constantField("field3", "3").build(),
                    FieldBuilder.constantField("field4", "4").build()
            )
    );

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testInitialiseUnevenLength() throws Exception {
        LinearCombinationField unevenField = new LinearCombinationField(
                "aLabel",
                Arrays.asList(0.6f, 0.3f, 0.1f),
                Arrays.asList(
                        FieldBuilder.constantField("field1", "1").build(),
                        FieldBuilder.constantField("field2", "2").build()
               )
        );
        thrown.expect(Exception.class);
        thrown.expectMessage("For LinearCombinationField, scalars and fields must have same length");
        unevenField.initialize();
    }

    @Test
    public void testInitialiseNonSingeValueField(){
        LinearCombinationField noneSingelValueField = new LinearCombinationField(
                "aLabel",
                Arrays.asList(0.6f, 0.4f),
                Arrays.asList(
                        FieldBuilder.constantField("field1", "1").build(),
                        FieldBuilder.valuesByTime("provider", "attribute").build()
                )
        );
        thrown.expect(Exception.class);
        thrown.expectMessage("Parameters for LinearCombinationField must be of type SingleValueField");
        noneSingelValueField.initialize();
    }

    @Test
    public void valueForSubject() throws Exception {
        assertEquals(0.6d + 0.4d + 0.3d + 0.4d,
                Double.parseDouble(linearCombinationField.valueForSubject(null, null)),
                0.0001d);
    }

    @Test
    public void jsonValueForSubject() throws Exception {
        LinearCombinationField simpleLinearCombinationField = new LinearCombinationField(
                "aLabel",
                Arrays.asList(2f),
                Arrays.asList(FieldBuilder.constantField("field2", "2").build())
        );
        String jsonString = simpleLinearCombinationField.jsonValueForSubject(null, false).toJSONString();
        JSONAssert.assertEquals("{aLabel: 4}", jsonString, false);
    }

    @Test
    public void getChildFields() throws Exception {
        assertEquals(4, linearCombinationField.getChildFields().size());
    }
}