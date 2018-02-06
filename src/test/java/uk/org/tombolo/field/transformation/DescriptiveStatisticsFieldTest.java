package uk.org.tombolo.field.transformation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.FieldBuilder;
import uk.org.tombolo.field.Field;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DescriptiveStatisticsFieldTest {

    DescriptiveStatisticsField meanField = new DescriptiveStatisticsField(
            "my-label",
            DescriptiveStatisticsField.Statistic.mean,
            Arrays.asList(
                    FieldBuilder.ConstantField("field1", "2").build(),
                    FieldBuilder.ConstantField("field2", "2").build(),
                    FieldBuilder.ConstantField("field3", "3").build(),
                    FieldBuilder.ConstantField("field4", "5").build()
            )
    );

    DescriptiveStatisticsField sumField = new DescriptiveStatisticsField(
            "my-label",
            DescriptiveStatisticsField.Statistic.sum,
            Arrays.asList(
                    FieldBuilder.ConstantField("field1", "2").build(),
                    FieldBuilder.ConstantField("field2", "2").build(),
                    FieldBuilder.ConstantField("field3", "3").build(),
                    FieldBuilder.ConstantField("field4", "5").build()
            )
    );

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testInitialiseNonSingeValueField(){
        DescriptiveStatisticsField noneSingelValueField = new DescriptiveStatisticsField(
                "my-label",
                DescriptiveStatisticsField.Statistic.sum,
                Arrays.asList(
                        FieldBuilder.ConstantField("field1", "1").build(),
                        FieldBuilder.valuesByTime("provider", "attribute").build()
                )
        );
        thrown.expect(Error.class);
        thrown.expectMessage("Parameters for DescriptiveStatisticsField must be of type SingleValueField");
        noneSingelValueField.initialize();
    }

    @Test
    public void getChildFields() throws Exception {
        List<Field> fields = meanField.getChildFields();

        assertEquals(4, fields.size());
        assertEquals("field1", fields.get(0).getLabel());
        assertEquals("field2", fields.get(1).getLabel());
        assertEquals("field3", fields.get(2).getLabel());
        assertEquals("field4", fields.get(3).getLabel());
    }

    @Test
    public void valueForSubject() throws Exception {
        // Mean
        assertEquals(3.0d,Double.parseDouble(meanField.valueForSubject(null, null)), 0.1d);

        // Sum
        assertEquals(12.0d,Double.parseDouble(sumField.valueForSubject(null, null)), 0.1d);
    }

    @Test
    public void jsonValueForSubject() throws Exception {
        String jsonString = meanField.jsonValueForSubject(null, false).toJSONString();
        JSONAssert.assertEquals("{my-label: 3}", jsonString, false);
    }

}
