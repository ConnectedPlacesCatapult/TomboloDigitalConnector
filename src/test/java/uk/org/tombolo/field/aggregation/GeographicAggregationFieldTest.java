package uk.org.tombolo.field.aggregation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldSpecificationBuilder;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.execution.spec.FieldSpecification;
import uk.org.tombolo.execution.spec.SpecificationDeserializer;
import uk.org.tombolo.field.IncomputableFieldException;

import static org.junit.Assert.assertEquals;

public class GeographicAggregationFieldTest extends AbstractTest {
    Attribute attribute;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr");
    }

    @Test
    public void testValueForSubjectSum() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E09000001"); // Subject that contains subjects below
        TestFactory.makeNamedSubject("E01000001");
        TestFactory.makeNamedSubject("E01002766");
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-01T00:00:00", 300d);
        TestFactory.makeTimedValue("E01002766", attribute, "2011-01-01T00:00:00", 13.37d);

        GeographicAggregationField field = new GeographicAggregationField("aLabel", "lsoa", GeographicAggregationField.AggregationFunction.sum, makeFieldSpec());

        String value = field.valueForSubject(subject);
        assertEquals("313.37", value);
    }

    @Test
    public void testValueForSubjectSumWithNoValues() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E09000001"); // Subject with no contents

        GeographicAggregationField field = new GeographicAggregationField("aLabel", "lsoa", GeographicAggregationField.AggregationFunction.sum, makeFieldSpec());
        String value = field.valueForSubject(subject);
        assertEquals("0.0", value);
    }

    @Test
    public void testValueForSubjectMean() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E09000001"); // Subject that contains subjects below
        TestFactory.makeNamedSubject("E01000001");
        TestFactory.makeNamedSubject("E01002766");
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-01T00:00:00", 300d);
        TestFactory.makeTimedValue("E01002766", attribute, "2011-01-01T00:00:00", 13.37d);

        GeographicAggregationField field = new GeographicAggregationField("aLabel", "lsoa", GeographicAggregationField.AggregationFunction.mean, makeFieldSpec());

        String value = field.valueForSubject(subject);
        assertEquals("156.685", value);
    }

    @Test
    public void testValueForSubjectMeanWithNoValues() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E09000001"); // Subject that contains subjects below

        GeographicAggregationField field = new GeographicAggregationField("aLabel", "lsoa", GeographicAggregationField.AggregationFunction.mean, makeFieldSpec());

        thrown.expect(IncomputableFieldException.class);
        thrown.expectMessage("Aggregation function mean returned NaN (possible division by zero?)");

        field.valueForSubject(subject);
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E09000001"); // Subject that contains subjects below
        TestFactory.makeNamedSubject("E01000001");
        TestFactory.makeNamedSubject("E01002766");
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-01T00:00:00", 300d);
        TestFactory.makeTimedValue("E01002766", attribute, "2011-01-01T00:00:00", 13.37d);

        GeographicAggregationField field = new GeographicAggregationField("aLabel", "lsoa", GeographicAggregationField.AggregationFunction.sum, makeFieldSpec());

        String jsonString = field.jsonValueForSubject(subject).toJSONString();
        JSONAssert.assertEquals("{" +
                "  aLabel: 313.37"+
                "}", jsonString, false);
    }

    private FieldSpecification makeFieldSpec() {
        return SpecificationDeserializer.fromJson(
                FieldSpecificationBuilder.latestValue("default_provider_label", "attr").toJSONString(),
                FieldSpecification.class);
    }
}