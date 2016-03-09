package uk.org.tombolo.reader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.ValueSeries;

public class FeatureMatrix {

	String relationName;
	Attribute key;
	List<Attribute> attributes =  new ArrayList<Attribute>();
	Map<String,Map<String,ValueSeries>> entityIdToAttributeNameToValueSeries = new HashMap<String,Map<String,ValueSeries>>();

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public Map<String, ValueSeries> getAttributeNameToValueSeriesByEntityId(String entityId){
		return entityIdToAttributeNameToValueSeries.get(entityId);
	}
	
	public void writeArff(Writer writer) throws IOException {
		BufferedWriter bwriter = new BufferedWriter(writer);
		
		// Writing out data
		bwriter.write("@RELATION "+relationName);
		bwriter.newLine();
		bwriter.newLine();
		
//		bwriter.write("@ATTRIBUTE "+key.getLabel() + " " + key.getDataType().name().toUpperCase());		
		for (Attribute attribute : attributes){
//			for (String seriesLabel : attribute.getLabels()){
//				String attributeName = attribute.getLabel()+"_"+seriesLabel;
//				bwriter.write("@ATTRIBUTE "+attributeName + " " + attribute.getDataType().name().toUpperCase());
//				bwriter.newLine();
//			}
		}
		bwriter.newLine();
		
		bwriter.write("@DATA");
		bwriter.newLine();
		writeCSVs(bwriter);
		bwriter.flush();
		bwriter.close();
	}
	
	public void writeCSV(Writer writer) throws IOException {
		BufferedWriter bwriter = new BufferedWriter(writer);
		
		// Writing headers
		bwriter.write(key.getLabel());
		for (Attribute attribute : attributes){
//			for (String seriesLabel : attribute.getLabels()){
//				String attributeName = attribute.getLabel()+"_"+seriesLabel;
//				bwriter.write("," + attributeName);
//			}
		}
		bwriter.newLine();
		
		// Writing data
		writeCSVs(bwriter);
		
		bwriter.flush();
		bwriter.close();
	}
	
	private void writeCSVs(BufferedWriter writer) throws IOException {
		for (String entityId : entityIdToAttributeNameToValueSeries.keySet()){
			writer.write(entityId);
			for (Attribute attribute : attributes){
//				for (String label : attribute.getLabels()){
//					Double value = entityIdToAttributeNameToValueSeries.get(entityId).get(attribute.getLabel()).getValue(label);
//					writer.write(","+value);
//				}
			}
			writer.newLine();
		}		
	}
	
	public void writeJson(Writer writer) throws IOException {
		Gson gson = new Gson();
		gson.toJson(this, writer);
	}
}
