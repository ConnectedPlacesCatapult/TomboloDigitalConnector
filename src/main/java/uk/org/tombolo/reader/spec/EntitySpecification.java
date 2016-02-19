package uk.org.tombolo.reader.spec;

import uk.org.tombolo.writer.Entity;

public class EntitySpecification {

	Entity.Type type;
	
	SeriesSpecification idNameSpecification;

	public Entity.Type getType() {
		return type;
	}

	public SeriesSpecification getIdNameSpecification() {
		return idNameSpecification;
	}
	
}
