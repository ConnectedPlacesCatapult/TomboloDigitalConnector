package uk.org.tombolo.core;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Datasource {
	
	private DatasourceSpec datasourceSpec;
	private List<SubjectType> subjectTypes;
	private List<Attribute> timedValueAttributes;
	private List<Attribute> fixedValueAttributes;

	public Datasource(DatasourceSpec datasourceSpec){
		this.datasourceSpec = datasourceSpec;
		this.subjectTypes = Collections.emptyList();
		this.timedValueAttributes = Collections.emptyList();
		this.fixedValueAttributes = Collections.emptyList();
	}

	public List<SubjectType> getSubjectTypes() {
		return subjectTypes;
	}

	public void addSubjectTypes(List<SubjectType> subjectTypes) {
		this.subjectTypes = subjectTypes;
	}

	public List<Attribute> getTimedValueAttributes() {
		return timedValueAttributes;
	}

	public void addTimedValueAttributes(List<Attribute> timedValueAttributes) {
		this.timedValueAttributes = timedValueAttributes;
	}

	public List<Attribute> getFixedValueAttributes() {
			return fixedValueAttributes;
	}

	public void addFixedValueAttributes(List<Attribute> fixedValueAttributes) {
		this.fixedValueAttributes = fixedValueAttributes;
	}

	public SubjectType getUniqueSubjectType() {
		if (subjectTypes.size() != 1) {
			throw new Error(String.format("Datasource %s expected to have 1 SubjectType, has %d", datasourceSpec.getId(), subjectTypes.size()));
		}
		return subjectTypes.get(0);
	}

	public DatasourceSpec getDatasourceSpec() {
		return datasourceSpec;
	}

	public void writeJSON(JsonWriter writer) throws IOException {
		writer.beginObject();
		writer.name("id").value(datasourceSpec.getId());
		writer.name("importerClass").value(datasourceSpec.getImporterClass().getCanonicalName());
		writer.name("name").value(datasourceSpec.getName());
		writer.name("description").value(datasourceSpec.getDescription());
		writer.name("url").value(datasourceSpec.getUrl());
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
