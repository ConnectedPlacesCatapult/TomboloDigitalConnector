package uk.org.tombolo.importer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DownloadUtils {
	private static Logger log = LoggerFactory.getLogger(DownloadUtils.class);

	public static final String DEFAULT_DATA_CACHE_ROOT = "/tmp";
	private static final String TOMBOLO_DATA_CACHE_DIRECTORY = "TomboloData";

	private String tomboloDataCacheRootDirectory = DEFAULT_DATA_CACHE_ROOT;	// Configurable root to where to store cached data

	public DownloadUtils(String dataCacheRootDirectory){
		tomboloDataCacheRootDirectory = dataCacheRootDirectory;
	}

	public File fetchFile(URL url, String prefix, String suffix) throws IOException{
		createCacheDir(prefix);
		File localDatasourceFile = urlToLocalFile(url, prefix, suffix);
		log.info("Fetching local file: {}", localDatasourceFile.getCanonicalPath());
		if (!localDatasourceFile.exists()){
			// Local datafile does not exist so we should download it
			log.info("Local file not found: {} \nDownloading external resource: {}",
													localDatasourceFile.getCanonicalPath(), url.toString());
			FileUtils.copyURLToFile(url, localDatasourceFile);
		}
		return localDatasourceFile;
	}

	public InputStream fetchInputStream(URL url, String prefix, String suffix) throws IOException {
		createCacheDir(prefix);
		File localDatasourceFile = urlToLocalFile(url, prefix, suffix);


		log.info("Fetching local file: {}", localDatasourceFile.getCanonicalPath());
		if (!localDatasourceFile.exists()){
			log.info("Local file not found: {} \nDownloading external resource: {}",
												localDatasourceFile.getCanonicalPath(), url.toString());

			URLConnection urlConnection = url.openConnection();
			urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			urlConnection.connect();

			if (urlConnection instanceof HttpURLConnection) {
				HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
				int responseCode = httpURLConnection.getResponseCode();
				if (responseCode != HttpURLConnection.HTTP_OK) {
					log.warn(responseCode +": "+ httpURLConnection.getResponseMessage());
				}
			}

			return new TeeInputStream(urlConnection.getInputStream(), new FileOutputStream(localDatasourceFile));
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
		createCacheDir(prefix);
		File localDatasourceFile = urlToLocalFile(url, prefix,".json");
		log.info("Fetching local file: {}", localDatasourceFile.getCanonicalPath());
		if (!localDatasourceFile.exists()){
			log.info("Local file not found: {} \nDownloading external resource: {}",
												localDatasourceFile.getCanonicalPath(), url.toString());
			URLConnection connection = url.openConnection();
			// ONS requires this be set, or else you get 406 errors.
			connection.setRequestProperty("Accept", "application/json");
			return new TeeInputStream(connection.getInputStream(), new FileOutputStream(localDatasourceFile));
		} else {
			return new FileInputStream(localDatasourceFile);
		}
	}

	private File urlToLocalFile (URL url, String prefix, String suffix){
		String urlKey = UUID.nameUUIDFromBytes(url.toString().getBytes()).toString();
		return new File(
				tomboloDataCacheRootDirectory
						+ "/" + TOMBOLO_DATA_CACHE_DIRECTORY
						+ "/" + prefix
						+ "/" + urlKey
						+ suffix);
	}

	private void createCacheDir(String prefix) throws IOException {
		FileUtils.forceMkdir(new File(tomboloDataCacheRootDirectory + "/" + TOMBOLO_DATA_CACHE_DIRECTORY + "/" + prefix));
	}
}
