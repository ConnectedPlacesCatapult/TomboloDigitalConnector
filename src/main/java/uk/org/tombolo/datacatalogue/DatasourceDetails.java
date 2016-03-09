package uk.org.tombolo.datacatalogue;

import java.util.ArrayList;
import java.util.List;

import uk.org.tombolo.core.Attribute;
import uk.org.tombolo.core.Provider;

public class DatasourceDetails {

	Provider provider;
	String name;
	String description;
	List<Attribute> attributes;
	
	public DatasourceDetails(Provider provider, String name, String description){
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
	
}
