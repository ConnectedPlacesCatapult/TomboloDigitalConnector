package uk.org.tombolo.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.GeographyUtils;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.londondatastore.PHOFLabelExtractor;

public abstract class ExcelImporter extends AbstractImporter implements Importer {
	protected String datasourceSpecDir;
	protected int timedValueBufferSize;

	private static Logger log = LoggerFactory.getLogger(ExcelImporter.class);
	private Map<String, Geography> geographyCache = new HashMap<String, Geography>();
	
	@Override
	public List<Datasource> getAllDatasources() throws Exception {
		List<Datasource> datasources = new ArrayList<Datasource>();
		
		File file = new File(getClass().getResource(datasourceSpecDir).getFile());

		if (file.isDirectory()){
			for (File spec : file.listFiles()){
				String id = (spec.getName().split("\\."))[0];
				Datasource datasource = readDatasourceSpec(id, new FileInputStream(spec));
				datasources.add(datasource);
			}
		}
		
		return datasources;
	}
	
	@Override
	public Datasource getDatasource(String datasourceId) throws Exception {
		String datasourceSpecPath = datasourceSpecDir+"/"+datasourceId+".json";
		InputStream is = getClass().getResourceAsStream(datasourceSpecPath);
		if (is == null)
			log.debug("Failed to load resource: {}", datasourceSpecPath);
		else
			log.debug("Successfully loaded resource: {}", datasourceSpecPath);
		return readDatasourceSpec(datasourceId, is);
	}
	
	@Override
	public int importDatasource(Datasource datasource) throws Exception {

		// Provider
		ProviderUtils.save(getProvider());
		
		// Attributes
		AttributeUtils.save(datasource.getAttributes());
		
		// Get LDSAttributes
		String datasourceSpecPath = datasourceSpecDir+"/"+datasource.getId()+".json";
		InputStream is = getClass().getResourceAsStream(datasourceSpecPath);
		
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(new InputStreamReader(is));
		JSONObject defaultAttributeJson = (JSONObject)json.get("default");
		JSONArray jsonAttributes = (JSONArray)json.get("attributes");
		List<ExcelAttribute> ldsAttributes = loadLDSAttributes(defaultAttributeJson, jsonAttributes);

		ExcelUtils excelUtils = new ExcelUtils();
		Workbook wb = excelUtils.getWorkbook(datasource);
		int valueCounter = 0;
		if (ldsAttributes.size() > 0){
			// We have explicitly defined columns
			
			for (int aIndex = 0; aIndex<ldsAttributes.size(); aIndex++){
				ExcelAttribute ldsa = ldsAttributes.get(aIndex);
				Sheet sheet = wb.getSheetAt(ldsa.sheetId);

				// Attribute
				Attribute attribute = datasource.getAttributeByLabel(ldsa.label);

				// Timestamp
				LocalDateTime timestamp = null;
				if (ldsa.timestampRowId != -1){
					Row tRow = sheet.getRow(ldsa.timestampRowId);
					Cell tCell = tRow.getCell(ldsa.dataColumnId);
					// FIXME: This could be string :/
					timestamp = TimedValueUtils.parseTimestampString((String.valueOf(new Double(tCell.getNumericCellValue()).intValue())));
				}else if(ldsa.timestamp != null){
					timestamp = LocalDateTime.parse(ldsa.timestamp);
				}
				if (timestamp == null)
					continue;
				
				// Timed value per geography
				List<TimedValue> timedValueBuffer = new ArrayList<TimedValue>();
				for (int i = ldsa.startLine; i< ldsa.endLine+1; i++){
					Row row = sheet.getRow(i);

					Cell cell;

					// Geography
					cell = row.getCell(ldsa.keyColumnId);
					String geographyId = cell.getStringCellValue();
					Geography geography = getGeographyByLabel(geographyId);
					if (geography == null)
						continue;
					
					// Value
					cell = row.getCell(ldsa.dataColumnId);
					Double value = cell.getNumericCellValue();
					//if (value == null)
					//	continue;

					TimedValue timedValue = new TimedValue(geography,attribute,timestamp,value);
					timedValueBuffer.add(timedValue);
					valueCounter++;
					
					if (valueCounter % timedValueBufferSize == 0){
						TimedValueUtils.save(timedValueBuffer);
						timedValueBuffer = new ArrayList<TimedValue>();
					}
				}	
				TimedValueUtils.save(timedValueBuffer);
			}				
		}else{
			// We have no explicitly defined columns
			
			//FIXME: This one is specially designed for the PHOF data ... maybe it should be its own importer
						
			ExcelAttribute defaultAttribute = new ExcelAttribute();
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
				Geography geography = getGeographyByLabel(geographyId);
				
				if (geography == null)
					continue;
				
				// Attribute
				cell = row.getCell(defaultAttribute.nameColumnId);
				String name = cell.getStringCellValue();
				// FIXME: Generalize!!!
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
				
				if (valueCounter % timedValueBufferSize == 0){
					TimedValueUtils.save(timedValueBuffer);
					timedValueBuffer = new ArrayList<TimedValue>();
				}
			}	
			TimedValueUtils.save(timedValueBuffer);
		}
		return valueCounter;
	}

	protected Datasource readDatasourceSpec(String id, InputStream spec) throws Exception {
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(new InputStreamReader(spec));
		JSONObject jsonDS = (JSONObject) json.get("datasource");
		
		// Basic details
		Datasource datasource = new Datasource(
				id,
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
			ExcelAttribute defaultAttribute = new ExcelAttribute();
			defaultAttribute.loadFromJsonObject(defaultAttributeJson);
			attributes = attributesFromDatasource(datasource, defaultAttribute);
		}else{
			// Explicit attributes defined in specification
			JSONArray jsonAttributes = (JSONArray)json.get("attributes");
			List<ExcelAttribute> ldsAttributes = loadLDSAttributes(defaultAttributeJson, jsonAttributes);
			attributes = attributesFromLDSAttributes(datasource, ldsAttributes);
		}
		for (Attribute attribute : attributes){
			datasource.addAttribute(attribute);
		}
		
		return datasource;
	}
	
	private List<Attribute> attributesFromDatasource(Datasource datasource, ExcelAttribute defaultAttribute) throws Exception{
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

	private List<Attribute> attributesFromLDSAttributes(Datasource datasource, List<ExcelAttribute> ldsAttributes) throws EncryptedDocumentException, MalformedURLException, InvalidFormatException, IOException{
		Set<String> uniqueAttributes = new HashSet<String>();
		List<Attribute> attributes = new ArrayList<Attribute>();
		ExcelUtils excelUtils = new ExcelUtils();
		Workbook wb = excelUtils.getWorkbook(datasource);
		for (ExcelAttribute ldsa : ldsAttributes){
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

	private List<ExcelAttribute> loadLDSAttributes(JSONObject defaultAttributeJson, JSONArray jsonAttributes){
		List<ExcelAttribute> ldsAttributes = new ArrayList<ExcelAttribute>();
		for(Object object : jsonAttributes){
			JSONObject jsonAttribute = (JSONObject)object;
			ExcelAttribute attribute = new ExcelAttribute();
			attribute.loadFromJsonObject(defaultAttributeJson);
			attribute.loadFromJsonObject(jsonAttribute);
			ldsAttributes.add(attribute);
		}
		return ldsAttributes;
	}

	private Geography getGeographyByLabel(String label) {
		if (!geographyCache.containsKey(label)) {
			geographyCache.put(label, GeographyUtils.getGeographyByLabel(label));
		}

		return geographyCache.get(label);
	}

}
