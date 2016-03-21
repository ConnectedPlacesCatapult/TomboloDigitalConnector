package uk.org.tombolo.core.utils;

import org.hibernate.Session;

import uk.org.tombolo.core.GeographyType;

public class GeographyTypeUtils {
	static Session session = HibernateUtil.getSessionFactory().openSession();

	public static GeographyType getUnknowhGeographyType(){
		GeographyType unknown = (GeographyType)session.load(GeographyType.class, "unknown");
		return unknown;
	}
	
	public static GeographyType getGeographyTypeByLabel(String label){
		return (GeographyType)session.load(GeographyType.class, label);
	}
}
