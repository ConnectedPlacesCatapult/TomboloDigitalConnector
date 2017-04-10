package uk.org.tombolo.core;

import com.google.gson.stream.JsonWriter;
import uk.org.tombolo.importer.Importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Datasource {
	
	Class<? extends Importer> importerClass;
	String id;
	Provider provider;
	String name;
	String description;
	String url;					// Url of the datasource for that series
	String remoteDatafile;		// Remote datafile

	List<Attribute> timedValueAttributes = new ArrayList<>();
	List<Attribute> fixedValueAttributes = new ArrayList<>();
	List<SubjectType> subjectTypes = new ArrayList<>();
	
	public Datasource(Class<? extends Importer> importerClass, String id, Provider provider, String name, String description){
		this.importerClass = importerClass;
		this.id = id;
		this.provider = provider;
		this.name = name;
		this.description = description;
	}

	public void addSubjectType(SubjectType subjectType){
		subjectTypes.add(subjectType);
	}

	public void addAllSubjectTypes(List<SubjectType> subjectTypes){
		this.subjectTypes.addAll(subjectTypes);
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

	public Class<? extends Importer> getImporterClass() {
		return importerClass;
	}

	public void setImporterClass(Class<? extends Importer> importerClass) {
		this.importerClass = importerClass;
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

	public List<SubjectType> getSubjectTypes() {
		return subjectTypes;
	}

	public SubjectType getUniqueSubjectType() {
		if (subjectTypes.size() != 1) { throw new Error(String.format("Datasource %s expected to have 1 SubjectType, has %s", getId(), subjectTypes.size())); }
		return subjectTypes.get(0);
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

	/**
	 * The use of the remote datafile is discouraged since the one-to-one relation between a datasource and datafile
	 * is not applicable for all importers.
	 *
	 * When we have found out how to solve the Census Importer then this will be deleted
	 *
	 * @return
	 */
	@Deprecated
	public String getRemoteDatafile() {
		return remoteDatafile;
	}
	@Deprecated
	public void setRemoteDatafile(String remoteDatafile) {
		this.remoteDatafile = remoteDatafile;
	}

	public void writeJSON(JsonWriter writer) throws IOException {
		writer.beginObject();
		writer.name("id").value(id);
		writer.name("importerClass").value(importerClass.getCanonicalName());
		writer.name("name").value(name);
		writer.name("description").value(description);
		writer.name("url").value(url);
		writer.name("remoteDatafile").value(remoteDatafile);
		writer.name("provider");
		provider.writeJSON(writer);
		writer.name("subjectTypes");
		writer.beginArray();
		for (SubjectType subjectType : getSubjectTypes()) {
			subjectType.writeJSON(writer);
		}
		writer.endArray();
		writer.name("timedValueAttributes");
		writer.beginArray();
		for (Attribute attribute : getTimedValueAttributes()) {
			attribute.writeJSON(writer);
		}
		writer.endArray();
		writer.name("fixedValueAttributes");
		writer.beginArray();
		for (Attribute attribute : getFixedValueAttributes()) {
			attribute.writeJSON(writer);
		}
		writer.endArray();
		writer.endObject();
	}
	
}
