package uk.org.tombolo.exporter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.TestFactory;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.field.FixedAnnotationField;
import uk.org.tombolo.field.SubjectLatitudeField;
import uk.org.tombolo.field.SubjectLongitudeField;
import uk.org.tombolo.field.organicity.OrganicitySubjectIDField;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;


public class GeoJsonOrganicityExporterTest extends AbstractTest {
	GeoJsonExporter exporter = new GeoJsonExporter();

	@Before
	public void addSubjectFixtures() {
		TestFactory.makeNamedSubject("E09000001");
	}

	@Test
	public void testWrite() throws Exception{
		Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "attr");
		TestFactory.makeTimedValue("E09000001", attribute, TestFactory.TIMESTAMP, 100d);

		Writer writer = new StringWriter();
		
		exporter.write(writer,
				Collections.singletonList(SubjectUtils.getSubjectByLabel("E09000001")),
				Arrays.asList(
						new OrganicitySubjectIDField("id", "london", null, null, null),
						new SubjectLatitudeField("latitude"),
						new SubjectLongitudeField("longitude"),
						new FixedAnnotationField("type", "whatever!")
				));

		JSONParser parser = new JSONParser();
		JSONObject root = (JSONObject) parser.parse(writer.toString());
		JSONArray features = (JSONArray) root.get("features");
		JSONObject firstFeature = (JSONObject) features.get(0);
		JSONObject firstFeatureProperties = (JSONObject) firstFeature.get("properties");
		assertEquals("E09000001", firstFeatureProperties.get("label").toString());
		assertEquals("urn:oc:entity:london:E09000001", firstFeatureProperties.get("id").toString());
		assertEquals("0.0", firstFeatureProperties.get("latitude").toString());
		assertEquals("0.0", firstFeatureProperties.get("longitude").toString());
		assertEquals("whatever!", firstFeatureProperties.get("type").toString());
	}
}
