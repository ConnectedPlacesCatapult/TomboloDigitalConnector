package uk.org.tombolo.exporter;

import static org.junit.Assert.*;
import static uk.org.tombolo.execution.spec.GeographySpecification.*;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import uk.org.tombolo.AbstractTest;
import uk.org.tombolo.execution.spec.AttributeSpecification;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;


public class GeoJsonExporterTest extends AbstractTest {
	GeoJsonExporter exporter = new GeoJsonExporter();
	
	@Test
	public void testWrite() throws Exception{
		Writer writer = new StringWriter();
		DatasetSpecification spec = new DatasetSpecification();
		List<GeographySpecification> geographySpecification = new ArrayList<GeographySpecification>();
		List<GeographyMatcher> matchers = Arrays.asList(new GeographyMatcher("label", "E09%"));
		geographySpecification.add(new GeographySpecification(matchers, "localAuthority"));
		List<AttributeSpecification> attributeSpecification = new ArrayList<AttributeSpecification>();
		attributeSpecification.add(new AttributeSpecification("uk.gov.london", "populationDensity"));
		spec.setGeographySpecification(geographySpecification);
		spec.setAttributeSpecification(attributeSpecification);
		
		exporter.write(writer, spec);
		writer.flush();

		assertNotNull(getFirstFeatureLabel(writer.toString()));
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
