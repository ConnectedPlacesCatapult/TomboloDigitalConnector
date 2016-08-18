package uk.org.tombolo.exporter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.execution.spec.AttributeMatcher;
import uk.org.tombolo.field.FixedAnnotationField;
import uk.org.tombolo.field.ValuesByTimeField;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;


public class GeoJsonExporterTest extends AbstractTest {
	GeoJsonExporter exporter = new GeoJsonExporter();

	@Before
	public void addSubjectFixtures() {
		TestFactory.makeNamedSubject("E09000001");
	}

	@Test
	public void testWrite() throws Exception{
		Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr_label");
		TestFactory.makeTimedValue("E09000001", attribute, TestFactory.TIMESTAMP, 100d);

		Writer writer = new StringWriter();
		
		exporter.write(writer, Collections.singletonList(
				SubjectUtils.getSubjectByLabel("E09000001")
		), Collections.singletonList(
				new ValuesByTimeField("attr_label",
						new AttributeMatcher("default_provider_label", "attr_label"))
		));

		assertEquals("E09000001", getFirstFeatureLabel(writer.toString()));
	}

	@Test
	public void testWriteWithFields() throws Exception {
		Writer writer = new StringWriter();

		exporter.write(writer,
				Arrays.asList(SubjectUtils.getSubjectByLabel("E09000001")),
				Arrays.asList(new FixedAnnotationField("some_label", "some_value"))
		);

		JSONAssert.assertEquals("{" +
				"  type: 'FeatureCollection'," +
				"  features: [" +
				"    {" +
				"      type: 'Feature'," +
				"      geometry: {" +
				"        type: 'Point'," +
				"        coordinates: [0.0, 0.0]" +
				"      }," +
				"      properties: {" +
				"        some_label: 'some_value'," +
				"        name: 'City of London'," +
				"        label: 'E09000001'" +
				"      }" +
				"    }" +
				"  ]" +
				"}", writer.toString(), false);
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
