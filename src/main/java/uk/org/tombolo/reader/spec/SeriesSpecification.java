package uk.org.tombolo.reader.spec;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SeriesSpecification {

	String datasourceId;
	
	// Series information
	String name;				// Name of the series
	String label;				// Label for the series
	String description;			// Description of the series
				
	// Excel sheet information
	int sheetId;
	int keyColumnId;
	
	// Series instance
	List<SeriesInstance> seriesInstances;

	public static List<SeriesSpecification> fromJsonFile(File file) throws IOException{
		Gson gson = new Gson();
		Reader reader = new FileReader(file);
		Type listType = new TypeToken<ArrayList<SeriesSpecification>>(){}.getType();
		return gson.fromJson(reader, listType);
	}

	public String getDatasourceId() {
		return datasourceId;
	}

	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}

	public int getSheetId() {
		return sheetId;
	}

	public int getKeyColumnId() {
		return keyColumnId;
	}

	public List<SeriesInstance> getSeriesInstances() {
		return seriesInstances;
	}

}

