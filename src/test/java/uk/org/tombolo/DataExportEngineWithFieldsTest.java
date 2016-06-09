package uk.org.tombolo;

import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.core.Attribute;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DataExportEngineWithFieldsTest extends AbstractTest {
    DataExportEngine engine = new DataExportEngine(makeTestDownloadUtils());
    DataExportSpecificationBuilder builder = DataExportSpecificationBuilder.withGeoJsonExporter();
    Writer writer = new StringWriter();

    @Before
    public void addSubjectFixtures() {
        TestFactory.makeNamedSubject("E01000001");
        TestFactory.makeNamedSubject("E09000001");
        TestFactory.makeNamedSubject("E01002766");
        TestFactory.makeNamedSubject("E08000035");
    }

    @Test
    public void testReturnsEmptyOnBlankSpec() throws Exception {
        engine.execute(builder.build(), writer, true);

        assertThat(writer.toString(), hasJsonPath("$.features", hasSize(0)));
    }

    @Test
    public void testReturnsSubject() throws Exception {
        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("lsoa").addMatcher("label", "E01000001")
        );

        engine.execute(builder.build(), writer, true);

        assertThat(writer.toString(), hasJsonPath("$.features", hasSize(1)));
        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.label", equalTo("E01000001")));
    }

    @Test
    public void testReturnsSubjectAndAttribute() throws Exception {
        Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr");
        TestFactory.makeTimedValue("E01000001", attribute, "2011-01-01T00:00:00", 100d);

        builder.addSubjectSpecification(
                new SubjectSpecificationBuilder("lsoa").addMatcher("label", "E01000001")
        ).addFieldSpecification(
                FieldSpecificationBuilder.latestValue("default_provider_label", "attr_label")
        );

        engine.execute(builder.build(), writer, true);

        assertThat(writer.toString(), hasJsonPath("$.features[0].properties.attributes.attr_label.name", equalTo("attr_label_name")));
        assertHasOnlyTimedValues(writer.toString(),
                new TimedValueMatcher("E01000001", "attr_label", "100.0"));
    }

    private void assertHasOnlyTimedValues(String json, TimedValueMatcher ...matchers) {
        List<Integer> allTimedAttributes = JsonPath.parse(json).read("$.features..properties.attributes.*");
        assertEquals("Number of matchers does not match number of values", matchers.length, allTimedAttributes.size());
        for (TimedValueMatcher matcher : matchers) {
            assertHasTimedValue(json, matcher);
        }
    }

    private void assertHasTimedValue(String json, TimedValueMatcher matcher) {
        ArrayList<Map<String, Object>> features = JsonPath.parse(json).read("$.features[?]",
                Filter.filter(Criteria.where("properties.label").is(matcher.subjectLabel)));
        assertEquals(String.format("Wrong number of features found for label %s", matcher.subjectLabel), 1, features.size());
        assertEquals(matcher.value, JsonPath.parse(features.get(0)).read("$.properties.attributes." + matcher.attributeName + ".values.latest").toString());
    }

    private static class TimedValueMatcher {
        String subjectLabel;
        String attributeName;
        String value;

        TimedValueMatcher(String subjectLabel, String attributeName, String value) {
            this.subjectLabel = subjectLabel;
            this.attributeName = attributeName;
            this.value = value;
        }
    }
}