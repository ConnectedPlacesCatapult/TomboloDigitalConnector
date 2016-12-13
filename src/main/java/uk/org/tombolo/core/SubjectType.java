package uk.org.tombolo.core;

import com.google.gson.stream.JsonWriter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.IOException;

@Entity
@Table(name="subject_type")
public class SubjectType {
	String label;
	String name;
	
	public SubjectType() {
		
	}
	
	public SubjectType(String label, String name){
		this.label = label;
		this.name = name;
	}

	@Id
	@Column(name="label")
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Column(name="name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void writeJSON(JsonWriter writer) throws IOException {
		writer.beginObject();
		writer.name("label").value(label);
		writer.name("name").value(name);
		writer.endObject();
	}
}
