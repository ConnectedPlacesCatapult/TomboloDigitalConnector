package uk.org.tombolo.datacatalogue;

import uk.org.tombolo.core.Datasource.DatafileType;

@Deprecated
public class DatasourceSpecification {

	String id;
	
	// Datafile information
	String provider;			// The data provider	
	String url;					// Url of the datasource for that series
	String remoteDatafile;		// Remote datafile
	String localDatafile; 		// Location of the local version of the datafile
	DatafileType datafileType;	// Type of the datafile

	public DatasourceSpecification(String id, String provider, 
			String url, String remoteDatafile, String localDatafile, 
			DatafileType datafileType){
		this.id = id;
		this.provider = provider;
		this.url = url;
		this.remoteDatafile = remoteDatafile;
		this.localDatafile = localDatafile;
		this.datafileType = datafileType;
	}
	
	public String getId() {
		return id;
	}
	public String getProvider(){
		return provider;
	}
	public String getUrl() {
		return url;
	}
	public String getRemoteDatafile() {
		return remoteDatafile;
	}
	public String getLocalDatafile() {
		return localDatafile;
	}
	public DatafileType getDatafileType() {
		return datafileType;
	}
}
