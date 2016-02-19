package uk.org.tombolo.writer;

import java.util.Map;
import java.util.TreeMap;

import uk.org.tombolo.core.ValueSeries;

public class Entity {
	public static enum Type {neighborhood,borough,localAuthority};
	
	Type type;
	String id;
	String name;
	Map<String,ValueSeries> attributeValues = new TreeMap<String,ValueSeries>();
	
	public Entity(Type type, String id, String name){
		this.type = type;
		this.id = id;
		this.name = name;
	}

	public void setAttributeValues(Map<String, ValueSeries> attributeValues) {
		this.attributeValues = attributeValues;
	}
	
}
