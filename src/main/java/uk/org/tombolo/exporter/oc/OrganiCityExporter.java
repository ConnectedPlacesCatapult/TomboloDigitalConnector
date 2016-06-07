package uk.org.tombolo.exporter.oc;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.json.JsonValue;

import org.geotools.geojson.geom.GeometryJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Point;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.execution.spec.AttributeSpecification;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.SubjectSpecification;
import uk.org.tombolo.exporter.Exporter;
import uk.org.tombolo.exporter.GeoJsonExporter;

public class OrganiCityExporter extends GeoJsonExporter implements Exporter {
	public static final String OC_SITE_NAME = "london";

	Logger log = LoggerFactory.getLogger(OrganiCityExporter.class);
	
	@Override
	public void write(Writer writer, DatasetSpecification datasetSpecification) throws Exception {

		// Write beginning of subject list
		writer.write("{");
		writeStringProperty(writer, 0, "type", "FeatureCollection");
		writeObjectPropertyOpening(writer, 1, "features",JsonValue.ValueType.ARRAY);
		
		int subjectCount = 0;
		for(SubjectSpecification subjectSpecification : datasetSpecification.getSubjectSpecification()){
			SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByLabel(subjectSpecification.getSubjectType());
			log.info("Getting subjects of type {} ({})", subjectType.getName(), subjectType.getLabel());
			List<Subject> subjectList = SubjectUtils
					.getSubjectBySpecification(subjectSpecification);
			String geoService = subjectSpecification.getAttributes().get("service");
			String geoProvider = subjectSpecification.getAttributes().get("provider");
			String geoGroup = subjectSpecification.getAttributes().get("group");
			String geoType = subjectSpecification.getAttributes().get("type");
			log.info("Writing subjects ...");
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
				log.info("Writing subject {}", subject.getName());
				GeometryJSON geoJson = new GeometryJSON();
				StringWriter geoJsonWriter = new StringWriter();
				geoJson.write(subject.getShape(),geoJsonWriter);
				writer.write(", \"geometry\" : ");
				geoJson.write(subject.getShape(), writer);

				// Open property list
				writeObjectPropertyOpening(writer, 1, "properties", JsonValue.ValueType.OBJECT);
				int propertyCount = 0;
				
				// Urn
				String urn = "urn:oc:entity:"
						+OC_SITE_NAME
						+((geoService != null)?":"+geoService:"")
						+((geoProvider != null)?":"+geoProvider:"")
						+((geoGroup != null)?":"+geoGroup:"")
						+":"+subject.getLabel();
				writeStringProperty(writer, propertyCount, "id", urn);
				propertyCount++;
				
				// Centroid
				Point centroid = subject.getShape().getCentroid();
				writeDoubleProperty(writer, propertyCount, "latitude", centroid.getY());
				propertyCount++;
				writeDoubleProperty(writer, propertyCount, "longitude", centroid.getX());
				propertyCount++;
				
				// Type
				writeStringProperty(writer, propertyCount, "type", geoType);
				propertyCount++;
				
				// Subject label
				writeStringProperty(writer, propertyCount, "label", subject.getLabel());
				propertyCount++;
				
				// Subject name
				writeStringProperty(writer, propertyCount, "name", subject.getName());
				propertyCount++;				
				
				// Write Attributes
				List<AttributeSpecification> attributeSpecs = datasetSpecification.getAttributeSpecification();
				writeObjectPropertyOpening(writer, propertyCount, "attributes", JsonValue.ValueType.OBJECT);
				int attributeCount = 0;
				for (AttributeSpecification attributeSpec : attributeSpecs){
					Provider provider = ProviderUtils.getByLabel(attributeSpec.getProviderLabel());
					Attribute attribute = AttributeUtils.getByProviderAndLabel(provider, attributeSpec.getAttributeLabel());
					
					// Write TimedValues
					writeAttributeProperty(writer, attributeCount, subject, attribute, attributeSpec);
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
		}
		
		// Write end of subject list
		writer.write("]}");
	}
}
