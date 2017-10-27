package uk.org.tombolo.field.transformation;

import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldBuilder;
import uk.org.tombolo.SubjectSpecificationBuilder;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.TimedValueId;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.importer.ons.AbstractONSImporter;

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
        quartilesField = (PercentilesField) FieldBuilder.percentilesField(
                "populationDensity",
                4,
                false)
                .set("valueField", FieldBuilder.latestValue(TestFactory.DEFAULT_PROVIDER.getLabel(), "populationDensity"))
                .set("normalizationSubjects", Collections.singletonList(new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "lsoa")))
                .build().toField();

        quintilesInverseField = (PercentilesField) FieldBuilder.percentilesField(
                "populationDensity",
                5,
                true)
                .set("valueField", FieldBuilder.latestValue(TestFactory.DEFAULT_PROVIDER.getLabel(), "populationDensity"))
                .set("normalizationSubjects", Collections.singletonList(new SubjectSpecificationBuilder(AbstractONSImporter.PROVIDER.getLabel(), "lsoa")))
                .build().toField();

        SubjectType lsoa = TestFactory.makeNamedSubjectType("lsoa");

        leeds1 = TestFactory.makeSubject(lsoa, "leeds1", "Leeds 1", null);
        leeds2 = TestFactory.makeSubject(lsoa, "leeds2", "Leeds 2", null);
        leeds3 = TestFactory.makeSubject(lsoa, "leeds3", "Leeds 3", null);
        leeds4 = TestFactory.makeSubject(lsoa, "leeds4", "Leeds 4", null);
        leeds5 = TestFactory.makeSubject(lsoa, "leeds5", "Leeds 5", null);

        brighton1 = TestFactory.makeSubject(lsoa, "brighton1", "Brighton 1", null);
        brighton2 = TestFactory.makeSubject(lsoa, "brighton2", "Brighton 2", null);
        brighton3 = TestFactory.makeSubject(lsoa, "brighton3", "Brighton 3", null);
        brighton4 = TestFactory.makeSubject(lsoa, "brighton4", "Brighton 4", null);
        brighton5 = TestFactory.makeSubject(lsoa, "brighton5", "Brighton 5", null);

        Attribute populationDensity = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "populationDensity");
        String timestamp = LocalDateTime.now().format(TimedValueId.DATE_TIME_FORMATTER);

        TestFactory.makeTimedValue(leeds1.getSubjectType(), "leeds1", populationDensity, timestamp, 1.0d);
        TestFactory.makeTimedValue(leeds2.getSubjectType(), "leeds2", populationDensity, timestamp, 2.0d);
        TestFactory.makeTimedValue(leeds3.getSubjectType(), "leeds3", populationDensity, timestamp, 3.0d);
        TestFactory.makeTimedValue(leeds4.getSubjectType(), "leeds4", populationDensity, timestamp, 4.0d);
        TestFactory.makeTimedValue(leeds5.getSubjectType(), "leeds5", populationDensity, timestamp, 5.0d);
        TestFactory.makeTimedValue(brighton1.getSubjectType(), "brighton1", populationDensity, timestamp, 6.0d);
        TestFactory.makeTimedValue(brighton2.getSubjectType(), "brighton2", populationDensity, timestamp, 7.0d);
        TestFactory.makeTimedValue(brighton3.getSubjectType(), "brighton3", populationDensity, timestamp, 8.0d);
        TestFactory.makeTimedValue(brighton4.getSubjectType(), "brighton4", populationDensity, timestamp, 9.0d);
        TestFactory.makeTimedValue(brighton5.getSubjectType(), "brighton5", populationDensity, timestamp, 100.0d);
    }

    @Test
    public void valueForSubjectQuartiles() throws Exception {
        assertEquals("1.0", quartilesField.valueForSubject(leeds1, true));
        assertEquals("1.0", quartilesField.valueForSubject(leeds2, true));
        assertEquals("2.0", quartilesField.valueForSubject(leeds3, true));
        assertEquals("2.0", quartilesField.valueForSubject(leeds4, true));
        assertEquals("2.0", quartilesField.valueForSubject(leeds5, true));
        assertEquals("3.0", quartilesField.valueForSubject(brighton1, true));
        assertEquals("3.0", quartilesField.valueForSubject(brighton2, true));
        assertEquals("3.0", quartilesField.valueForSubject(brighton3, true));
        assertEquals("4.0", quartilesField.valueForSubject(brighton4, true));
        assertEquals("4.0", quartilesField.valueForSubject(brighton5, true));
    }

    @Test
    public void valueForSubjectInverseQuintiles() throws Exception {
        assertEquals("5.0", quintilesInverseField.valueForSubject(leeds1, false));
        assertEquals("5.0", quintilesInverseField.valueForSubject(leeds2, false));
        assertEquals("4.0", quintilesInverseField.valueForSubject(leeds3, false));
        assertEquals("4.0", quintilesInverseField.valueForSubject(leeds4, false));
        assertEquals("3.0", quintilesInverseField.valueForSubject(leeds5, false));
        assertEquals("3.0", quintilesInverseField.valueForSubject(brighton1, null));
        assertEquals("2.0", quintilesInverseField.valueForSubject(brighton2, null));
        assertEquals("2.0", quintilesInverseField.valueForSubject(brighton3, null));
        assertEquals("1.0", quintilesInverseField.valueForSubject(brighton4, null));
        assertEquals("1.0", quintilesInverseField.valueForSubject(brighton5, null));
    }

    @Test
    public void getLabel() throws Exception {
        assertEquals("populationDensity", quartilesField.getLabel());
    }

    @Test
    public void getChildFields() throws Exception {
        List<Field> valueField = quartilesField.getChildFields();
        assertEquals(1, valueField.size());
        assertEquals("uk.org.tombolo.field.value.LatestValueField", valueField.get(0).getClass().getName());
        assertEquals("populationDensity", valueField.get(0).getLabel());
    }

}