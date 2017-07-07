package uk.org.tombolo.importer;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static Logger log = LoggerFactory.getLogger(DownloadUtils.class);

	public static final String DEFAULT_DATA_CACHE_ROOT = "/tmp";
	private static final String TOMBOLO_DATA_CACHE_DIRECTORY = "TomboloData";
	
	private String tomboloDataCacheRootDirectory = DEFAULT_DATA_CACHE_ROOT;	// Configurable root to where to store cached data

	public DownloadUtils(String dataCacheRootDirectory){
		tomboloDataCacheRootDirectory = dataCacheRootDirectory;
	}

	public File fetchFile(URL url, String prefix, String suffix) throws MalformedURLException, IOException{
		createCacheDir();
		File localDatasourceFile = urlToLocalFile(url, prefix, suffix);
		log.info("Feching local file: {}", localDatasourceFile.getName());
		if (!localDatasourceFile.exists()){
			// Local datafile does not exist so we should download it
			log.info("Downloading external resource: {}",url.toString());
			FileUtils.copyURLToFile(url, localDatasourceFile);
		}
		return localDatasourceFile;
	}

	public InputStream fetchInputStream(URL url, String prefix, String suffix) throws IOException {
		createCacheDir();
		File localDatasourceFile = urlToLocalFile(url, prefix, suffix);
		log.info("Feching local file: {}", localDatasourceFile.getName());
		if (!localDatasourceFile.exists()){
			URLConnection connection = url.openConnection();
			return new TeeInputStream(connection.getInputStream(), new FileOutputStream(localDatasourceFile));
		} else {
			return new FileInputStream(localDatasourceFile);
		}
	}

	public static String paramsToString(Map<String,String> params){
		List<String> paramList = new ArrayList<String>();
		for (String key : params.keySet()){
			paramList.add(key+"="+params.get(key));
		}
		return paramList.stream().collect(Collectors.joining("&"));
	}

	public JSONObject fetchJSON(URL url, String prefix) throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		return (JSONObject) parser.parse(new InputStreamReader(fetchJSONStream(url, prefix)));
	}

	public InputStream fetchJSONStream(URL url, String prefix) throws IOException {
		createCacheDir();
		File localDatasourceFile = urlToLocalFile(url, prefix,".json");
		if (!localDatasourceFile.exists()){
			URLConnection connection = url.openConnection();
			// ONS requires this be set, or else you get 406 errors.
			connection.setRequestProperty("Accept", "application/json");
			return new TeeInputStream(connection.getInputStream(), new FileOutputStream(localDatasourceFile));
		} else {
			return new FileInputStream(localDatasourceFile);
		}
	}

	private File urlToLocalFile (URL url, String prefix, String suffix){
		String urlKey = Base64.getUrlEncoder().encodeToString(url.toString().getBytes());
		if (urlKey.length() > 250)
			// The urlKey is too long for creating a file
			urlKey = DigestUtils.md5Hex(urlKey);
		return new File(
				tomboloDataCacheRootDirectory
						+ "/" + TOMBOLO_DATA_CACHE_DIRECTORY
						+ "/" + prefix
						+ "/" + urlKey
						+ suffix);
	}

	private void createCacheDir() throws IOException {
		FileUtils.forceMkdir(new File(tomboloDataCacheRootDirectory + "/" + TOMBOLO_DATA_CACHE_DIRECTORY));
	}
}
