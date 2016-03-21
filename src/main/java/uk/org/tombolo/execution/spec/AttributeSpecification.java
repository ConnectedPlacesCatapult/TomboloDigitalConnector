package uk.org.tombolo.execution.spec;

import java.time.LocalDateTime;

public class AttributeSpecification {
	String providerLabel;
	String attributeLabel;
	LocalDateTime startTime;
	LocalDateTime endTime;
	
	public AttributeSpecification(String providerLabel, String attributeLabel){
		this.providerLabel = providerLabel;
		this.attributeLabel = attributeLabel;
	}

	public String getProviderLabel() {
		return providerLabel;
	}

	public String getAttributeLabel() {
		return attributeLabel;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}
	
}
