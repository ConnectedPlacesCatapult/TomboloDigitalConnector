package uk.org.tombolo.core;

import com.vividsolutions.jts.geom.Geometry;

import javax.persistence.*;

@Entity
@Table(name="subject")
public class Subject {
	// SRID in the system config file
	public static int SRID;

	Integer id;
	SubjectType subjectType;
	String label;
	String name;

	Geometry shape;
		
	public Subject(){}
	
	public Subject(SubjectType subjectType, String label, String name, Geometry shape){
		this.subjectType = subjectType;
		this.label = label;
		this.name = name;
		this.shape = shape;
	}

	@Id
	@SequenceGenerator(name="subject_id_sequence",sequenceName="subject_id_sequence", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="subject_id_sequence")
	@Column(name="id")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne()
	@JoinColumn(name="subject_type_id")
	public SubjectType getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(SubjectType subjectType) {
		this.subjectType = subjectType;
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

	@Column(name="shape", columnDefinition="Geometry")
	public Geometry getShape() {
		return shape;
	}

	public void setShape(Geometry shape) {
		this.shape = shape;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != Subject.class)
			return false;
		
		Subject geoObj = (Subject)obj;
		return this.getId().equals(geoObj.getId());
	}
	
	
}
