package uk.org.tombolo.core;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

// FIXME: Probably turn this into and interface and a DoubleTimedValue class
@Entity
@Table(name="timed_value")
public class TimedValue {
	
	@EmbeddedId
	TimedValueId id;
	
	@Column(name="value")
	Double value;
	
	public TimedValue(){
		
	}
	
	public TimedValue(Subject subject, Attribute attribute, LocalDateTime timestamp, Double value){
		this.id = new TimedValueId(subject, attribute, timestamp);
		this.value = value;
	}
	
	public TimedValueId getId() {
		return id;
	}

	public void setId(TimedValueId id) {
		this.id = id;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

}
