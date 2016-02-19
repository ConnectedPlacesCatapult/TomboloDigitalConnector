package uk.org.tombolo.importer;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.datacatalogue.DatasourceDetails;
import uk.org.tombolo.datacatalogue.DatasourceSpecification;

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
public class ONSCensusImporter {
	private static final String ONS_API_URL = "http://data.ons.gov.uk/ons/api/data/";
	private static final String ONS_API_KEY = "kil61db9uf";

	public static final String ONS_PROVIDER_ID = "uk.gov.ons";
	
	private static final String ONS_LANGUAGE_ATTRIBUTE_KEY = "@xml.lang";
	private static final String ONS_LANGUAGE_ATTRIBUTE_VALUE_EN = "en";
	private static final String ONS_ATTRIBUTE_VALUE_KEY = "$";
	
	private static final String baseUrl = "http://data.statistics.gov.uk/ons/datasets/";
	private static final String filePrefix = "csv/CSV_";
	private static final String filePostfix = "_2011STATH_1_EN.zip";

	DownloadUtils downloadUtils = new DownloadUtils();
		
	public void loadDataset(String datasetId) throws MalformedURLException, IOException{
		DatasourceSpecification datasource = new DatasourceSpecification(
				datasetId,ONS_PROVIDER_ID,
				"http://www.ons.gov.uk/ons/datasets-and-tables/index.html",
				baseUrl + filePrefix + datasetId + filePostfix,
				filePrefix + datasetId + filePostfix,
				DatasourceSpecification.DatafileType.zip);
		
		File localFile = downloadUtils.getDatasourceFile(datasource);
		
	}
	
	public DatasourceDetails getDatasetDetails(String datasetId) throws IOException, ParseException{
		// Set-up the basic url and the parameters
		String baseUrl = ONS_API_URL + "datasetdetails/" + datasetId + ".json?";
		Map<String,String> params = new HashMap<String,String>();
		params.put("context", "Census");
		params.put("apikey", ONS_API_KEY);
		params.put("geog", "2011STATH");
		String paramsString = DownloadUtils.paramsToString(params);
		URL url = new URL(baseUrl + paramsString);
		
		// Parse the json content
		JSONParser parser = new JSONParser();
		JSONObject rootObject = (JSONObject) parser.parse(new InputStreamReader(url.openStream()));
		
		JSONObject ons = (JSONObject)rootObject.get("ons");
		JSONObject datasetDetail = (JSONObject)ons.get("datasetDetail");
		
		// Get dataset English name
		JSONArray names = (JSONArray)((JSONObject)datasetDetail.get("names")).get("name");
		String datasetDescription = getEnglishValue(names);
		DatasourceDetails datasourceDetails = new DatasourceDetails(ONS_PROVIDER_ID,datasetId,datasetDescription);
		
		// Get dataset dimensions
		JSONArray dimensions = (JSONArray)((JSONObject)datasetDetail.get("dimensions")).get("dimension");
		for (int dimIndex = 0; dimIndex<dimensions.size(); dimIndex++){
			JSONObject dimension = (JSONObject)dimensions.get(dimIndex);
			String dimensionType = (String)dimension.get("dimensionType");
			if (dimensionType.equals("Topic")){
				// The dimension is of type "Topic", i.e. an attribute in our terminology
				String attributeName = (String)dimension.get("dimensionId");
				JSONArray dimensionTitles = (JSONArray)((JSONObject)dimension.get("dimensionTitles")).get("dimensionTitle");
				String attributeDescription = getEnglishValue(dimensionTitles);
				// FIXME: The Attribute.DataType value should be gotten from somewhere rather than defaulting to numeric
				Attribute attribute = new Attribute(ONS_PROVIDER_ID, attributeName, attributeDescription, Attribute.DataType.numeric);
				datasourceDetails.addAttribute(attribute);
			}
		}
		
		return datasourceDetails;
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
}
