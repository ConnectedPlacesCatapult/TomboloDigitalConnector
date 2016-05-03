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
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.GeographyType;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.GeographyTypeUtils;
import uk.org.tombolo.core.utils.GeographyUtils;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.execution.spec.AttributeSpecification;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;
import uk.org.tombolo.exporter.Exporter;
import uk.org.tombolo.exporter.GeoJsonExporter;

public class OrganiCityExporter extends GeoJsonExporter implements Exporter {
	public static final String OC_SITE_NAME = "london";

	Logger log = LoggerFactory.getLogger(OrganiCityExporter.class);
	
	@Override
	public void write(Writer writer, DatasetSpecification datasetSpecification) throws Exception {

		// Write beginning of geography list
		writer.write("{");
		writeStringProperty(writer, 0, "type", "FeatureCollection");
		writeObjectPropertyOpening(writer, 1, "features",JsonValue.ValueType.ARRAY);
		
		int geographyCount = 0;
		for(GeographySpecification geographySpecification : datasetSpecification.getGeographySpecification()){
			GeographyType geographyType = GeographyTypeUtils.getGeographyTypeByLabel(geographySpecification.getGeographyType());
			log.info("Getting geographies of type {} ({})", geographyType.getName(), geographyType.getLabel());
			List<Geography> geographyList = GeographyUtils
					.getGeographyByTypeAndLabelPattern(geographyType, geographySpecification.getLabelPattern());
			String geoService = geographySpecification.getAttributes().get("service");
			String geoProvider = geographySpecification.getAttributes().get("provider");
			String geoGroup = geographySpecification.getAttributes().get("group");
			String geoType = geographySpecification.getAttributes().get("type");
			log.info("Writing geographies ...");
			for (Geography geography : geographyList){
				// Geography is an a polygon or point for which data is to be output

				if (geographyCount > 0){
					// This is not the first geography
					writer.write(",\n");
				}
								
				// Open geography object
				writer.write("{");
				writeStringProperty(writer, 0, "type","Feature");
								
				// Write geometry
				log.info("Writing geography {}", geography.getName());
				GeometryJSON geoJson = new GeometryJSON();
				StringWriter geoJsonWriter = new StringWriter();
				geoJson.write(geography.getShape(),geoJsonWriter);
				writer.write(", \"geometry\" : ");
				geoJson.write(geography.getShape(), writer);

				// Open property list
				writeObjectPropertyOpening(writer, 1, "properties", JsonValue.ValueType.OBJECT);
				int propertyCount = 0;
				
				// Urn
				String urn = "urn:oc:entity:"
						+OC_SITE_NAME
						+((geoService != null)?":"+geoService:"")
						+((geoProvider != null)?":"+geoProvider:"")
						+((geoGroup != null)?":"+geoGroup:"")
						+":"+geography.getLabel();
				writeStringProperty(writer, propertyCount, "id", urn);
				propertyCount++;
				
				// Centroid
				Point centroid = geography.getShape().getCentroid();
				writeDoubleProperty(writer, propertyCount, "latitude", centroid.getY());
				propertyCount++;
				writeDoubleProperty(writer, propertyCount, "longitude", centroid.getX());
				propertyCount++;
				
				// Type
				writeStringProperty(writer, propertyCount, "type", geoType);
				propertyCount++;
				
				// Geography label
				writeStringProperty(writer, propertyCount, "label", geography.getLabel());
				propertyCount++;
				
				// Geography name
				writeStringProperty(writer, propertyCount, "name", geography.getName());
				propertyCount++;				
				
				// Write Attributes
				List<AttributeSpecification> attributeSpecs = datasetSpecification.getAttributeSpecification();
				writeObjectPropertyOpening(writer, propertyCount, "attributes", JsonValue.ValueType.OBJECT);
				int attributeCount = 0;
				for (AttributeSpecification attributeSpec : attributeSpecs){
					Provider provider = ProviderUtils.getByLabel(attributeSpec.getProviderLabel());
					Attribute attribute = AttributeUtils.getByProviderAndLabel(provider, attributeSpec.getAttributeLabel());
					
					// Write TimedValues
					writeAttributeProperty(writer, attributeCount, geography, attribute, attributeSpec);
					attributeCount++;
				}
				// Close attribute list
				writer.write("}");
				propertyCount++;
				
				// Close property list
				writer.write("}");
				
				// Close geography object
				writer.write("}");
				
				geographyCount++;
			}
		}
		
		// Write end of geography list
		writer.write("]}");
	}
}
