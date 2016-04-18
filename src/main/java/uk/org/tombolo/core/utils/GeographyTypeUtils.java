package uk.org.tombolo.core.utils;

import org.hibernate.Hibernate;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;

import uk.org.tombolo.core.GeographyType;

public class GeographyTypeUtils {
	static Session session = HibernateUtil.getSessionFactory().openSession();

	public static GeographyType getUnknowhGeographyType(){
		GeographyType unknown = (GeographyType)session.load(GeographyType.class, "unknown");
		return unknown;
	}
	
	public static GeographyType getGeographyTypeByLabel(String label){
		GeographyType geographyType = (GeographyType)session.load(GeographyType.class, label);
		try {
			Hibernate.initialize(geographyType);
		}catch (ObjectNotFoundException e){
			return null;
		}
		return geographyType;
	}
	
	public static void save(GeographyType geographyType){
		session.beginTransaction();
		session.saveOrUpdate(geographyType);
		session.getTransaction().commit();
	}
}
