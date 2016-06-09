package uk.org.tombolo.execution.spec;

import com.google.gson.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;

public class DataExportSpecification {

	DatasetSpecification datasetSpecification;
	String exporterClass;

	public DatasetSpecification getDatasetSpecification() {
		return datasetSpecification;
	}

	public void setDatasetSpecification(DatasetSpecification datasetSpecification) {
		this.datasetSpecification = datasetSpecification;
	}

	public String getExporterClass() {
		return exporterClass;
	}

	public void setExporterClass(String exporterClass) {
		this.exporterClass = exporterClass;
	}

	public static DataExportSpecification fromJsonFile(File jsonFile) throws JsonSyntaxException, JsonIOException, FileNotFoundException{
		Gson gson = new Gson();
		
		return gson.fromJson(new FileReader(jsonFile), DataExportSpecification.class);
	}

	public static DataExportSpecification fromJson(String jsonString) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(FieldSpecification.class, new FieldSpecificationDeserializer());
		Gson gson = gsonBuilder.create();
		return gson.fromJson(jsonString, DataExportSpecification.class);
	}

	private static class FieldSpecificationDeserializer implements JsonDeserializer<FieldSpecification> {
		@Override
		public FieldSpecification deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = (JsonObject) json;
			FieldSpecification fieldSpec = new FieldSpecification(jsonObject.get("fieldClass").getAsString(), jsonObject.get("label").getAsString());
			jsonObject.remove("fieldClass");
			jsonObject.remove("label");
			// We convert the gson object to the org.simple.json type as we use that everywhere else
			fieldSpec.setData((JSONObject) JSONValue.parse(jsonObject.toString()));
			return fieldSpec;
		}
	}
}
