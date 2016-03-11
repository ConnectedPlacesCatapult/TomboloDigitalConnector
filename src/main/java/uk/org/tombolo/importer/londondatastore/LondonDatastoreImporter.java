package uk.org.tombolo.importer.londondatastore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.GeographyUtils;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.ExcelUtils;
import uk.org.tombolo.importer.Importer;

public class LondonDatastoreImporter implements Importer {
	public static final Provider PROVIDER = new Provider(
			"uk.gov.london",
			"London Datastore - Greater London Authority"
			);

	private static final String DATASOURCE_SPEC_DIR = "datasources/londondatastore/";	
	
	@Override
	public Provider getProvider() {
		return PROVIDER;
	}
	
	@Override
	public List<Datasource> getAllDatasources() throws Exception {
		List<Datasource> datasources = new ArrayList<Datasource>();
		
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(DATASOURCE_SPEC_DIR).getFile());

		if (file.isDirectory()){
			for (File spec : file.listFiles()){
				Datasource datasource = readDatasourceSpec(spec);
				datasources.add(datasource);
			}
		}
		
		return datasources;
	}

	@Override
	public Datasource getDatasource(String datasourceId) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader
				.getResource(DATASOURCE_SPEC_DIR+"/"+datasourceId+".json")
				.getFile());
		return readDatasourceSpec(file);
	}

	@Override
	public int importDatasource(String datasourceId) throws Exception {
		Datasource datasource = getDatasource(datasourceId);

		// Provider
		ProviderUtils.save(getProvider());
		
		// Attributes
		AttributeUtils.save(datasource.getAttributes());
		
		// Get LDSAttributes
		ClassLoader classLoader = getClass().getClassLoader();
		File spec = new File(classLoader
				.getResource(DATASOURCE_SPEC_DIR+"/"+datasourceId+".json")
				.getFile());
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(new FileReader(spec));
		JSONObject defaultAttributeJson = (JSONObject)json.get("default");
		JSONArray jsonAttributes = (JSONArray)json.get("attributes");
		List<LDSAttribute> ldsAttributes = loadLDSAttributes(defaultAttributeJson, jsonAttributes);

		ExcelUtils excelUtils = new ExcelUtils();
		Workbook wb = excelUtils.getWorkbook(datasource);
		int valueCounter = 0;
		for (int aIndex = 0; aIndex<ldsAttributes.size(); aIndex++){
			LDSAttribute ldsa = ldsAttributes.get(aIndex);
			Attribute attribute = datasource.getAttributes().get(aIndex);
			Sheet sheet = wb.getSheetAt(ldsa.sheetId);
			for (int i = ldsa.startLine; i< ldsa.endLine+1; i++){
				Row row = sheet.getRow(i);
				
				Cell cell;
				
				// Geography
				cell = row.getCell(ldsa.keyColumnId);
				String geographyId = cell.getStringCellValue();
				Geography geography = GeographyUtils.getGeographyByLabel(geographyId);
				
				// Timestamp
				LocalDateTime timestamp = LocalDateTime.parse(ldsa.timestamp);
				
				// Value
				cell = row.getCell(ldsa.dataColumnId);
				Double value = cell.getNumericCellValue();
				
				TimedValue timedValue = new TimedValue(geography,attribute,timestamp,value);
				TimedValueUtils.save(timedValue);
				valueCounter++;
			}
	
		}
		return valueCounter;
	}
	
	protected Datasource readDatasourceSpec(File spec) throws FileNotFoundException, IOException, ParseException, EncryptedDocumentException, InvalidFormatException{
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(new FileReader(spec));
		JSONObject jsonDS = (JSONObject) json.get("datasource");
		
		// Basic details
		Datasource datasource = new Datasource(
				getProvider(),
				(String)jsonDS.get("name"),
				(String)jsonDS.get("description"));
		datasource.setUrl((String)jsonDS.get("url"));
		
		// Datasource file
		datasource.setRemoteDatafile((String)jsonDS.get("remoteDatafile"));
		datasource.setLocalDatafile((String)jsonDS.get("localDatafilePath"));
		
		// Attributes
		JSONObject defaultAttributeJson = (JSONObject)json.get("default");
		JSONArray jsonAttributes = (JSONArray)json.get("attributes");
		List<LDSAttribute> ldsAttributes = loadLDSAttributes(defaultAttributeJson, jsonAttributes);
		List<Attribute> attributes = attributesFromLDSAttributes(datasource, ldsAttributes);
		for (Attribute attribute : attributes){
			datasource.addAttribute(attribute);
		}
		
		return datasource;
	}
	
	private List<Attribute> attributesFromLDSAttributes(Datasource datasource, List<LDSAttribute> ldsAttributes) throws EncryptedDocumentException, MalformedURLException, InvalidFormatException, IOException{
		List<Attribute> attributes = new ArrayList<Attribute>();
		ExcelUtils excelUtils = new ExcelUtils();
		Workbook wb = excelUtils.getWorkbook(datasource);
		for (LDSAttribute ldsa : ldsAttributes){
			Sheet sheet = wb.getSheetAt(ldsa.sheetId);
			Row row = sheet.getRow(ldsa.nameRowId);
			Cell cell = row.getCell(ldsa.dataColumnId);
			String name = cell.getStringCellValue();
			row = sheet.getRow(ldsa.descriptionRowId);
			cell = row.getCell(ldsa.dataColumnId);
			String description = cell.getStringCellValue();
			Attribute attribute = new Attribute(getProvider(),ldsa.label,name,description,Attribute.DataType.numeric);
			attributes.add(attribute);
		}
		return attributes;
	}
	
	private List<LDSAttribute> loadLDSAttributes(JSONObject defaultAttributeJson, JSONArray jsonAttributes){
		List<LDSAttribute> ldsAttributes = new ArrayList<LDSAttribute>();
		for(Object object : jsonAttributes){
			JSONObject jsonAttribute = (JSONObject)object;
			LDSAttribute attribute = new LDSAttribute();
			loadFromJsonObject(attribute, defaultAttributeJson);
			loadFromJsonObject(attribute, jsonAttribute);
			ldsAttributes.add(attribute);
		}
		return ldsAttributes;
	}
	
	private void loadFromJsonObject(LDSAttribute attribute, JSONObject json){
		if (json.get("label") != null)
			attribute.label = (String)json.get("label");
		if (json.get("timestamp") != null)
			attribute.timestamp = (String)json.get("timestamp");
		if (json.get("dataColumnId") != null)
			attribute.dataColumnId = ((Long)json.get("dataColumnId")).intValue();
		if (json.get("sheetId") != null)
			attribute.sheetId = ((Long)json.get("sheetId")).intValue();
		if (json.get("keyColumnId") != null)
			attribute.keyColumnId = ((Long)json.get("keyColumnId")).intValue();
		if (json.get("labelRowId") != null)
			attribute.labelRowId = ((Long)json.get("labelRowId")).intValue();
		if (json.get("nameRowId") != null)
			attribute.nameRowId = ((Long)json.get("nameRowId")).intValue();
		if (json.get("descriptionRowId") != null)
			attribute.descriptionRowId = ((Long)json.get("descriptionRowId")).intValue();
		if (json.get("startLine") != null)
			attribute.startLine = ((Long)json.get("startLine")).intValue();
		if (json.get("endLine") != null)
			attribute.endLine = ((Long)json.get("endLine")).intValue();
	}	
}
