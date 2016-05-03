package uk.org.tombolo.importer.ons;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Datasource;
import uk.org.tombolo.core.Geography;
import uk.org.tombolo.core.Provider;
import uk.org.tombolo.core.TimedValue;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.GeographyUtils;
import uk.org.tombolo.core.utils.ProviderUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.DownloadUtils;
import uk.org.tombolo.importer.Importer;

/**
 * A class for importing ONS Census data into the Tombolo Digital Connector platform.
 * 
 * All Census datasets
 * http://data.ons.gov.uk/ons/api/data/collections.xml?apikey=kil61db9uf&context=Census&firstRecord=1&noOfRecords=5
 * 
 * Get all Hierarchies for a dataset
 * http://data.ons.gov.uk/ons/api/data/hierarchies/OT102EW.json?context=Census&apikey=kil61db9uf
 *
 * Get description of a dataset
 * http://data.ons.gov.uk/ons/api/data/datasetdetails/OT102EW.json?context=Census&apikey=kil61db9uf&geog=2011STATH
 * 
 * Census population for LSOAs
 * http://data.ons.gov.uk/ons/api/data/dataset/OT102EW/set.json?context=Census&apikey=kil61db9uf&geog=2011STATH&startobs=1&noobs=10
 * 
 */
public class ONSCensusImporter extends AbstractONSImporter implements Importer{
	private static final String ONS_API_URL = "http://data.ons.gov.uk/ons/api/data/";
	private static final String ONS_API_KEY = "kil61db9uf";	//FIXME: This should not be in the source code!!!
	
	private static final String ONS_LANGUAGE_ATTRIBUTE_KEY = "@xml.lang";
	private static final String ONS_LANGUAGE_ATTRIBUTE_VALUE_EN = "en";
	private static final String ONS_ATTRIBUTE_VALUE_KEY = "$";
	
	private static final String ONS_DATASET_BASE_URL = "http://data.statistics.gov.uk/ons/datasets/";
	
	private static final LocalDateTime CENSUS_2011_DATE_TIME = LocalDateTime.of(2011,12,31,23,59,59);

	protected int timedValueBufferSize = 100000;
	
	private Logger log = LoggerFactory.getLogger(ONSCensusImporter.class);
	
	@Override
	public List<Datasource> getAllDatasources() throws IOException, ParseException{
		List<Datasource> datasources = new ArrayList<Datasource>();
	
		String baseUrl = ONS_API_URL + "collections.json?";
		Map<String,String> params = new HashMap<String,String>();
		params.put("apikey", ONS_API_KEY);
		params.put("context", "Census");		
		String paramsString = DownloadUtils.paramsToString(params);
		URL url = new URL(baseUrl + paramsString);
		
		// Parse the json content
		JSONParser parser = new JSONParser();
		URLConnection connection = url.openConnection();
		connection.setRequestProperty("Accept", "application/json");
		
		JSONObject rootObject = (JSONObject) parser.parse(new InputStreamReader(connection.getInputStream()));		
		JSONObject ons = (JSONObject)rootObject.get("ons");

		JSONArray collections = (JSONArray)((JSONObject)ons.get("collectionList")).get("collection");
		for (int i=0; i<collections.size(); i++){
			JSONObject collection = (JSONObject)collections.get(i);
			String datasetId = (String)collection.get("id");
			JSONArray names = (JSONArray)((JSONObject)collection.get("names")).get("name");
			String datasetDescription = getEnglishValue(names);
			
			Datasource datasourceDetails = new Datasource(datasetId,getProvider(),datasetId,datasetDescription);
			datasources.add(datasourceDetails);
		}
		
		return datasources;
	}
		
	public int importDatasource(Datasource datasource) throws IOException, ParseException{
		// Get the provider details
		Provider provider = getProvider();
		
		// Store the provider
		ProviderUtils.save(provider);
				
		// Store data in database
		File localFile = downloadUtils.getDatasourceFile(datasource);
		ZipFile zipFile = new ZipFile(localFile);
		ZipArchiveEntry zipEntry = null;
		Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
		int valueCount = 0;
		while(entries.hasMoreElements()){
			zipEntry = entries.nextElement();
			
			if (zipEntry.getName().startsWith("CSV_") && zipEntry.getName().endsWith("_EN.csv")){
			
				BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)));
				
				String line = null;
				List<TimedValue> timedValueBuffer = new ArrayList<TimedValue>();
				int lineCounter = 0;
				List<String> attributeBaseNames = new ArrayList<String>();
				while ((line = br.readLine()) != null){
					lineCounter++;
					// We are processing the lineCounter-th line
					
					String[] fields = line.split(",");
					
					// FIXME: Store the attributes when we have updated the attribute names
					if (lineCounter == 8){
						for (String field : fields){
							attributeBaseNames.add(dequote(field));
						}
					}else if (lineCounter == 9){
						for (int i = 2; i<datasource.getAttributes().size()+2; i++){
							String name = attributeBaseNames.get(i);
							if (!attributeBaseNames.get(i).equals(dequote(fields[i])))
								name += " - " + dequote(fields[i]);
							datasource.getAttributes().get(i-2).setName(name);
							datasource.getAttributes().get(i-2).setDescription(name);
						}
						
						// Store attributes in database
						AttributeUtils.save(datasource.getAttributes());
						log.info("Saved {} attributes", datasource.getAttributes().size());
					}
					
					if (fields.length == 2 + datasource.getAttributes().size()
							&& fields[0].startsWith("\"")){
						// We have an actual data line
						try{
							String areaId = dequote(fields[0]);
							List<Double> values = new ArrayList<Double>();
							for (int i=2; i<2+datasource.getAttributes().size(); i++){
								values.add(Double.parseDouble(dequote(fields[i])));
							}
							Geography geography = GeographyUtils.getGeographyByLabel(areaId);
							if (geography != null
									&& values.size() == datasource.getAttributes().size()){
								for (int i=0; i<values.size(); i++){
									TimedValue tv 
										= new TimedValue(geography, datasource.getAttributes().get(i), CENSUS_2011_DATE_TIME, values.get(i));
									timedValueBuffer.add(tv);
									valueCount++;
									
									// Flushing buffer
									if (valueCount % timedValueBufferSize == 0){
										// Buffer is full ... we write values to db
										log.info("Preparing to write a batch of {} values ...", timedValueBuffer.size());
										TimedValueUtils.save(timedValueBuffer);
										timedValueBuffer = new ArrayList<TimedValue>();
										log.info("Total values written: {}", valueCount);
									}
								}								
							}							
						}catch (NumberFormatException e){
							// Ignoring this line since it does not contain numeric values for the attributes
						}
					}
				}
				log.info("Preparing to write a batch of {} values", timedValueBuffer.size());
				TimedValueUtils.save(timedValueBuffer);
				log.info("Total values written: {}", valueCount);
				br.close();
			}
			
		}
		zipFile.close();		
		
		return valueCount;
	}
	
	public Datasource getDatasource(String datasourceId) throws IOException, ParseException{
		// Set-up the basic url and the parameters
		String baseUrl = ONS_API_URL + "datasetdetails/" + datasourceId + ".json?";
		Map<String,String> params = new HashMap<String,String>();
		params.put("context", "Census");
		params.put("apikey", ONS_API_KEY);
		params.put("geog", "2011STATH");
		String paramsString = DownloadUtils.paramsToString(params);
		URL url = new URL(baseUrl + paramsString);
		
		// Parse the json content
		JSONParser parser = new JSONParser();
		URLConnection connection = url.openConnection();
		connection.setRequestProperty("Accept", "application/json");
		
		JSONObject rootObject = (JSONObject) parser.parse(new InputStreamReader(connection.getInputStream()));
		
		JSONObject ons = (JSONObject)rootObject.get("ons");
		JSONObject datasetDetail = (JSONObject)ons.get("datasetDetail");
		
		// Get dataset English name
		JSONArray names = (JSONArray)((JSONObject)datasetDetail.get("names")).get("name");
		String datasourceDescription = getEnglishValue(names);
		
		Datasource datasource = new Datasource(datasourceId,getProvider(),datasourceId,datasourceDescription);
		
		// Get dataset dimensions
		JSONArray dimensions = (JSONArray)((JSONObject)datasetDetail.get("dimensions")).get("dimension");
		for (int dimIndex = 0; dimIndex<dimensions.size(); dimIndex++){
			JSONObject dimension = (JSONObject)dimensions.get(dimIndex);
			String dimensionType = (String)dimension.get("dimensionType");
			if (dimensionType.equals("Topic")){
				// The dimension is of type "Topic", i.e. an attribute in our terminology
				long numberOfDimensionItems = (long)dimension.get("numberOfDimensionItems");

				String attributeLabel = (String)dimension.get("dimensionId");
				JSONArray dimensionTitles = (JSONArray)((JSONObject)dimension.get("dimensionTitles")).get("dimensionTitle");
				String attributeDescription = getEnglishValue(dimensionTitles);
				// FIXME: The Attribute.DataType value should be gotten from somewhere rather than defaulting to numeric
				Attribute.DataType dataType = Attribute.DataType.numeric;

				if (numberOfDimensionItems == 1){
					// The attribute is one-dimensional
					Attribute attribute = new Attribute(getProvider(), attributeLabel, attributeDescription, attributeDescription, dataType);
					datasource.addAttribute(attribute);
				}else{
					// The attribute is multi-dimensional
					// We add an attribute for each dimension
					// The name and the description will be added later when we get this information from the datafile itself
					for (int i=0; i<numberOfDimensionItems; i++){
						String multiAttributeLabel = attributeLabel+"_"+(i+1);
						Attribute attribute = new Attribute(getProvider(), multiAttributeLabel, "T.b.a.", "T.b.a.", dataType);
						datasource.addAttribute(attribute);
					}
				}
			}
		}
		
		// Get datafile
		JSONArray documents = (JSONArray)((JSONObject)datasetDetail.get("documents")).get("document");
		String remoteDatafile = null;
		for (int docIndex = 0; docIndex<documents.size(); docIndex++){
			JSONObject document = (JSONObject)documents.get(docIndex);
			if (document.get("@type").equals("CSV")){
				JSONObject href = (JSONObject)document.get("href");
				if (href.get("@xml.lang").equals("en")){
					remoteDatafile = (String)href.get("$");
					break;
				}
			}
		}
		String localDatafile = remoteDatafile.substring(ONS_DATASET_BASE_URL.length());
		
		datasource.setUrl("http://www.ons.gov.uk/ons/datasets-and-tables/index.html"); // Dataset location (description)
		datasource.setRemoteDatafile(remoteDatafile);	// Remote file
		datasource.setLocalDatafile(localDatafile);		// Local file (relative to local data root)
		
		return datasource;
	}
	
	private static String getEnglishValue(JSONArray array){
		for (int index = 0; index<array.size(); index++){
			JSONObject name = (JSONObject)array.get(index);
			if (name.get(ONS_LANGUAGE_ATTRIBUTE_KEY).equals(ONS_LANGUAGE_ATTRIBUTE_VALUE_EN)){
				return (String)name.get(ONS_ATTRIBUTE_VALUE_KEY);
			}
		}
		return null;
	}
	
	private String dequote(String string){
		if (!string.startsWith("\""))
			return string;
		if (!string.endsWith("\""))
			return string;
		return string.substring(1, string.length()-1);
	}
}
