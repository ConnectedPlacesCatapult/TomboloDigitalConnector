package uk.org.tombolo.core;

import java.util.ArrayList;
import java.util.List;

public class Datasource {
	public static enum DatafileType {xls,xlsx,zip};
	
	Provider provider;
	String name;
	String description;
	String url;					// Url of the datasource for that series
	String remoteDatafile;		// Remote datafile
	String localDatafile; 		// Location of the local version of the datafile
	DatafileType datafileType;	// Type of the datafile

	List<Attribute> attributes;
	
	public Datasource(Provider provider, String name, String description){
		this.provider = provider;
		this.name = name;
		this.description = description;
		this.attributes = new ArrayList<Attribute>();
	}
	
	public void addAttribute(Attribute attribute){
		attributes.add(attribute);
	}

	public Provider getProvider() {
		return provider;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String getRemoteDatafile() {
		return remoteDatafile;
	}
	public void setRemoteDatafile(String remoteDatafile) {
		this.remoteDatafile = remoteDatafile;
	}

	public String getLocalDatafile() {
		return localDatafile;
	}	
	public void setLocalDatafile(String localDatafile) {
		this.localDatafile = localDatafile;
	}
	
}
