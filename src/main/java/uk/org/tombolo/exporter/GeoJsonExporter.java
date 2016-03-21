package uk.org.tombolo.exporter;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.postgis.MultiPolygon;

import com.google.gson.Gson;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.GeographyType;
import uk.org.tombolo.core.utils.GeographyTypeUtils;
import uk.org.tombolo.core.utils.GeographyUtils;
import uk.org.tombolo.execution.spec.DatasetSpecification;
import uk.org.tombolo.execution.spec.GeographySpecification;

public class GeoJsonExporter implements Exporter {

	Gson gson = new Gson();
	
	@Override
	public void write(Writer writer, DatasetSpecification datasetSpecification) throws IOException, ParseException {
		
		// TODO Write Geographies
		//List<Map<String,Object>> geographies = new ArrayList<Map<String,Object>>();
		DefaultFeatureCollection geographies = new DefaultFeatureCollection();
		for(GeographySpecification geographySpecification : datasetSpecification.getGeographySpecification()){
			GeographyType geographyType = GeographyTypeUtils.getGeographyTypeByLabel(geographySpecification.getGeographyType());
			List<Geography> geographyList = GeographyUtils
					.getGeographyByTypeAndLabelPattern(geographyType, geographySpecification.getLabelPattern());
			for (Geography geography : geographyList){

				SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
				typeBuilder.setName("Geography");
				typeBuilder.add("label", String.class);
				typeBuilder.add("geometry", MultiPolygon.class);

				SimpleFeatureType featureType = typeBuilder.buildFeatureType();

				SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
				featureBuilder.set("label", geography.getLabel());
				featureBuilder.set("geometry", geography.getShape());

				SimpleFeature feature = featureBuilder.buildFeature(geography.getLabel());

				GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
				WKTReader reader = new WKTReader(geometryFactory);	
				Geometry geometry = reader.read(geography.getShape().toText());

				feature.setDefaultGeometry(geometry);

				geographies.add(feature);

				//GeometryJSON geoJson = new GeometryJSON();
				//StringWriter geoJsonWriter = new StringWriter();
				//geoJson.write(geography.getShape(),geoJsonWriter);
				//FeatureObject fo = new FeatureObject();

				//Map<String,Object> geographyJson = new LinkedHashMap<String,Object>();
				//geographyJson.put("label", geography.getLabel());
				//geographyJson.put("name", geography.getName());
				//geographyJson.put("shape", geoJson);
				//geographies.add(geographyJson);


			}
			FeatureJSON featureJson = new FeatureJSON();
			featureJson.writeFeatureCollection(geographies, writer);


			// TODO Write Attributes

			// TODO Write TimedValues

	}
	}
}
