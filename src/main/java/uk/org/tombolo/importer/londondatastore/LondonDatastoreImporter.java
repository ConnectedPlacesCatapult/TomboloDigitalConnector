package uk.org.tombolo.importer.londondatastore;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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
import uk.org.tombolo.importer.LabelExtractor;

public class LondonDatastoreImporter implements Importer {
	public static final Provider PROVIDER = new Provider(
			"uk.gov.london",
			"London Datastore - Greater London Authority"
			);

	private static final String DATASOURCE_SPEC_DIR = "datasources/londondatastore/";	
	private static final int TIMEDVALUE_BUFFER_SIZE = 1000;
	
	
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
		if (ldsAttributes.size() > 0){
			// We have explicitly defined columns
			
			for (int aIndex = 0; aIndex<ldsAttributes.size(); aIndex++){
				LDSAttribute ldsa = ldsAttributes.get(aIndex);
				Attribute attribute = datasource.getAttributeByLabel(ldsa.label);
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
				
		}else{
			// We have no explicitly defined columns
			
			//FIXME: This one is specially designed for the PHOF data ... maybe it should be its own importer
						
			LDSAttribute defaultAttribute = new LDSAttribute();
			defaultAttribute.loadFromJsonObject(defaultAttributeJson);
			Sheet sheet = wb.getSheetAt(defaultAttribute.sheetId);
			Iterator<Row> rowIterator = sheet.rowIterator();
			List<TimedValue> timedValueBuffer = new ArrayList<TimedValue>();
			while (rowIterator.hasNext()){
				Row row = rowIterator.next();
				
				if (row.getRowNum() ==0)
					continue;
				
				// Geography
				Cell cell = row.getCell(defaultAttribute.keyColumnId);
				String geographyId = cell.getStringCellValue();
				Geography geography = GeographyUtils.getGeographyByLabel(geographyId);
				if (geography == null)
					continue;
				
				// Attribute
				cell = row.getCell(defaultAttribute.nameColumnId);
				String name = cell.getStringCellValue();
				PHOFLabelExtractor extractor = new PHOFLabelExtractor();
				Attribute attribute = AttributeUtils.getByProviderAndLabel(getProvider(), extractor.extractLabel(name));
				if (attribute == null)
					continue;
				
				// Timestamp
				cell = row.getCell(defaultAttribute.timestampColumnId);
				String timestampString = cell.getStringCellValue();
				LocalDateTime timestamp = TimedValueUtils.parseTimestampString(timestampString);
				if (timestamp == null)
					continue;
				
				// Value
				cell = row.getCell(defaultAttribute.dataColumnId);
				Double value = null;
				if (cell != null)
					value = cell.getNumericCellValue();
				if (value == null)
					continue;
				
				TimedValue timedValue = new TimedValue(geography,attribute,timestamp,value);
				timedValueBuffer.add(timedValue);
				valueCounter++;
				
				if (valueCounter % TIMEDVALUE_BUFFER_SIZE == 0){
					TimedValueUtils.save(timedValueBuffer);
					timedValueBuffer = new ArrayList<TimedValue>();
				}
			}	
			TimedValueUtils.save(timedValueBuffer);
		}
		return valueCounter;
	}
	
	protected Datasource readDatasourceSpec(File spec) throws Exception {
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
		List<Attribute> attributes = null;
		if (json.get("attributes") == null || ((JSONArray)json.get("attributes")).size() == 0){
			// No attributes defined in specification
			LDSAttribute defaultAttribute = new LDSAttribute();
			defaultAttribute.loadFromJsonObject(defaultAttributeJson);
			attributes = attributesFromDatasource(datasource, defaultAttribute);
		}else{
			// Explicit attributes defined in specification
			JSONArray jsonAttributes = (JSONArray)json.get("attributes");
			List<LDSAttribute> ldsAttributes = loadLDSAttributes(defaultAttributeJson, jsonAttributes);
			attributes = attributesFromLDSAttributes(datasource, ldsAttributes);
		}
		for (Attribute attribute : attributes){
			datasource.addAttribute(attribute);
		}
		
		return datasource;
	}
	
	private List<Attribute> attributesFromDatasource(Datasource datasource, LDSAttribute defaultAttribute) throws Exception{
		List<Attribute> attributes = new ArrayList<Attribute>();
		ExcelUtils excelUtils = new ExcelUtils();
		Workbook wb = excelUtils.getWorkbook(datasource);
		Sheet sheet = wb.getSheetAt(defaultAttribute.sheetId);
		Set<String> uniqueLabels = new HashSet<String>();
		LabelExtractor labelExtractor = null;
		if (defaultAttribute.labelExtractor != null)
			labelExtractor = (LabelExtractor)Class.forName(defaultAttribute.labelExtractor).newInstance();
		if (defaultAttribute.rowBased){
			Iterator<Row> rowIterator = sheet.rowIterator();
			while(rowIterator.hasNext()){
				Row row = rowIterator.next();
				if (row.getRowNum() > 0){
					String label = null;
					String name = null;
					String description = null;
					if (defaultAttribute.nameColumnId != -1){
						Cell cell = row.getCell(defaultAttribute.nameColumnId);
						name = cell.getStringCellValue();
					}
					if (defaultAttribute.descriptionColumnId != -1){
						Cell cell = row.getCell(defaultAttribute.descriptionColumnId);
						description = cell.getStringCellValue();
					}
					if (labelExtractor != null){
						label = labelExtractor.extractLabel(name);
					}
					if (label != null){
						if (!uniqueLabels.contains(label)){
							Attribute attribute = new Attribute(getProvider(), label, name, description, Attribute.DataType.numeric);
							attributes.add(attribute);
							uniqueLabels.add(label);	
						}
					}
				}		
			}
		}
		//FIXME: Implement columnBased if needed
		return attributes;
	}
	
	private List<Attribute> attributesFromLDSAttributes(Datasource datasource, List<LDSAttribute> ldsAttributes) throws EncryptedDocumentException, MalformedURLException, InvalidFormatException, IOException{
		Set<String> uniqueAttributes = new HashSet<String>();
		List<Attribute> attributes = new ArrayList<Attribute>();
		ExcelUtils excelUtils = new ExcelUtils();
		Workbook wb = excelUtils.getWorkbook(datasource);
		for (LDSAttribute ldsa : ldsAttributes){
			Sheet sheet = wb.getSheetAt(ldsa.sheetId);
			
			// If label has been encountered the we continue
			if (uniqueAttributes.contains(ldsa.label))
				continue;
			uniqueAttributes.add(ldsa.label);
			
			// Name
			String name = null;
			if (ldsa.name != null){
				name = ldsa.name;
			}else if(ldsa.nameRowId != -1){
				Row row = sheet.getRow(ldsa.nameRowId);
				Cell cell = row.getCell(ldsa.dataColumnId);
				name = cell.getStringCellValue();
			}
			
			// Description
			String description = null;
			if (ldsa.description != null){
				description = ldsa.description;
			}else if (ldsa.descriptionRowId != -1){
				Row row = sheet.getRow(ldsa.descriptionRowId);
				Cell cell = row.getCell(ldsa.dataColumnId);
				description = cell.getStringCellValue();
			}			
			
			// Attribute
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
			attribute.loadFromJsonObject(defaultAttributeJson);
			attribute.loadFromJsonObject(jsonAttribute);
			ldsAttributes.add(attribute);
		}
		return ldsAttributes;
	}
	
}
