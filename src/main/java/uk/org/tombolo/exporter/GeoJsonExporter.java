package uk.org.tombolo.exporter;

import com.google.gson.stream.JsonWriter;
import org.geotools.geojson.geom.GeometryJSON;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.IncomputableFieldException;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class GeoJsonExporter implements Exporter {
	private Logger log = LoggerFactory.getLogger(GeoJsonExporter.class);

	public void write(Writer writer, List<Subject> subjects, List<Field> fields) throws IOException {
		JsonWriter jsonWriter = new JsonWriter(writer);

		jsonWriter.beginObject();
		jsonWriter.name("type").value("FeatureCollection");
		jsonWriter.name("features").beginArray();

		for (Subject subject : subjects) {
			writeFeatureForSubject(fields, subject, jsonWriter);
		}

		jsonWriter.endArray();
		jsonWriter.endObject();

		jsonWriter.close();
	}

	private void writeFeatureForSubject(List<Field> fields, Subject subject, JsonWriter jsonWriter) throws IOException {
		jsonWriter.beginObject();

		jsonWriter.name("type").value("Feature");
		jsonWriter.name("geometry").jsonValue(getGeoJSONGeometryForSubject(subject));
		jsonWriter.name("properties").jsonValue(getPropertiesForSubject(fields, subject).toJSONString());

		jsonWriter.endObject();
	}

	private JSONObject getPropertiesForSubject(List<Field> fields, Subject subject) {
		JSONObject properties = new JSONObject();

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
		return properties;
	}

	private String getGeoJSONGeometryForSubject(Subject subject) {
		return (new GeometryJSON()).toString(subject.getShape());
	}
}
