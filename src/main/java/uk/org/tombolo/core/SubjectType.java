package uk.org.tombolo.core;

import com.google.gson.stream.JsonWriter;

import javax.persistence.*;
import java.io.IOException;

@Entity
@Table(name="subject_type")
public class SubjectType {
	Integer id;
	Provider provider;
	String label;
	String name;
	
	public SubjectType() {
		
	}
	
	public SubjectType(Provider provider, String label, String name){
		this.provider = provider;
		this.label = label;
		this.name = name;
	}


	@Id
	@SequenceGenerator(name="subject_type_id_sequence",sequenceName="subject_type_id_sequence", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="subject_type_id_sequence")
	@Column(name="id")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne()
	@JoinColumn(name="provider_label")
	public Provider getProvider() { return provider; }

	public void setProvider(Provider provider) { this.provider = provider;}

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
