package uk.org.tombolo.core.utils;

import org.hibernate.Hibernate;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;

import uk.org.tombolo.core.GeographyType;

public class GeographyTypeUtils {
	public static GeographyType getGeographyTypeByLabel(String label){
		return HibernateUtil.withSession(session -> {
			return (GeographyType) session.get(GeographyType.class, label);
		});
	}
	
	public static void save(GeographyType geographyType){
		HibernateUtil.withSession(session -> {
			session.beginTransaction();
			session.saveOrUpdate(geographyType);
			session.getTransaction().commit();
		});
	}
}
