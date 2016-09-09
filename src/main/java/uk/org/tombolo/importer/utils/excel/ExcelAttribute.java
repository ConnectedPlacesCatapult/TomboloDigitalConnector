package uk.org.tombolo.importer.utils.excel;

import org.json.simple.JSONObject;

public class ExcelAttribute {

	// Label
	String label;
	int labelRowId = -1;
	String labelExtractor;

	// Sheet
	int sheetId = -1;
	boolean rowBased = false;
	boolean columnBased = false;

	// Time
	String timestamp;
	int timestampRowId = -1;
	int timestampColumnId = -1;
	
	// Name index
	String name;
	int nameRowId = -1;
	int nameColumnId = -1;
	
	// Description index
	String description;
	int descriptionRowId = -1;
	int descriptionColumnId = -1;
	
	// Key and data column and lines
	int keyColumnId = -1;
	int dataColumnId = -1;
	int startLine = -1;
	int endLine = -1;

	public void loadFromJsonObject(JSONObject json){
		// Label
		if (json.get("label") != null)
			label = (String)json.get("label");
		if (json.get("labelRowId") != null)
			labelRowId = ((Long)json.get("labelRowId")).intValue();
		if (json.get("labelExtractor") != null)
			labelExtractor = (String)json.get("labelExtractor");

		// Sheet
		if (json.get("sheetId") != null)
			sheetId = ((Long)json.get("sheetId")).intValue();
		if (json.get("rowBased") != null)
			rowBased = ((Boolean)json.get("rowBased")).booleanValue();
		if (json.get("columnBased") != null)
			columnBased = ((Boolean)json.get("columnBased")).booleanValue();
		
		// Time
		if (json.get("timestamp") != null)
			timestamp = (String)json.get("timestamp");
		if (json.get("timestampRowId") != null)
			timestampRowId = ((Long)json.get("timestampRowId")).intValue();
		if (json.get("timestampColumnId") != null)
			timestampColumnId = ((Long)json.get("timestampColumnId")).intValue();	

		// Name
		if (json.get("name") != null)
			name = (String)json.get("name");
		if (json.get("nameRowId") != null)
			nameRowId = ((Long)json.get("nameRowId")).intValue();
		if (json.get("nameColumnId") != null)
			nameColumnId = ((Long)json.get("nameColumnId")).intValue();

		// Description
		if (json.get("description") != null)
			description = (String)json.get("description");
		if (json.get("descriptionRowId") != null)
			descriptionRowId = ((Long)json.get("descriptionRowId")).intValue();
		if (json.get("descriptionColumnId") != null)
			descriptionColumnId = ((Long)json.get("descriptionColumnId")).intValue();

		// Key data column and lines
		if (json.get("keyColumnId") != null)
			keyColumnId = ((Long)json.get("keyColumnId")).intValue();
		if (json.get("dataColumnId") != null)
			dataColumnId = ((Long)json.get("dataColumnId")).intValue();
		if (json.get("startLine") != null)
			startLine = ((Long)json.get("startLine")).intValue();
		if (json.get("endLine") != null)
			endLine = ((Long)json.get("endLine")).intValue();
	}
}
