package uk.org.tombolo.core;

import com.google.gson.stream.JsonWriter;

import javax.persistence.*;
import java.io.IOException;

@Entity
@Table(name="attribute")
public class Attribute {
	private Integer id;
	private Provider provider;
	private String label;
	private String description;

	public Attribute() {}

	public Attribute(Provider provider, String label, String description){
		this.provider = provider;
		this.label = label;
		this.description = description;
	}
	
	// FIXME: Using allocationSize=1 may be inefficient and be problematic if we want multiple deployments to access the same db
	@Id
	@SequenceGenerator(name="attribute_id_sequence",sequenceName="attribute_id_sequence", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="attribute_id_sequence")
	@Column(name="id")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne()
	@JoinColumn(name="provider_label")
	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Column(name="label")
	public String getLabel(){
		return label;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="description")
	public String getDescription(){
		return description;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != Attribute.class)
			return false;
		
		Attribute attObj = (Attribute)obj;
		return this.getId().equals(attObj.getId());
	}

	public String uniqueLabel() {
		return String.join("_", this.getProvider().getLabel(), this.getLabel());
	}

	public void writeJSON(JsonWriter writer) throws IOException {
		writer.beginObject();
		writer.name("label").value(label);
		writer.name("description").value(description);
		writer.name("provider");
		provider.writeJSON(writer);
		writer.endObject();
	}
	
}
