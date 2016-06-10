package uk.org.tombolo.exporter;

import org.geotools.geojson.geom.GeometryJSON;
import org.json.simple.JSONObject;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.field.Field;

import javax.json.JsonValue;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class GeoJsonExporter implements Exporter {

	// FIXME: Rewriter using geotools ... I could not get it to work quicly in the initial implementation (borkur)

	public void write(Writer writer, List<Subject> subjects, List<Field> fields) throws Exception {
		// Write beginning of subject list
		writer.write("{");
		writeStringProperty(writer, 0, "type", "FeatureCollection");
		writeObjectPropertyOpening(writer, 1, "features",JsonValue.ValueType.ARRAY);
		
		int subjectCount = 0;
		for (Subject subject : subjects){
			// Subject is an a polygon or point for which data is to be output

			if (subjectCount > 0){
				// This is not the first subject
				writer.write(",\n");
			}

			// Open subject object
			writer.write("{");
			writeStringProperty(writer, 0, "type","Feature");

			// Write geometry
			GeometryJSON geoJson = new GeometryJSON();
			StringWriter geoJsonWriter = new StringWriter();
			geoJson.write(subject.getShape(),geoJsonWriter);
			writer.write(", \"geometry\" : ");
			geoJson.write(subject.getShape(), writer);


			JSONObject properties = new JSONObject();
			properties.put("label", subject.getLabel());
			properties.put("name", subject.getName());

			for (Field field : fields){
				properties.putAll(field.jsonValueForSubject(subject));
			}

			writer.write(String.format(", \"properties\": %s", properties.toJSONString()));

			// Close subject object
			writer.write("}");

			subjectCount++;
		}
		
		// Write end of subject list
		writer.write("]}");
	}

	protected void writeStringProperty(Writer writer, int propertyCount, String key, String value) throws IOException{
		
		if (propertyCount > 0)
			writer.write(",");
		
		writer.write("\""+key+"\":\""+value+"\"");
	}

	protected void writeDoubleProperty(Writer writer, int propertyCount, String key, Double value) throws IOException{
		
		if (propertyCount > 0)
			writer.write(",");
		
		writer.write("\""+key+"\":"+value+"");
	}
	
	protected void writeObjectPropertyOpening(Writer writer, int propertyCount, String key, JsonValue.ValueType valueType) throws IOException{
		if (propertyCount > 0)
			writer.write(",");

		writer.write("\""+key+"\":");
		
		switch(valueType){
			case ARRAY:
				writer.write("[");
				break;
			case OBJECT:
				writer.write("{");
				break;
			default:
				break;	
		}
	}
}
