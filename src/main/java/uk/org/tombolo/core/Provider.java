package uk.org.tombolo.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="provider")
public class Provider {

	private String label;
	private String name;
	
	public Provider(){
	}
	
	public Provider(String label, String name){
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
	
}
