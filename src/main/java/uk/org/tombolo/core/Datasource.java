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

	List<Attribute> attributes;
	
	public Datasource(String id, Provider provider, String name, String description){
		this.id = id;
		this.provider = provider;
		this.name = name;
		this.description = description;
		this.attributes = new ArrayList<Attribute>();
	}
	
	public void addAttribute(Attribute attribute){
		attributes.add(attribute);
	}

	public void addAllAttributes(List<Attribute> attributes){
		this.attributes.addAll(attributes);
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

	public List<Attribute> getAttributes() {
		return attributes;
	}
	
	public Attribute getAttributeByLabel(String label){
		for (Attribute attribute : attributes){
			if (label.equals(attribute.getLabel()))
				return attribute;
		}
		return null;
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
