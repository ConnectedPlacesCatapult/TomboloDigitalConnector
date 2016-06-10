package uk.org.tombolo.execution.spec;

import com.google.gson.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

	public static DataExportSpecification fromJsonFile(File jsonFile) throws IOException {
		return fromJson(FileUtils.readFileToString(jsonFile));
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
			String fieldClass = (String) jsonObject.remove("fieldClass").getAsString();
			return new FieldSpecification(fieldClass, jsonObject.toString());
		}
	}
}
