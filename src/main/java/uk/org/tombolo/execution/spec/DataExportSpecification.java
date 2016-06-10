package uk.org.tombolo.execution.spec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

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
		gsonBuilder.registerTypeAdapter(FieldSpecification.class, new FieldSpecification.FieldSpecificationDeserializer());
		Gson gson = gsonBuilder.create();
		return gson.fromJson(jsonString, DataExportSpecification.class);
	}
}
