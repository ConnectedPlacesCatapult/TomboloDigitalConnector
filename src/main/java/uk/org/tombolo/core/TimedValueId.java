package uk.org.tombolo.core;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

@Embeddable
public class TimedValueId implements Serializable {
	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name="geography_id")
	Subject geography;

	@ManyToOne
	@JoinColumn(name="attribute_id")
	Attribute attribute;
	
	@Column(name="timestamp")
	@Type(type="uk.org.tombolo.core.utils.LocalDateTimeUserType")
	LocalDateTime timestamp;

	public TimedValueId(){
		
	}
	
	public TimedValueId(Subject geography, Attribute attribute, LocalDateTime timestamp){
		this.geography = geography;
		this.attribute = attribute;
		this.timestamp = timestamp;
	}

	public Subject getGeography() {
		return geography;
	}

	public void setGeography(Subject geography) {
		this.geography = geography;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public int hashCode() {
		return (geography.getId()
				+"\t"+attribute.getId()
				+"\t"+timestamp.toString()).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != this.getClass())
			return false;
		
		TimedValueId idObj = (TimedValueId)obj;
		if (idObj.geography.equals(this.geography)
				&& idObj.attribute.equals(this.attribute)
				&& idObj.timestamp.equals(this.timestamp))
			return true;
		
		return false;
	}
	
}