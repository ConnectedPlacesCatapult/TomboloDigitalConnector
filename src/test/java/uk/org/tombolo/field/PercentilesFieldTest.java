package uk.org.tombolo.field;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldSpecificationBuilder;
import uk.org.tombolo.SubjectSpecificationBuilder;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.TimedValueId;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class PercentilesFieldTest extends AbstractTest {
    private PercentilesField quartilesField;
    private PercentilesField quintilesInverseField;

    private Subject leeds1;
    private Subject leeds2;
    private Subject leeds3;
    private Subject leeds4;
    private Subject leeds5;

    private Subject brighton1;
    private Subject brighton2;
    private Subject brighton3;
    private Subject brighton4;
    private Subject brighton5;

    @Before
    public void setUp() throws Exception {
        quartilesField = (PercentilesField) FieldSpecificationBuilder.percentilesField(
                "populationDensity",
                4,
                false)
                .set("valueField", FieldSpecificationBuilder.latestValue(TestFactory.DEFAULT_PROVIDER.getLabel(), "populationDensity"))
                .set("normalizationSubjects", Collections.singletonList(new SubjectSpecificationBuilder("lsoa")))
                .build().toField();

        quintilesInverseField = (PercentilesField) FieldSpecificationBuilder.percentilesField(
                "populationDensity",
                5,
                true)
                .set("valueField", FieldSpecificationBuilder.latestValue(TestFactory.DEFAULT_PROVIDER.getLabel(), "populationDensity"))
                .set("normalizationSubjects", Collections.singletonList(new SubjectSpecificationBuilder("lsoa")))
                .build().toField();

        TestFactory.makeSubjectType("lsoa", "Lower Super Output Area");

        leeds1 = TestFactory.makeSubject("lsoa", "leeds1", "Leeds 1", null);
        leeds2 = TestFactory.makeSubject("lsoa", "leeds2", "Leeds 2", null);
        leeds3 = TestFactory.makeSubject("lsoa", "leeds3", "Leeds 3", null);
        leeds4 = TestFactory.makeSubject("lsoa", "leeds4", "Leeds 4", null);
        leeds5 = TestFactory.makeSubject("lsoa", "leeds5", "Leeds 5", null);

        brighton1 = TestFactory.makeSubject("lsoa", "brighton1", "Brighton 1", null);
        brighton2 = TestFactory.makeSubject("lsoa", "brighton2", "Brighton 2", null);
        brighton3 = TestFactory.makeSubject("lsoa", "brighton3", "Brighton 3", null);
        brighton4 = TestFactory.makeSubject("lsoa", "brighton4", "Brighton 4", null);
        brighton5 = TestFactory.makeSubject("lsoa", "brighton5", "Brighton 5", null);

        Attribute populationDensity = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "populationDensity");
        String timestamp = LocalDateTime.now().format(TimedValueId.DATE_TIME_FORMATTER);

        TestFactory.makeTimedValue("leeds1", populationDensity, timestamp, 1.0d);
        TestFactory.makeTimedValue("leeds2", populationDensity, timestamp, 2.0d);
        TestFactory.makeTimedValue("leeds3", populationDensity, timestamp, 3.0d);
        TestFactory.makeTimedValue("leeds4", populationDensity, timestamp, 4.0d);
        TestFactory.makeTimedValue("leeds5", populationDensity, timestamp, 5.0d);
        TestFactory.makeTimedValue("brighton1", populationDensity, timestamp, 6.0d);
        TestFactory.makeTimedValue("brighton2", populationDensity, timestamp, 7.0d);
        TestFactory.makeTimedValue("brighton3", populationDensity, timestamp, 8.0d);
        TestFactory.makeTimedValue("brighton4", populationDensity, timestamp, 9.0d);
        TestFactory.makeTimedValue("brighton5", populationDensity, timestamp, 100.0d);
    }

    @Test
    public void valueForSubjectQuartiles() throws Exception {
        assertEquals("1.0", quartilesField.valueForSubject(leeds1));
        assertEquals("1.0", quartilesField.valueForSubject(leeds2));
        assertEquals("2.0", quartilesField.valueForSubject(leeds3));
        assertEquals("2.0", quartilesField.valueForSubject(leeds4));
        assertEquals("2.0", quartilesField.valueForSubject(leeds5));
        assertEquals("3.0", quartilesField.valueForSubject(brighton1));
        assertEquals("3.0", quartilesField.valueForSubject(brighton2));
        assertEquals("3.0", quartilesField.valueForSubject(brighton3));
        assertEquals("4.0", quartilesField.valueForSubject(brighton4));
        assertEquals("4.0", quartilesField.valueForSubject(brighton5));
    }

    @Test
    public void valueForSubjectInverseQuintiles() throws Exception {
        assertEquals("5.0", quintilesInverseField.valueForSubject(leeds1));
        assertEquals("5.0", quintilesInverseField.valueForSubject(leeds2));
        assertEquals("4.0", quintilesInverseField.valueForSubject(leeds3));
        assertEquals("4.0", quintilesInverseField.valueForSubject(leeds4));
        assertEquals("3.0", quintilesInverseField.valueForSubject(leeds5));
        assertEquals("3.0", quintilesInverseField.valueForSubject(brighton1));
        assertEquals("2.0", quintilesInverseField.valueForSubject(brighton2));
        assertEquals("2.0", quintilesInverseField.valueForSubject(brighton3));
        assertEquals("1.0", quintilesInverseField.valueForSubject(brighton4));
        assertEquals("1.0", quintilesInverseField.valueForSubject(brighton5));
    }

    @Test
    public void getLabel() throws Exception {
        assertEquals("populationDensity", quartilesField.getLabel());
    }

    @Test
    public void getChildFields() throws Exception {
        List<Field> valueField = quartilesField.getChildFields();
        assertEquals(1, valueField.size());
        assertEquals("uk.org.tombolo.field.LatestValueField", valueField.get(0).getClass().getName());
    }

}