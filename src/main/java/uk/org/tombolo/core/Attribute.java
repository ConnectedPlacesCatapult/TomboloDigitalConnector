package uk.org.tombolo.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="attribute")
public class Attribute {
	public static enum DataType {string,numeric};

	Integer id;
	Provider provider;
	String label;
	String name;
	String description;
	
	// FIXME: Maybe deprecate ... or implement properly
//	DataType dataType;
		
	public Attribute(){
		
	}
	
	public Attribute(Provider provider, String label, String name, String description, DataType dataType){
		this.provider = provider;
		this.label = label;
		this.name = name;
		this.description = description;
//		this.dataType = dataType;
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
	
	@Column(name="name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		if (obj.getClass() != Geography.class)
			return false;
		
		Attribute attObj = (Attribute)obj;
		return this.getId().equals(attObj.getId());
	}

	public String uniqueLabel() {
		return String.join("_", this.getProvider().getLabel(), this.getLabel());
	}
	
//	public DataType getDataType(){
//		return dataType;
//	}
	
}
