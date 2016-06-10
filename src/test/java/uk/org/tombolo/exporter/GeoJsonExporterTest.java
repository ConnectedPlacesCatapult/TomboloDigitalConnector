package uk.org.tombolo.exporter;

import com.jayway.jsonpath.JsonPath;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.execution.spec.AttributeSpecification;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.SubjectSpecification;
import uk.org.tombolo.field.FieldWithProvider;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static uk.org.tombolo.execution.spec.SubjectSpecification.SubjectMatcher;


public class GeoJsonExporterTest extends AbstractTest {
	GeoJsonExporter exporter = new GeoJsonExporter();

	private class FixedValueField implements FieldWithProvider {
		private final String label;
		private final Double value;

		public FixedValueField(String label, Double value) {
			this.label = label;
			this.value = value;
		}

		@Override
		public JSONObject jsonValueForSubject(Subject subject) {
			JSONObject obj = new JSONObject();
			obj.put("latest", value);
			return obj;
		}

		@Override
		public String getLabel() {
			return label;
		}

		@Override
		public String getHumanReadableName() {
			return label + "_readable";
		}

		@Override
		public Provider getProvider() {
			return new Provider("providerTestLabel", "providerTestName");
		}
	}

	@Before
	public void addSubjectFixtures() {
		TestFactory.makeNamedSubject("E09000001");
	}

	@Test
	public void testWrite() throws Exception{
		Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr");
		TestFactory.makeTimedValue("E09000001", attribute, TestFactory.TIMESTAMP, 100d);

		Writer writer = new StringWriter();
		DatasetSpecification spec = new DatasetSpecification();
		List<SubjectSpecification> subjectSpecification = new ArrayList<SubjectSpecification>();
		List<SubjectMatcher> matchers = Arrays.asList(new SubjectMatcher("label", "E09000001"));
		subjectSpecification.add(new SubjectSpecification(matchers, "localAuthority"));
		List<AttributeSpecification> attributeSpecification = new ArrayList<AttributeSpecification>();
		attributeSpecification.add(new AttributeSpecification("default_provider_label", "attr_label"));
		spec.setSubjectSpecification(subjectSpecification);
		spec.setAttributeSpecification(attributeSpecification);
		
		exporter.write(writer, spec);

		assertEquals("E09000001", getFirstFeatureLabel(writer.toString()));
	}

	@Test
	public void testWriteWithFields() throws Exception {
		Writer writer = new StringWriter();

		exporter.write(writer,
				Arrays.asList(SubjectUtils.getSubjectByLabel("E09000001")),
				Arrays.asList(new FixedValueField("some_label", 100d))
		);

		assertEquals("providerTestName", JsonPath.read(writer.toString(), "$.features[0].properties.attributes.some_label.provider").toString());
		assertEquals("some_label_readable", JsonPath.read(writer.toString(), "$.features[0].properties.attributes.some_label.name").toString());
		assertEquals("100.0", JsonPath.read(writer.toString(), "$.features[0].properties.attributes.some_label.values.latest").toString());
	}

	private String getFirstFeatureLabel(String jsonString) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject root = (JSONObject) parser.parse(jsonString);
		JSONArray features = (JSONArray) root.get("features");
		JSONObject firstFeature = (JSONObject) features.get(0);
		JSONObject firstFeatureProperties = (JSONObject) firstFeature.get("properties");
		return firstFeatureProperties.get("label").toString();
	}
}
