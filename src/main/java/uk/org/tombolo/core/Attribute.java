package uk.org.tombolo.core;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class Attribute {
	public static enum DataType {string,numeric};
	
	String provider;
	String name;
	String description;
	DataType dataType;
	TreeSet<String> labels = new TreeSet<String>();
	
	public Attribute(String provider, String name, String description, DataType dataType){
		this.provider = provider;
		this.name = name;
		this.description = description;
		this.dataType = dataType;
	}
	
	public String getName(){
		return name;
	}
	
	public String getDescription(){
		return description;
	}
	
	public DataType getDataType(){
		return dataType;
	}
	
	public void addLabel(String label){
		labels.add(label);
	}
	
	public List<String> getLabels(){
		return new ArrayList<String>(labels);
	}
}
