package uk.org.tombolo.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.ValueSeries;
import uk.org.tombolo.datacatalogue.DatasourceSpecification;
import uk.org.tombolo.reader.spec.DatapackSpecification;
import uk.org.tombolo.reader.spec.SeriesInstance;
import uk.org.tombolo.reader.spec.SeriesSpecification;
import uk.org.tombolo.writer.Entity;

public class DataReader {
	String localDataPath;
	
	List<Entity> entities;
	FeatureMatrix featureMatrix;
	
	public DataReader(String localDataPath){
		this.localDataPath = localDataPath;
	}
		
	public List<Entity> getEntities() {
		return entities;
	}

	public FeatureMatrix getFeatureMatrix() {
		return featureMatrix;
	}

	public void readData(File specification) throws IOException, EncryptedDocumentException, InvalidFormatException {
		
		//List<SeriesSpecification> seriesSpecs = SeriesSpecification.fromJsonFile(specification);
		DatapackSpecification datapackSpecification = DatapackSpecification.fromJsonFile(specification);
		List<SeriesSpecification> seriesSpecs = datapackSpecification.getSeriesSpecifications();
		
		// FIXME: Read entities
		entities = new ArrayList<Entity>();
		SeriesSpecification idNameSpec = datapackSpecification.getEntitySpecification().getIdNameSpecification();
		SeriesInstance idNameInstance = idNameSpec.getSeriesInstances().get(0);
		
		DatasourceSpecification idNameDatasource = datapackSpecification.getDatasourceSpecificationById(idNameSpec.getDatasourceId());
		Workbook idNameWb = getWorkbook(idNameDatasource);
					
		// Connect to sheet
		Sheet idNameSheet = idNameWb.getSheetAt(idNameSpec.getSheetId());		
		for (int i = idNameInstance.getStartLine(); i< idNameInstance.getEndLine()+1; i++){
			Row row = idNameSheet.getRow(i);
			Cell cell;
			
			// Id column
			cell = row.getCell(idNameSpec.getKeyColumnId());
			String entityId = cell.getStringCellValue();

			// Name column
			cell = row.getCell(idNameInstance.getDataColumnId());
			String entityName = null;
			if (cell != null)
				entityName = cell.getStringCellValue();
			
			entities.add(new Entity(datapackSpecification.getEntitySpecification().getType(),entityId,entityName));
		}
		
		
		// Read feature values
		featureMatrix = new FeatureMatrix();
		// FIXME: Specify this somewhere in a file!
		featureMatrix.key = new Attribute(null,"boroughid","Borough identifier","Borough identifier",Attribute.DataType.string);
		
		for (SeriesSpecification  seriesSpec : seriesSpecs){
			// Processing the i-th feature
			
			// Connect to Workbook
			// FIXME: We should pool together datasets coming from the same workbook
			
			DatasourceSpecification datasource = datapackSpecification.getDatasourceSpecificationById(seriesSpec.getDatasourceId());
			Workbook wb = getWorkbook(datasource);
						
			// Connect to sheet
			Sheet sheet = wb.getSheetAt(seriesSpec.getSheetId());
			int pnor = sheet.getPhysicalNumberOfRows();
			String sname = sheet.getSheetName();
			
			// Update feature vector
			// FIXME: The numeric should not be hard-coded
			String attributeLabel = seriesSpec.getLabel();
			Attribute attribute = new Attribute(null,attributeLabel,attributeLabel,seriesSpec.getDescription(),Attribute.DataType.numeric);
			for (SeriesInstance seriesInstance : seriesSpec.getSeriesInstances()){
//				attribute.addLabel(seriesInstance.getLabel());
			}
			featureMatrix.attributes.add(attribute);
						
			// Loop through SeriesInstances
			for (SeriesInstance seriesInstance: seriesSpec.getSeriesInstances()){
				for (int i = seriesInstance.getStartLine(); i< seriesInstance.getEndLine()+1; i++){
					Row row = sheet.getRow(i);
					Cell cell;

					// Key column
					cell = row.getCell(seriesSpec.getKeyColumnId());
					String entityId = cell.getStringCellValue();
					if (!featureMatrix.entityIdToAttributeNameToValueSeries.containsKey(entityId))
						featureMatrix.entityIdToAttributeNameToValueSeries.put(entityId, new HashMap<String,ValueSeries>());
					if (!featureMatrix.entityIdToAttributeNameToValueSeries.get(entityId).containsKey(attribute.getLabel()))
						featureMatrix.entityIdToAttributeNameToValueSeries.get(entityId).put(attribute.getLabel(), new ValueSeries());

					// Data column
					cell = row.getCell(seriesInstance.getDataColumnId());
					Double value = Double.NaN;
					if (cell != null)
						value = cell.getNumericCellValue();
					
					featureMatrix
						.entityIdToAttributeNameToValueSeries
							.get(entityId).get(attribute.getLabel()).addValue(seriesInstance.getLabel(), value);
				}
				
			}
			wb.close();
		}
	}
	
	private Workbook getWorkbook(DatasourceSpecification datasource) throws MalformedURLException, IOException, EncryptedDocumentException, InvalidFormatException{
		File localDatafile = new File(localDataPath + "/" + datasource.getLocalDatafile());
		if (!localDatafile.exists()){
			// Local datafile does not exist so we should download it
			FileUtils.copyURLToFile(new URL(datasource.getRemoteDatafile()), localDatafile);
		}
		InputStream inp = new FileInputStream(localDatafile);
		Workbook wb = null;
		wb = WorkbookFactory.create(localDatafile);
		/*
		switch(datasource.getDatafileType()){
			case xls:					
				wb = new HSSFWorkbook(new POIFSFileSystem(inp));
				break;
			case xlsx:
				wb = new XSSFWorkbook(inp);
				//wb = new SXSSFWorkbook(new XSSFWorkbook(inp));
				break;
		}
		*/
		return wb;
	}
}
