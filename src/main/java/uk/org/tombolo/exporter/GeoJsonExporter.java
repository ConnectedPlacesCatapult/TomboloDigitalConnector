package uk.org.tombolo.exporter;

import org.geotools.geojson.geom.GeometryJSON;
import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.execution.spec.AttributeSpecification;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.field.Field;

import javax.json.JsonValue;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

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
	
	@Override
	public void write(Writer writer, DatasetSpecification datasetSpecification) throws Exception {
		TimedValueUtils timedValueUtils = new TimedValueUtils();
		List<Subject> subjectList = SubjectUtils.getSubjectBySpecification(datasetSpecification);

		Map<Subject, List<AttributeWrapper>> subjectsToAttributeWrappers = new HashMap<>();
		List<AttributeSpecification> attributeSpecs = datasetSpecification.getAttributeSpecification();
		for (Subject subject : subjectList) {
			ArrayList<AttributeWrapper> attributeWrappers = new ArrayList<>();
			subjectsToAttributeWrappers.put(subject, attributeWrappers);
			for (AttributeSpecification attributeSpec : attributeSpecs) {
				Provider provider = ProviderUtils.getByLabel(attributeSpec.getProviderLabel());
				Attribute attribute = AttributeUtils.getByProviderAndLabel(provider, attributeSpec.getAttributeLabel());
				List<Map<String, Object>> timedValueWrappers = timedValueUtils.getBySubjectAndAttribute(subject, attribute).stream().map(timedValue -> {
					Map<String, Object> timedValueWrapper = new HashMap<>();
					timedValueWrapper.put("timestamp", timedValue.getId().getTimestamp().toString());
					timedValueWrapper.put("value", timedValue.getValue());
					return timedValueWrapper;
				}).collect(Collectors.toList());

				attributeWrappers.add(new AttributeWrapper(attribute.getLabel(), attribute.getName(), attribute.getProvider().getLabel(), attribute.getProvider().getName(), attributeSpec.getAttributes(), timedValueWrappers));
			}
		}

		writeInner(writer, subjectList, subjectsToAttributeWrappers);
	}

	public void writeInner(Writer writer, List<Subject> subjectList, Map<Subject, List<AttributeWrapper>> subjectsToAttributeWrappers) throws Exception {

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

			// Open property list
			writeObjectPropertyOpening(writer, 1, "properties", JsonValue.ValueType.OBJECT);
			int propertyCount = 0;

			// Subject label
			writeStringProperty(writer, propertyCount, "label", subject.getLabel());
			propertyCount++;

			// Subject name
			writeStringProperty(writer, propertyCount, "name", subject.getName());
			propertyCount++;

			// Write Attributes
			writeObjectPropertyOpening(writer, propertyCount, "attributes", JsonValue.ValueType.OBJECT);
			int attributeCount = 0;
			for (AttributeWrapper attributeWrapper : subjectsToAttributeWrappers.get(subject)){
				// Write TimedValues
				writeAttributeProperty(writer, attributeCount, subject, attributeWrapper);
				attributeCount++;
			}
			// Close attribute list
			writer.write("}");
			propertyCount++;

			// Close property list
			writer.write("}");

			// Close subject object
			writer.write("}");

			subjectCount++;
		}
		
		// Write end of subject list
		writer.write("]}");
	}

	@Override
	public void write(Writer writer, List<Subject> subjects, List<Field> fields) throws Exception {
		Map<Subject, List<AttributeWrapper>> subjectsToAttributeWrappers = new HashMap<>();
		for (Subject subject : subjects) {
			ArrayList<AttributeWrapper> attributeWrappers = new ArrayList<>();
			subjectsToAttributeWrappers.put(subject, attributeWrappers);
			for (Field field : fields) {
				Map<String, Object> valueWrapper = new HashMap<String, Object>();
				valueWrapper.put("timestamp", "latest");
				valueWrapper.put("value", Double.parseDouble(field.valueForSubject(subject)));
				attributeWrappers.add(new AttributeWrapper(
						field.getLabel(),
						field.getLabel() + "_name",
						field.getLabel() + "_provider_label",
						field.getLabel() + "_provider_name",
						null,
						Arrays.asList(valueWrapper)));
			}
		}

		writeInner(writer, subjects, subjectsToAttributeWrappers);
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

	protected void writeAttributeProperty(Writer writer, int propertyCount, Subject subject, AttributeWrapper attributeWrapper) throws IOException{
		writeProperty(writer, propertyCount, subject, attributeWrapper.attributeLabel, attributeWrapper.attributeName, attributeWrapper.providerName, attributeWrapper.attributeAttributes, attributeWrapper.timedValueWrappers);
	}

	private void writeProperty(Writer writer, int propertyCount, Subject subject, String attributeLabel, String attributeName, String providerName, Map<String, String> attributeAttributes, List<Map<String, Object>> timedValueWrappers) throws IOException {
		// Open attribute
		writeObjectPropertyOpening(writer, propertyCount, attributeLabel, JsonValue.ValueType.OBJECT);
		int subPropertyCount = 0;

		// Write name
		writeStringProperty(writer, subPropertyCount, "name", attributeName);
		subPropertyCount++;

		// Write provider
		writeStringProperty(writer, subPropertyCount, "provider", providerName);
		subPropertyCount++;

		// Write attribute attributes (sic)
		if (attributeAttributes != null){

			writeObjectPropertyOpening(writer, subPropertyCount, "attributes", JsonValue.ValueType.OBJECT);
			int attributeAttributeCount = 0;
			for (String attributeKey : attributeAttributes.keySet()){
				writeStringProperty(writer, attributeAttributeCount, attributeKey, attributeAttributes.get(attributeKey));
				attributeAttributeCount++;
			}
			writer.write("}");
			subPropertyCount++;
		}

		// Write timed values

		// Open values
		writeObjectPropertyOpening(writer, subPropertyCount, "values", JsonValue.ValueType.OBJECT);
		int valueCount = 0;
		for (Map<String, Object> valueWrapper : timedValueWrappers){
			writeDoubleProperty(writer, valueCount, (String) valueWrapper.get("timestamp"), (Double) valueWrapper.get("value"));
			valueCount++;
		}
		// Close values
		writer.write("}");
		subPropertyCount++;

		// Close attribute
		writer.write("}");
	}
}
