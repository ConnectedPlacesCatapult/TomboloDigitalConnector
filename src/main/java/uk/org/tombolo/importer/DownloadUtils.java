package uk.org.tombolo.importer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import uk.org.tombolo.datacatalogue.DatasourceSpecification;

public class DownloadUtils {

	public static final String TOMBOLO_DATA_CACHE_DIRECTORY = "TomboloData";
	
	public String tomboloDataCacheRootDirectory = "/tmp";	// Configurable root to where to store cached data
	
	public File getDatasourceFile(DatasourceSpecification datasource) throws MalformedURLException, IOException{
		File localDatasourceFile = new File(
				tomboloDataCacheRootDirectory 
				+ "/" + TOMBOLO_DATA_CACHE_DIRECTORY 
				+ "/" + datasource.getProvider() 
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
	
}
