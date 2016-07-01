package uk.org.tombolo.exporter;

import org.geotools.geojson.geom.GeometryJSON;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.IncomputableFieldException;

import javax.json.JsonValue;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

public class GeoJsonExporter implements Exporter {
	private Logger log = LoggerFactory.getLogger(GeoJsonExporter.class);

	public void write(Writer writer, List<Subject> subjects, List<Field> fields) throws IOException {
		JSONObject root = new JSONObject();
		JSONArray features = new JSONArray();
		root.put("type", "FeatureCollection");
		root.put("features", features);

		root.put("features", subjects.stream()
				.map(subject -> subjectToFeature(fields, subject))
				.collect(Collectors.toList()));

		writer.write(root.toJSONString());
	}

	private JSONObject subjectToFeature(List<Field> fields, Subject subject) {
		// Subject is an a polygon or point for which data is to be output
		JSONObject feature = new JSONObject();
		JSONObject properties = new JSONObject();
		feature.put("type", "Feature");
		GeometryJSON geoJson = new GeometryJSON();
		properties.put("geometry", geoJson.toString(subject.getShape()));
		feature.put("properties", properties);

		properties.put("label", subject.getLabel());
		properties.put("name", subject.getName());

		for (Field field : fields){
			try {
				properties.putAll(field.jsonValueForSubject(subject));
			} catch (IncomputableFieldException e) {
				log.warn("Could not compute Field %s for Subject %s, reason: %s", field.getLabel(), subject.getLabel(), e.getMessage());
				properties.put(field.getLabel(), null);
			}
		}

		return feature;
	}
}
