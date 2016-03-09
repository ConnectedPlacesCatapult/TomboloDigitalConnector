package uk.org.tombolo.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.MultiPolygon;

@Entity
@Table(name="geography_object")
public class Geography {

	Integer id;
	GeographyType geographyType;
	String label;
	String name;
	//FIXME: Here I wanted to use 'Geometry shape;' rather than 'MultiPolygon shape;' but I ran into issues with ORM mapping.
	//       This will work for all the areas we will be using for the time being (lsoa, msoa and la) but we might have to revisit
	//       this as we generalize the connector.
	MultiPolygon shape;
		
	public Geography(){}

	@Id
	@Column(name="id")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne()
	@JoinColumn(name="geography_type_label")
	public GeographyType getGeographyType() {
		return geographyType;
	}

	public void setGeographyType(GeographyType geographyType) {
		this.geographyType = geographyType;
	}

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

	@Column(name="shape", columnDefinition="geometry(MultiPolygon,4326)")
	@Type(type = "org.hibernate.spatial.GeometryType")
	public MultiPolygon getShape() {
		return shape;
	}

	public void setShape(MultiPolygon shape) {
		this.shape = shape;
	}
}
