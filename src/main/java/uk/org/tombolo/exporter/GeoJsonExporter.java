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

	private class AttributeWrapper {
		private final String attributeLabel;
		private final String attributeName;
		private final String providerLabel;
		private final String providerName;
		private final Map<String, String> attributeAttributes;
		private final List<Map<String, Object>> timedValueWrappers;

		public AttributeWrapper(String attributeLabel, String attributeName, String providerLabel, String providerName, Map<String, String> attributeAttributes, List<Map<String, Object>> timedValueWrappers) {
			this.attributeLabel = attributeLabel;
			this.attributeName = attributeName;
			this.providerLabel = providerLabel;
			this.providerName = providerName;
			this.attributeAttributes = attributeAttributes;
			this.timedValueWrappers = timedValueWrappers;
		}
	}
	
	// FIXME: Rewriter using geotools ... I could not get it to work quicly in the initial implementation (borkur)

	public void writeInner(Writer writer, List<Subject> subjectList, List<Field> fields) throws Exception {

		// Write beginning of subject list
		writer.write("{");
		writeStringProperty(writer, 0, "type", "FeatureCollection");
		writeObjectPropertyOpening(writer, 1, "features",JsonValue.ValueType.ARRAY);
		
		int subjectCount = 0;
		for (Subject subject : subjectList){
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

	@Override
	public void write(Writer writer, List<Subject> subjects, List<Field> fields) throws Exception {
		writeInner(writer, subjects, fields);
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

	protected void writeAttributeProperty(Writer writer, int propertyCount, Subject subject, Field field) throws IOException{
		writeProperty(writer, propertyCount, subject, field);
	}

	private void writeProperty(Writer writer, int propertyCount, Subject subject, Field field) throws IOException {
		// Write attribute attributes (sic)
		// TODO: add this back in
//		if (attributeAttributes != null){
//
//			writeObjectPropertyOpening(writer, subPropertyCount, "attributes", JsonValue.ValueType.OBJECT);
//			int attributeAttributeCount = 0;
//			for (String attributeKey : attributeAttributes.keySet()){
//				writeStringProperty(writer, attributeAttributeCount, attributeKey, attributeAttributes.get(attributeKey));
//				attributeAttributeCount++;
//			}
//			writer.write("}");
//			subPropertyCount++;
//		}
	}
}
