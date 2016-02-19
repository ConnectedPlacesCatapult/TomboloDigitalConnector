package uk.org.tombolo.writer;

import java.io.Writer;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.reader.FeatureMatrix;

public class JsonDatapack {

	List<Attribute> attributes;
	List<Entity> entities;
	
	public JsonDatapack(List<Attribute> attributes, List<Entity> entities){
		this.attributes = attributes;
		this.entities = entities;
	}

	public void mergeFeatureMatrix(FeatureMatrix featureMatrix){
		for (Entity entity : entities){
			entity.setAttributeValues(featureMatrix.getAttributeNameToValueSeriesByEntityId(entity.id));
		}
	}
	
	public void toJson(Writer writer) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();;
		gson.toJson(this, writer);
	}
	
}
