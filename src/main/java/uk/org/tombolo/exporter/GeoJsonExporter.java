package uk.org.tombolo.exporter;

import com.google.gson.stream.JsonWriter;
import org.geotools.geojson.geom.GeometryJSON;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.DataExportRunner;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.Field;
import uk.org.tombolo.field.IncomputableFieldException;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class GeoJsonExporter implements Exporter {
	private Logger log = LoggerFactory.getLogger(GeoJsonExporter.class);
	private static final int LOGGING_FREQUENCY = 100;
	private Boolean timeStamp;

	public void write(Writer writer, List<Subject> subjects, List<Field> fields, Boolean timeStamp) throws IOException {
		this.timeStamp = null == timeStamp ? true : timeStamp;

		JsonWriter jsonWriter = new JsonWriter(writer);

		jsonWriter.beginObject();
		jsonWriter.name("type").value("FeatureCollection");
		jsonWriter.name("crs").jsonValue(getProjection().toJSONString());
		jsonWriter.name("features").beginArray();

		log.info("Exporting {} subjects", subjects.size());
		int subjectCounter = 0;
		for (Subject subject : subjects) {
			subjectCounter++;
			writeFeatureForSubject(fields, subject, jsonWriter);
			if (subjectCounter % LOGGING_FREQUENCY == 0)
				log.info("Exported {} subjects", subjectCounter);
		}
		log.info("Exported {} subjects", subjectCounter);

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

	private JSONObject getPropertiesForSubject(List<Field> fields, Subject subject) throws IOException {
		JSONObject properties = new JSONObject();

		properties.put("label", subject.getLabel());
		properties.put("name", subject.getName());

		fields.forEach(field -> {
			try {
				properties.putAll(field.jsonValueForSubject(subject, timeStamp));
			} catch (IncomputableFieldException e) {
				log.warn(DataExportRunner.YELLOW + "Could not compute Field {} for Subject {}, reason: {}" +
								DataExportRunner.END, field.getLabel(), subject.getLabel(), e.getMessage());
				properties.put(field.getLabel(), null);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(String.format("Could not compute Field %s for Subject %s" +
						"(%s), reason: %s", field.getLabel(), subject.getLabel(), subject.getId(), e.getMessage()));
			}
		});
		return properties;
	}

	private String getGeoJSONGeometryForSubject(Subject subject) {
		return (new GeometryJSON()).toString(subject.getShape());
	}

	private JSONObject getProjection() {
		JSONObject projection = new JSONObject();
		projection.put("name", "urn:ogc:def:crs:EPSG::" + Subject.SRID);

		JSONObject crs = new JSONObject();
		crs.put("type", "name");
		crs.put("properties", projection);
		return crs;
	}
}
