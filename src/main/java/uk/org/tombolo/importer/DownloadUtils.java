package uk.org.tombolo.importer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uk.org.tombolo.core.Datasource;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DownloadUtils {

	public static final String TOMBOLO_DATA_CACHE_DIRECTORY = "TomboloData";
	
	public String tomboloDataCacheRootDirectory = "/tmp";	// Configurable root to where to store cached data

	public DownloadUtils(){
		
	}
	
	public DownloadUtils(String dataCacheRootDirectory){
		tomboloDataCacheRootDirectory = dataCacheRootDirectory;
	}
	
	public File getDatasourceFile(Datasource datasource) throws MalformedURLException, IOException{
		File localDatasourceFile = new File(
				tomboloDataCacheRootDirectory 
				+ "/" + TOMBOLO_DATA_CACHE_DIRECTORY 
				+ "/" + datasource.getProvider().getLabel()
				+ "/" + datasource.getLocalDatafile());
		if (!localDatasourceFile.exists()){
			// Local datafile does not exist so we should download it
			FileUtils.copyURLToFile(new URL(datasource.getRemoteDatafile()), localDatasourceFile);
		}
		
		return localDatasourceFile;
	}
	
	public static String paramsToString(Map<String,String> params){
		List<String> paramList = new ArrayList<String>();
		for (String key : params.keySet()){
			paramList.add(key+"="+params.get(key));
		}
		return paramList.stream().collect(Collectors.joining("&"));
	}

	public JSONObject fetchJSON(URL url) throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		return (JSONObject) parser.parse(new InputStreamReader(fetchJSONStream(url)));
	}

	public InputStream fetchJSONStream(URL url) throws IOException {
		String urlKey = Base64.getUrlEncoder().encodeToString(url.toString().getBytes());
		File localDatasourceFile = new File(
				tomboloDataCacheRootDirectory
						+ "/" + TOMBOLO_DATA_CACHE_DIRECTORY
						+ "/" + urlKey + ".json");
		if (!localDatasourceFile.exists()){
			URLConnection connection = url.openConnection();
			// ONS requires this be set, or else you get 406 errors.
			connection.setRequestProperty("Accept", "application/json");
			return new TeeInputStream(connection.getInputStream(), new FileOutputStream(localDatasourceFile));
		} else {
			return new FileInputStream(localDatasourceFile);
		}
	}
}
