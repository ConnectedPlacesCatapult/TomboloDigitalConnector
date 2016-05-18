package uk.org.tombolo.execution.spec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

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
		Gson gson = new Gson();
		return gson.fromJson(jsonString, DataExportSpecification.class);
	}

}
