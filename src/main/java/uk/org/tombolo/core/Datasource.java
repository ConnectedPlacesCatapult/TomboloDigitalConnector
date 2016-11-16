package uk.org.tombolo.core;

import java.util.ArrayList;
import java.util.List;

public class Datasource {
	
	String id;
	Provider provider;
	String name;
	String description;
	String url;					// Url of the datasource for that series
	String remoteDatafile;		// Remote datafile
	String localDatafile; 		// Location of the local version of the datafile

	List<Attribute> timedValueAttributes = new ArrayList<>();
	List<Attribute> fixedValueAttributes = new ArrayList<>();
	
	public Datasource(String id, Provider provider, String name, String description){
		this.id = id;
		this.provider = provider;
		this.name = name;
		this.description = description;
	}
	
	public void addTimedValueAttribute(Attribute attribute){
		timedValueAttributes.add(attribute);
	}

	public void addAllTimedValueAttributes(List<Attribute> attributes){
		this.timedValueAttributes.addAll(attributes);
	}

	public void addFixedValueAttribute(Attribute attribute){
		fixedValueAttributes.add(attribute);
	}

	public void addAllFixedValueAttributes(List<Attribute> attributes){
		this.fixedValueAttributes.addAll(attributes);
	}

	public String getId(){
		return id;
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

	public List<Attribute> getTimedValueAttributes() {
		return timedValueAttributes;
	}

	public List<Attribute> getFixedValueAttributes() {
		return fixedValueAttributes;
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

	@Deprecated
	/**
	 * This should be handled internally by DownloadUtils
	 */
	public String getLocalDatafile() {
		return localDatafile;
	}

	@Deprecated
	/**
	 * This should be handled internally by DownloadUtils
	 */
	public void setLocalDatafile(String localDatafile) {
		this.localDatafile = localDatafile;
	}
	
}
