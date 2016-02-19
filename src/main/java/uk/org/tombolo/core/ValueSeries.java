package uk.org.tombolo.core;

import java.util.TreeMap;

public class ValueSeries {
	
	TreeMap<String, Double> labelToValue = new TreeMap<String,Double>();
	
	public void addValue(String label, Double value){
		labelToValue.put(label, value);
	}
	
	public Double getValue(String label){
		return labelToValue.get(label);
	}
}
