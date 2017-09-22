package uk.org.tombolo.field.aggregation;

import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.FieldBuilder;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.recipe.RecipeDeserializer;
import uk.org.tombolo.importer.ons.AbstractONSImporter;
import uk.org.tombolo.recipe.SubjectRecipe;

import static org.junit.Assert.assertEquals;

public class MapToContainingSubjectFieldTest extends AbstractTest {
    private Subject subject;
    private MapToContainingSubjectField field;

    @Before
    public void setUp() {
        TestFactory.makeNamedSubjectType("localAuthority");
        field = new MapToContainingSubjectField("aLabel", new SubjectRecipe(AbstractONSImporter.PROVIDER.getLabel(), "localAuthority", null, null), makeFieldSpec());
        Subject containingSubject = TestFactory.makeNamedSubject("E09000001"); // Subject that contains subject below
        subject = TestFactory.makeNamedSubject("E01000001");
        Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr_label");
        TestFactory.makeTimedValue(containingSubject.getSubjectType(), "E09000001", attribute, "2011-01-01T00:00:00", 100d);
    }

    @Test
    public void testValueForSubject() throws Exception {
        String value = field.valueForSubject(subject, true);
        assertEquals("100.0", value);
    }

    @Test
    public void testJsonValueForSubject() throws Exception {
        String jsonString = field.jsonValueForSubject(subject, true).toJSONString();
        JSONAssert.assertEquals(
                "{ aLabel: 100.0}" ,jsonString,false);
    }

    private FieldRecipe makeFieldSpec() {
        return RecipeDeserializer.fromJson(
                FieldBuilder.latestValue("default_provider_label", "attr_label").toJSONString(),
                FieldRecipe.class);
    }
}