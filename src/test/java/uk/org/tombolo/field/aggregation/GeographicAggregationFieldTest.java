package uk.org.tombolo.field.aggregation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldBuilder;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.IncomputableFieldException;
import uk.org.tombolo.importer.ons.AbstractONSImporter;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.recipe.RecipeDeserializer;
import uk.org.tombolo.recipe.SubjectRecipe;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class GeographicAggregationFieldTest extends AbstractTest {
    private Attribute attribute;
    private SubjectType localAuthority;
    private SubjectType lsoa;

    private GeographicAggregationField sumField = new GeographicAggregationField("aLabel",
            new SubjectRecipe(AbstractONSImporter.PROVIDER.getLabel(), "lsoa", null, null),
            GeographicAggregationField.AggregationFunction.sum, makeFieldSpec());

    private GeographicAggregationField meanField = new GeographicAggregationField("aLabel",
            new SubjectRecipe(AbstractONSImporter.PROVIDER.getLabel(),"lsoa", null, null),
            GeographicAggregationField.AggregationFunction.mean, makeFieldSpec());


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr");
        localAuthority = TestFactory.makeNamedSubjectType("localAuthority");
        lsoa = TestFactory.makeNamedSubjectType("lsoa");
    }

    @Test
    public void testValueForSubjectSum() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E09000001"); // Subject that contains subjects below
        TestFactory.makeNamedSubject("E01000001");
        TestFactory.makeNamedSubject("E01002766");
        TestFactory.makeTimedValue(lsoa, "E01000001", attribute, "2011-01-01T00:00:00", 300d);
        TestFactory.makeTimedValue(lsoa, "E01002766", attribute, "2011-01-01T00:00:00", 13.37d);

        String value = sumField.valueForSubject(subject, true);
        assertEquals("313.37", value);
    }

    @Test
    public void testValueForSubjectSumWithNoValues() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E09000001"); // Subject with no contents

        String value = sumField.valueForSubject(subject, true);
        assertEquals("0.0", value);
    }

    @Test
    public void testValueForSubjectMean() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E09000001"); // Subject that contains subjects below
        TestFactory.makeNamedSubject("E01000001");
        TestFactory.makeNamedSubject("E01002766");
        TestFactory.makeTimedValue(lsoa, "E01000001", attribute, "2011-01-01T00:00:00", 300d);
        TestFactory.makeTimedValue(lsoa, "E01002766", attribute, "2011-01-01T00:00:00", 13.37d);

        String value = meanField.valueForSubject(subject, true);
        assertEquals("156.685", value);
    }

    @Test
    public void testValueForSubjectMeanWithNoValues() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E09000001"); // Subject that contains subjects below

        thrown.expect(IncomputableFieldException.class);
        thrown.expectMessage("Aggregation function mean returned NaN (possible division by zero?)");
        meanField.valueForSubject(subject, true);
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        Subject subject = TestFactory.makeNamedSubject("E09000001"); // Subject that contains subjects below
        TestFactory.makeNamedSubject("E01000001");
        TestFactory.makeNamedSubject("E01002766");
        TestFactory.makeTimedValue(lsoa, "E01000001", attribute, "2011-01-01T00:00:00", 300d);
        TestFactory.makeTimedValue(lsoa, "E01002766", attribute, "2011-01-01T00:00:00", 13.37d);

        String jsonString = sumField.jsonValueForSubject(subject, true).toJSONString();
        JSONAssert.assertEquals("{" +
                "  aLabel: 313.37"+
                "}", jsonString, false);
    }

    @Test
    public void testGetChildFields(){
        List<Field> childFields = sumField.getChildFields();
        assertEquals(1, childFields.size());
        assertEquals("attr", childFields.get(0).getLabel());
    }

    private FieldRecipe makeFieldSpec() {
        return RecipeDeserializer.fromJson(
                FieldBuilder.latestValue("default_provider_label", "attr").toJSONString(),
                FieldRecipe.class);
    }
}