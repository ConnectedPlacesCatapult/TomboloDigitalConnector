package uk.org.tombolo.core;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Embeddable
public class TimedValueId implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	@ManyToOne
	@JoinColumn(name="subject_id")
	Subject subject;

	@ManyToOne
	@JoinColumn(name="attribute_id")
	Attribute attribute;
	
	@Column(name="timestamp")
	LocalDateTime timestamp;

	public TimedValueId(){
		
	}
	
	public TimedValueId(Subject subject, Attribute attribute, LocalDateTime timestamp){
		this.subject = subject;
		this.attribute = attribute;
		this.timestamp = timestamp;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
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
		return (subject.getId()
				+"\t"+attribute.getId()
				+"\t"+timestamp.toString()).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != this.getClass())
			return false;
		
		TimedValueId idObj = (TimedValueId)obj;
		if (idObj.subject.equals(this.subject)
				&& idObj.attribute.equals(this.attribute)
				&& idObj.timestamp.equals(this.timestamp))
			return true;
		
		return false;
	}
	
}